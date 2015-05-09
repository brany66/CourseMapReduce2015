import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * 专利被引用次数分布统计，输入文件为专利引用次数统计的输出结果
 * 扫描文件忽略专利号，仅仅考虑被引用的次数，统计每一个次数分别
 * 有多少次出现。
 * 
 * @author KING
 *
 */
public class CitationCountDistribution {
	public static class MapClass extends Mapper<Text, Text, IntWritable, IntWritable> {
		private IntWritable one = new IntWritable(1);
		
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			IntWritable citationCount = new IntWritable(Integer.parseInt(value.toString()));
			context.write (citationCount, one);
	     }
	}
	
	public static class ReduceClass extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable>{
		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int count = 0;
			for(IntWritable val : values){
				count += val.get();
			}
			 // 输出key: 被引次数；value: 总出现次数
			context.write(key, new IntWritable(count));
			}
		}
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
		Job citationCountDistributionJob = new Job();
		citationCountDistributionJob.setJobName("citationCountDistributionJob");
		citationCountDistributionJob.setJarByClass(CitationCountDistribution.class);
		
		citationCountDistributionJob.setMapperClass(MapClass.class);
		citationCountDistributionJob.setMapOutputKeyClass(IntWritable.class);
		citationCountDistributionJob.setMapOutputValueClass(IntWritable.class);

		citationCountDistributionJob.setReducerClass(ReduceClass.class);
		citationCountDistributionJob.setOutputKeyClass(IntWritable.class);
		citationCountDistributionJob.setOutputValueClass(IntWritable.class);

		citationCountDistributionJob.setInputFormatClass(TextInputFormat.class);
		citationCountDistributionJob.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(citationCountDistributionJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(citationCountDistributionJob, new Path(args[1]));
		
		citationCountDistributionJob.waitForCompletion(true);
		System.out.println("finished!");
	}
}
