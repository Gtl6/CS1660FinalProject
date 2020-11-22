import java.io.*; 
import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
  
public class MRSearch_Mapper extends Mapper<Object, 
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
	
    // Data format:
	// Each line starts with a word
	// After a tab, the second part is a list of filenames and counts
    @Override
    public void map(Object key, Text value,  
       Context context) throws IOException,  
                      InterruptedException 
    { 
  
        String[] tokens = value.toString().split("\t");
  
        String word = tokens[0];
        String backHalf = tokens[1];
        
        if(word.equals(myWord)) {
        	context.write(new Text(word), 
                                  new Text(backHalf)); 
        }
    } 
}