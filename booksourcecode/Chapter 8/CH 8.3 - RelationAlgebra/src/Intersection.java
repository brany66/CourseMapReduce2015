import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


/**
 * 求交集，对于每个record发送(record,1)，reduce
 * 时值为2才发射此record
 * @author KING
 *
 */
public class Intersection {
	public static class IntersectionMap extends Mapper<LongWritable, Text, RelationA, IntWritable>{
		private IntWritable one = new IntWritable(1);
		@Override
		public void map(LongWritable offSet, Text line, Context context)throws 
		IOException, InterruptedException{
			RelationA record = new RelationA(line.toString());
			context.write(record, one);
		}
	}
	public static class IntersectionReduce extends Reducer<RelationA, IntWritable, RelationA, NullWritable>{
		@Override
		public void reduce(RelationA key, Iterable<IntWritable> value, Context context) throws 
		IOException,InterruptedException{
			int sum = 0;
			for(IntWritable val : value){
				sum += val.get();
			}
			if(sum == 2)
				context.write(key, NullWritable.get());
		}
	}
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
		Job intersectionJob = new Job();
		intersectionJob.setJobName("intersectionJob");
		intersectionJob.setJarByClass(Intersection.class);
	
		intersectionJob.setMapperClass(IntersectionMap.class);
		intersectionJob.setMapOutputKeyClass(RelationA.class);
		intersectionJob.setMapOutputValueClass(IntWritable.class);

		intersectionJob.setReducerClass(IntersectionReduce.class);
		intersectionJob.setOutputKeyClass(RelationA.class);
		intersectionJob.setOutputValueClass(NullWritable.class);

		intersectionJob.setInputFormatClass(TextInputFormat.class);
		intersectionJob.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(intersectionJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(intersectionJob, new Path(args[1]));
		
		intersectionJob.waitForCompletion(true);
		System.out.println("finished!");
	}
}
