import java.io.IOException;

import org.apache.hadoop.fs.Path;
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
 * 构建专利引用列表，输入专利引用关系的关系对(patentNo1,patentNo2)
 * 文件，输出每个专利号的所引用的文件，以逗号相隔。
 * @author KING
 *
 */
public class PatentCitation {
	public static class PatentCitationMapper extends Mapper<LongWritable,Text,Text,Text>{
		/**
		 * 输入键位行偏移，值为“专利号1,专利号2”
		 */
		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] citation = value.toString().split(",");
			context.write(new Text(citation[1]), new Text(citation[0]));
		} 
	}
	
	public static class PatentCitationReducer extends Reducer<Text,Text,Text,Text>{
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			StringBuilder csv = new StringBuilder("");
			for (Text val:values) {
				if (csv.length() > 0) {
				csv.append(",");
				}
				csv.append(val.toString());
			}
			context.write(key, new Text(csv.toString()));
		 } 
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
		Job patentCitationJob = new Job();
		patentCitationJob.setJobName("patentCitationJob");
		patentCitationJob.setJarByClass(PatentCitation.class);
		
		patentCitationJob.setMapperClass(PatentCitationMapper.class);
		patentCitationJob.setMapOutputKeyClass(Text.class);
		patentCitationJob.setMapOutputValueClass(Text.class);

		patentCitationJob.setReducerClass(PatentCitationReducer.class);
		patentCitationJob.setOutputKeyClass(Text.class);
		patentCitationJob.setOutputValueClass(Text.class);

		patentCitationJob.setInputFormatClass(TextInputFormat.class);
		patentCitationJob.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(patentCitationJob, new Path(args[0]));
		FileOutputFormat.setOutputPath(patentCitationJob, new Path(args[1]));
		
		patentCitationJob.waitForCompletion(true);
		System.out.println("finished!");
	}
}
