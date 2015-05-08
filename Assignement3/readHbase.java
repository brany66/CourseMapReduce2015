import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;


public class readHbase
{
	private static Configuration conf = null;
	static {
		conf = HBaseConfiguration.create();
	    conf.set("hbase.zookeeper.quorum", "127.0.0.1");
	}
	public static void scanRows(String tableName, String fileName) throws IOException
	{
		HTable table = new HTable(conf, tableName);
		Scan s = new Scan();
		ResultScanner ss = table.getScanner(s);
		for (Result r : ss)
		{
			for (KeyValue kv : r.raw())
			{
				AppendToFile(fileName, new String(kv.getRow()) + "\t");
				AppendToFile(fileName, new String(kv.getValue()));
				AppendToFile(fileName, "\r\n");
			}
		}
	}
	public static void AppendToFile(String fileName, String str)
	{
		try
		{
			File file = new File(fileName);
			if(!file.exists())
			{
				try {
					file.createNewFile();
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			FileWriter w = new FileWriter(fileName, true);
			w.write(str);
			w.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		try {
			String tableName = args[0];
			String fileName = "/home/hadoop/exp3_4text_out.txt";
			readHbase.scanRows(tableName, fileName);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}