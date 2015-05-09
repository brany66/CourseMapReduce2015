import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * 按照特定属性统计，例如按照年份统计每一年专利数目
 * 或者按照国家统计每个国家的专利数目
 * @author KING
 *
 */
public class CitationBy {
	public static class MapClass extends Mapper<LongWritable, Text, Text, IntWritable> {
		private IntWritable one = new IntWritable(1);
		private int colNo;//属性的列号，决定按照哪个属性值进行统计，年份1，国家4。
		
		@Override
        protected void setup(Context context) throws IOException,InterruptedException{
			colNo = context.getConfiguration().getInt("col", 1);//默认按照年份统计
		}
		
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] cols = value.toString().split(",");  // value：读入的一行专利描述数据记录 
	    	String col_data = cols[colNo];
			context.write (new Text(col_data), one);
	     }

	}
	
	public static class ReduceClass extends Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterable<IntWritable> values, Context context) 
				throws IOException, InterruptedException {	
			int count = 0;
			for(IntWritable val : values){
				count += val.get();
			}
			// 输出key: 年份或国家；value: 总的专利数
			context.write(key, new IntWritable(count));
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
		Job citationByJob = new Job();
		citationByJob.setJobName("citationByJob");
		citationByJob.setJarByClass(CitationBy.class);
		
		citationByJob.setMapperClass(MapClass.class);
		citationByJob.setMapOutputKeyClass(Text.class);
		citationByJob.setMapOutputValueClass(IntWritable.class);

		citationByJob.setReducerClass(ReduceClass.class);
		citationByJob.setOutputKeyClass(Text.class);
		citationByJob.setOutputValueClass(IntWritable.class);

		citationByJob.setInputFormatClass(TextInputFormat.class);
		citationByJob.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(citationByJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(citationByJob, new Path(args[1]));
		citationByJob.getConfiguration().setInt("col", Integer.parseInt(args[2]));
		
		citationByJob.waitForCompletion(true);
		System.out.println("finished!");
	}
}
