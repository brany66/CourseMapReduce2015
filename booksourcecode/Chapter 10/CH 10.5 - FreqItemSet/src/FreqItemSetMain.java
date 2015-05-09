
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class FreqItemSetMain
{
	public static void main(String[] args) throws Exception
	{
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		int k,i;
		FileSystem fs = FileSystem.get(conf);
		Path path_in, path_out;
		
		if(otherArgs.length != 5)
		{
			System.err.println("Usage: FreqItemSetMain <dfs_path> <input> <k> <spt_dg> <output>");
			System.exit(2);
		}
		put2HDFS(otherArgs[1], otherArgs[0] + "/" + otherArgs[1], conf);
		
		conf.set("support", otherArgs[3]);
		k = Integer.parseInt(otherArgs[2]);
		conf.set("cur_k", Integer.toString(k));
		{
		Job job = new Job(conf, "mining " + k + " item sets");
		job.setJarByClass(FreqItemSetMain.class);
		job.setMapperClass(FreqItemSetRun.MiningMapper.class);
		job.setCombinerClass(FreqItemSetRun.MiningCombiner.class);
		job.setReducerClass(FreqItemSetRun.MiningReducer.class);
		job.setInputFormatClass(NLineInputFormat.class);
		NLineInputFormat.setNumLinesPerSplit(job, 1);
		job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(IntWritable.class);
		
		path_in = new Path(otherArgs[0] + "/" + otherArgs[1]);
		NLineInputFormat.setInputPaths(job, path_in);
		path_out = new Path(otherArgs[0] + "/" + otherArgs[4]);
		if(fs.exists(path_out))
    		fs.delete(path_out, true);
		FileOutputFormat.setOutputPath(job, path_out);
		if(job.waitForCompletion(true)==false)
			System.exit(1);
		}
			
		getFromHDFS(otherArgs[0] + "/" + otherArgs[4], ".", conf);
    	
    	fs.close();
    	System.exit(0);
	}
	
	public static void put2HDFS(String src, String dst, Configuration conf) throws Exception
	{
		Path dstPath = new Path(dst);
		FileSystem hdfs = dstPath.getFileSystem(conf);
		
		hdfs.copyFromLocalFile(false, true, new Path(src), new Path(dst));
		
	}
	
	public static void getFromHDFS(String src, String dst, Configuration conf) throws Exception
	{
		Path dstPath = new Path(dst);
		FileSystem lfs = dstPath.getFileSystem(conf);
		String temp[] = src.split("/");
		Path ptemp = new Path(temp[temp.length-1]);
		if(lfs.exists(ptemp));
			lfs.delete(ptemp, true);
		lfs.copyToLocalFile(true, new Path(src), dstPath);
		
	}
	
}
