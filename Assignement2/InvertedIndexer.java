﻿import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexer {
  /** 自定义FileInputFormat **/
  public static class FileNameInputFormat extends FileInputFormat<Text, Text> {
    @Override
    public RecordReader<Text, Text> createRecordReader(InputSplit split,
        TaskAttemptContext context) throws IOException, InterruptedException {
      FileNameRecordReader fnrr = new FileNameRecordReader();
        fnrr.initialize(split, context);
      return fnrr;
    }
  }

  /** 自定义RecordReader **/
  public static class FileNameRecordReader extends RecordReader<Text, Text> {
    String fileName;
    LineRecordReader lrr = new LineRecordReader();

    @Override
    public Text getCurrentKey() throws IOException, InterruptedException {
      return new Text(fileName);
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
      return lrr.getCurrentValue();
    }

    @Override
    public void initialize(InputSplit arg0, TaskAttemptContext arg1)
        throws IOException, InterruptedException {
      lrr.initialize(arg0, arg1);
      fileName = ((FileSplit) arg0).getPath().getName();
    }

    public void close() throws IOException {
        lrr.close();
    }

    public boolean nextKeyValue() throws IOException, InterruptedException {
      return lrr.nextKeyValue();
    }

    public float getProgress() throws IOException, InterruptedException {
      return lrr.getProgress();
    }
  }


  public static class InvertedIndexMapper extends
      Mapper<Text, Text, Text, IntWritable> {
	  private String pattern = ".txt.segmented" +"|" + ".TXT.segmented" + "|" + " ";
    protected void map(Text key, Text value, Context context)
        throws IOException, InterruptedException {
      String temp = new String();
      String line = value.toString();
      Text newKey = new Text(key.toString().replaceAll(pattern, ""));
      StringTokenizer itr = new StringTokenizer(line);
      for (; itr.hasMoreTokens();) {
        temp = itr.nextToken();
        Text word = new Text();
        word.set(temp + "#" + newKey);
        context.write(word, new IntWritable(1));
      }
    }
  }
  public static class SumCombiner extends
      Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static class NewPartitioner extends HashPartitioner<Text, IntWritable> {
    public int getPartition(Text key, IntWritable value, int numReduceTasks) {
      String term = new String();
      term = key.toString().split("#")[0];
      return super.getPartition(new Text(term), value, numReduceTasks);
    }
  }

  public static class InvertedIndexReducer extends
      Reducer<Text, IntWritable, Text, Text> {
    private Text word1 = new Text();
    private Text word2 = new Text();
    String temp = new String();
    static Text CurrentItem = new Text(" ");
    private String pattern = "<" +"|" + ">";
    static List<String> postingList = new ArrayList<String>();

    public void reduce(Text key, Iterable<IntWritable> values, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      word1.set(key.toString().split("#")[0]);
      temp = key.toString().split("#")[1];
      for (IntWritable val : values) {
        sum += val.get();
      }
      word2.set("<" + temp + ":" + sum + ">");
      if (!CurrentItem.equals(word1) && !CurrentItem.equals(" ")) {
        StringBuilder out = new StringBuilder();
        long count = 0;
        long count2 = 0;
        for (String p : postingList) {
          out.append(p);
          out.append(";");
          count2 += 1;
          count =
              count
                  + Long.parseLong(p.substring(p.indexOf(":") + 1,
                      p.indexOf(">")));
        }
        double result = (count / (count2 * 1.0));
        
        String average = String.valueOf(result) + "," + out.toString().replaceAll(pattern, "");
        if (count > 0)
          context.write(CurrentItem, new Text(average));
        postingList = new ArrayList<String>();
      }
      CurrentItem = new Text(word1);
      postingList.add(word2.toString()); 
    }


    public void cleanup(Context context) throws IOException,
        InterruptedException {
      StringBuilder out = new StringBuilder();
      long count = 0;
      long count2 = 0;
      for (String p : postingList) {
        out.append(p);
        out.append("; ");
        count2 += 1;
        count =
            count
                + Long
                    .parseLong(p.substring(p.indexOf(":") + 1, p.indexOf(">")));
      }
      double result = (count / (count2 * 1.0));
      String average = String.valueOf(result) + "," + out.toString().replaceAll(pattern, "");
      if (count > 0)
        context.write(CurrentItem, new Text(average));
    }

  }

  public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration();
      Job job = new Job(conf, "inverted index");
      job.setJarByClass(InvertedIndexer.class);
      job.setInputFormatClass(FileNameInputFormat.class);
      job.setMapperClass(InvertedIndexMapper.class);
      job.setCombinerClass(SumCombiner.class);
      job.setReducerClass(InvertedIndexReducer.class);
      job.setPartitionerClass(NewPartitioner.class);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(IntWritable.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(Text.class);
      FileInputFormat.addInputPath(job, new Path(args[0]));
      FileOutputFormat.setOutputPath(job, new Path(args[1]));
      System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
