package HMMParallel.Demo;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class HmmParallel {
	public static class HmmParallelMap extends Mapper<Object, Text, Text, ArrayWritable>{
		
    // the number of Observations;
    private static int Observations;
    // the number of States;
    private static int States;
    // the observationSequence;
    int[] observedSequence;
    // the hiddenSequence;
    int[] hiddenSequence;
		private HmmModel hmm;
		
		private ArrayWritable piStripe;
		private ArrayWritable emissionStripe;
		private ArrayWritable transitionStripe;
		private Writable[] pi_tmp;
		private Writable[] em_tmp;
		private Writable[] tr_tmp;
    // the Observations of training data
    int[] o;
  
    /**
     * Create an supervised initial estimate of an HMM Model based on
     * transitionMatrix sequence of observed and hidden states.
     * 
     * @return An initial model using the estimated parameters
     */
		public void setup(Context context)
				throws IOException, InterruptedException{
			hmm = HmmTrainer.trainSupervised(States, Observations, observedSequence, hiddenSequence, 0);	
		}
		
    /**
     * Map function of the HMM Parallel. The input of <key, value> is: key
     * contains the offset of the first character in a line, value contains one
     * line data of the training data. The output of <key, value> is: <initial,
     * initialProbabilities>, <emit from states, Stripe emissionMatrix>,
     * <transit from states, Stripe transitionMatrix>
     */
		public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException{

			for(int i = 0; i < value.getLength(); i++){
				o[i] = value.charAt(i);
			}
			
			int T = value.getLength();
			double[][] fwd;
			double[][] bwd;
			
      double initialProbabilities[] = new double[hmm.numStates];
      double transitionMatrix[][] = new double[hmm.numStates][hmm.numStates];
      double emissionMatrix[][] = new double[hmm.numStates][hmm.sigmaSize];
	
			//calculation of forward and backward Variables from the current model
			fwd = HmmAlgorithms.forwardAlgorithm(hmm, o, true);
			bwd = HmmAlgorithms.backwardAlgorithm(hmm, o, true);
			
			//re-estimation of initial state probabilities
			for(int i = 0; i < hmm.numStates; i++)
        initialProbabilities[i] = gamma(i, 0, o, fwd, bwd);
			
			//re-estimation of transition probabilities
			for(int i =0; i < hmm.numStates; i++){
				for(int j = 0; j < hmm.numStates; j++){
					double num = 0;
					double denum = 0;
					
					for(int t = 0; t < T - 1; t++){
						num += p(t, i, j, o, fwd, bwd);
						denum += gamma(i, t, o, fwd, bwd);
					}
          transitionMatrix[i][j] = divide(num, denum);
				}
			}
			
			//re-estimation of emission probabilities
			for(int i = 0; i < hmm.numStates; i++){
				for(int k = 0; k <hmm.sigmaSize; k++){
					double num = 0;
					double denum = 0;
					
					for(int t = 0; t < T - 1; t++){
						double g = gamma(i, t, o, fwd, bwd);
						num += g * (k == o[t] ? 1 : 0);
						denum += g;
					}
          emissionMatrix[i][k] = divide(num, denum);
				}
			}
			
      // emit the context
			for(int i = 0; i < hmm.numStates; i++){
        pi_tmp[i] = new DoubleWritable(hmm.initialProbabilities[i]);
			}
			piStripe.set(pi_tmp);
			context.write(new Text("initial"), piStripe);
			
			
			for(int i = 0; i < hmm.numStates; i++){
				for(int j = 0; j < hmm.sigmaSize; j++){
          em_tmp[j] = new DoubleWritable(hmm.emissionMatrix[i][j]);
				}
				emissionStripe.set(em_tmp);
				context.write(new Text("emit from" + hiddenSequence.toString()), emissionStripe);
			}
			
			
			for(int i = 0; i < hmm.numStates; i++){
				for(int j = 0; j < hmm.numStates; j++){
          tr_tmp[j] = new DoubleWritable(hmm.transitionMatrix[i][j]);
				}
				transitionStripe.set(tr_tmp);
				context.write(new Text("transit from" + hiddenSequence.toString()), transitionStripe);
			}
		}

		/** calculation of probability P(X_t = s_i, X_t+1 = s_j | O, m).
	      @param t time t
	      @param i the number of state s_i
	      @param j the number of state s_j
	      @param o an output sequence o
	      @param fwd the Forward-Variables for o
	      @param bwd the Backward-Variables for o
	      @return P
	  */
	  public double p(int t, int i, int j, int[] o, double[][] fwd, double[][] bwd) {
	    double num;
	    if (t == o.length - 1)
        num = fwd[i][t] * hmm.transitionMatrix[i][j];
	    else
        num =
            fwd[i][t] * hmm.transitionMatrix[i][j]
                * hmm.emissionMatrix[j][o[t + 1]]
                * bwd[j][t + 1];

	    double denom = 0;

	    for (int k = 0; k < hmm.numStates; k++)
	      denom += (fwd[k][t] * bwd[k][t]);
      // normalize the rows of the matrix
	    return divide(num, denom);
	  }

    /**
     * computes gamma(i, t)
     * */
	  public double gamma(int i, int t, int[] o, double[][] fwd, double[][] bwd) {
	    double num = fwd[i][t] * bwd[i][t];
	    double denom = 0;

	    for (int j = 0; j < hmm.numStates; j++)
	      denom += fwd[j][t] * bwd[j][t];
	    // normalize the rows of matrix
	    return divide(num, denom);
	  }

	  /** divides two doubles. 0 / 0 = 0! */
	  public double divide(double n, double d) {
	    if (n == 0)
	      return 0;
	    else
	      return n / d;
	  }
	}
	
	public static class HmmParallelReduce extends Reducer<Text, ArrayWritable, Text, ArrayWritable>{
		private ArrayWritable Cf;
		private DoubleWritable[] Cf_tmp;
		double[] doubleValue;
		
    /**
     * Reduce function of the HMM Parallel. The input of <key, value> is:
     * <initial, initialProbabilities>, <emit from states, Stripe
     * emissionMatrix>, <transit from states, Stripe transitionMatrix>. Then
     * this part aggregates the results from different mappers. The output of
     * <key, value> is also: <initial, initialProbabilities>, <emit from states,
     * Stripe emissionMatrix>, <transit from states, Stripe transitionMatrix>
     */
		public void reduce(Text key, Iterable<ArrayWritable> values, Context context)
			throws IOException, InterruptedException{

		
			for(ArrayWritable value : values){
				for(int i = 0; i < value.get().length ;i++){
					doubleValue[i] += Double.parseDouble(value.get()[i].toString());
					Cf_tmp[i] = new DoubleWritable(doubleValue[i]);
				}
			}
			Cf.set(Cf_tmp);
			double z = 0;
			for(int i = 0; i < Cf.get().length; i++){
				z += Double.parseDouble(Cf.get()[i].toString());
			}
			//normalization
			for(int i = 0; i < Cf.get().length; i++){
				Cf_tmp[i] = new DoubleWritable(Double.parseDouble(Cf.get()[i].toString()) / z);
			}
			Cf.set(Cf_tmp);
			
			context.write(key, Cf);
		}
	}
	
	
	public static void main(String[] args)
			throws IOException, InterruptedException, ClassNotFoundException{

    /**
     * The first arguments of the map-reduce program is: the input Path of the
     * training data; the second arguments is the output Path of the result; the
     * third arguments is the number of iteration.
     */
		int iterationNum = Integer.parseInt(args[2]);
		
		Job hmmParallel = new Job();
		hmmParallel.setJobName("hmmParallel");
		hmmParallel.setJarByClass(HmmParallel.class);
		
		FileInputFormat.addInputPath(hmmParallel, new Path(args[0]));
		FileOutputFormat.setOutputPath(hmmParallel, new Path("temp0"));
		
		hmmParallel.setMapperClass(HmmParallelMap.class);
		hmmParallel.setReducerClass(HmmParallelReduce.class);
		hmmParallel.setMapOutputKeyClass(Text.class);
		hmmParallel.setMapOutputValueClass(ArrayWritable.class);
		hmmParallel.setOutputKeyClass(Text.class);
		hmmParallel.setOutputValueClass(ArrayWritable.class);
		hmmParallel.setOutputFormatClass(SequenceFileOutputFormat.class);
		hmmParallel.setInputFormatClass(SequenceFileInputFormat.class);
		
		hmmParallel.waitForCompletion(true);
		for(int i = 1; i < iterationNum; i++){
			Job loopJob = new Job();
			loopJob.setJobName("loopJob");
			loopJob.setJarByClass(HmmParallel.class);
			
			FileInputFormat.addInputPath(loopJob, new Path("temp" + new Integer(i - 1)));
			if(i == (iterationNum - 1))
				FileOutputFormat.setOutputPath(loopJob, new Path(args[1]));
			else
				FileOutputFormat.setOutputPath(loopJob, new Path("temp" + new Integer(i)));
			
			loopJob.setMapperClass(HmmParallelMap.class);
			loopJob.setReducerClass(HmmParallelReduce.class);
			loopJob.setMapOutputKeyClass(Text.class);
			loopJob.setMapOutputValueClass(ArrayWritable.class);
			loopJob.setOutputKeyClass(Text.class);
			loopJob.setOutputValueClass(ArrayWritable.class);
			loopJob.setInputFormatClass(SequenceFileInputFormat.class);
			loopJob.setOutputFormatClass(SequenceFileOutputFormat.class);
			
			loopJob.waitForCompletion(true);
			FileSystem.get(new Configuration()).delete(new Path("temp" + new Integer(i - 1)));
		}
	}
}
