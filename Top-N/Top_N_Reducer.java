import java.io.IOException; 
import org.apache.hadoop.io.LongWritable; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Reducer; 
import org.apache.hadoop.conf.Configuration; 
  
public class Top_N_Reducer extends Reducer<LongWritable, 
                            Text, LongWritable, Text> { 
  
    static int count; 
  
    @Override
    public void setup(Context context) throws IOException, 
                                     InterruptedException 
    { 
  
        Configuration conf = context.getConfiguration(); 
  
        // we will use the value passed in myValue at runtime 
        count = conf.getInt("NValue", 10);
        System.out.println(count);
    } 
  
    @Override
    public void reduce(LongWritable key, Iterable<Text> values, 
     Context context) throws IOException, InterruptedException 
    { 
  
        long no_of_views = -1 * key.get(); 
        String movie_name = null; 
  
        for (Text val : values) { 
            movie_name = val.toString(); 
        } 
  
        // we just write 10 records as output 
        if (!movie_name.equals("") && count > 0) 
        { 
            context.write(new LongWritable(no_of_views), 
                                  new Text(movie_name)); 
            count--;
        } 
    } 
}