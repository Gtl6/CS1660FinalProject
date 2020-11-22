import java.io.IOException;
 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Reducer; 
  
public class Inverted_Index_Reducer extends Reducer<Text, 
                     Text, Text, Text> { 
    @Override
    public void setup(Context context) throws IOException, 
                                     InterruptedException 
    { 
    } 
  
    @Override
    public void reduce(Text key, Iterable<Text> values, 
      Context context) throws IOException, InterruptedException 
    { 
        // So we have our word and our list of NumFilenameCombiners
    	String outputString = "";
    	
    	// For each word we know we'll have at least one document ping so let's print that out
    	for(Text nfcs: values) {
    		// We did a to_string to get the value before so we can just turn it to a string and add it on here
    		outputString += nfcs.toString();
    	}
    	
    	context.write(key, new Text(outputString));
    } 
  
    @Override
    public void cleanup(Context context) throws IOException, 
                                       InterruptedException 
    { 
    		// Don't need to do anything
    } 
} 