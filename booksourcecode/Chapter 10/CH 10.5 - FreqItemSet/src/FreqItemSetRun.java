
import java.util.Scanner;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class FreqItemSetRun
{
	public static class MiningMapper
		extends Mapper<Object, Text, Text, IntWritable>
	{
		private final static IntWritable one = new IntWritable(1);
		private Text word;
		
		public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException 
		{
			Configuration conf = context.getConfiguration();
			Scanner scan = new Scanner(value.toString());
			String vals[];
			String item1, item2;
			List<String> cur_Items = new ArrayList<String>();
			List<List<String>> subsets;
			List<String> subset;
			
			int i,j;
			int k,s;
			FindSubset fs;
			
			word = new Text();
			k = Integer.parseInt(conf.get("cur_k"));
			while(scan.hasNextLine() == true)
			{
				vals = scan.nextLine().split(",");
				for(i = 0; i<vals.length; i++)
					cur_Items.add(vals[i]);
				fs = new FindSubset(cur_Items);
				fs.execute(k);
				subsets = fs.getSubsets();
				if(subsets == null)
					continue;
				for(i = 0; i<subsets.size(); i++)
				{
					subset = subsets.get(i);
					vals = new String[subset.size()];
					subset.toArray(vals);
					Arrays.sort(vals);
					for(j = 1; j<vals.length; j++)
					{
						vals[0] += "," + vals[j];
					}
				
					word.set(vals[0]);
					context.write(word, one);
				}
				
				subsets.clear();
			}
			cur_Items.clear();
			fs.clearSubsets();	
					
		}
				
	}
		
	
	public static class MiningCombiner
		extends Reducer<Text,IntWritable,Text,IntWritable>
	{
		private IntWritable result = new IntWritable();
		public void reduce(Text key, Iterable<IntWritable> values, 
        	Context context) throws IOException, InterruptedException 
		{
			int sum = 0;
			for (IntWritable val : values) 
			{
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
        }
	}
	
	public static class MiningReducer
		extends Reducer<Text,IntWritable,Text,IntWritable>
	{
		private IntWritable result = new IntWritable();
		
		public void reduce(Text key, Iterable<IntWritable> values, 
        	Context context) throws IOException, InterruptedException 
		{
			Configuration conf = context.getConfiguration();
			int support = Integer.parseInt(conf.get("support"));		
			int sum = 0;
			for (IntWritable val : values)
			{
				sum += val.get();
			}
			if(support < sum)
			{
				result.set(sum);
				context.write(key, result);
			}
        }
	}
}

