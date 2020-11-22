import java.io.IOException; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Reducer; 
import org.apache.hadoop.conf.Configuration; 
  
public class MRSearch_Reducer extends Reducer<Text, 
                            Text, Text, Text> { 
  
    static String myWord; 
  
    @Override
    public void setup(Context context) throws IOException, 
                                     InterruptedException 
    { 
  
        Configuration conf = context.getConfiguration(); 
  
        // we will use the value passed in myValue at runtime 
        myWord = conf.get("Word");
    } 
  
    @Override
    public void reduce(Text key, Iterable<Text> values, 
     Context context) throws IOException, InterruptedException 
    { 
    	// I think there's only gonna be one thing, but let's find out together
    	String resultant = "";
    	for(Text value: values) {
    		resultant += value.toString();
    	}
        
        if(key.toString().equals(myWord)) {
        	context.write(key, new Text(resultant)); 
        }
    } 
}