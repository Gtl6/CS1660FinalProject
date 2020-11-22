import java.io.*; 
import java.util.*; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
  
public class Inverted_Index_Mapper extends Mapper<Object, 
                            Text, Text, Text> { 
  
	HashMap<String, NumFilenameCombiner> myMap;
	String fileName;
	
    @Override
    public void setup(Context context) throws IOException, 
                                     InterruptedException 
    { 
    	// The Hashmap we'll use to store our data
    	myMap = new HashMap<String, NumFilenameCombiner>();
    	
    	// The name of the file
    	fileName = ((FileSplit) context.getInputSplit()).getPath().toString();
    	// Our filenames are going to be gs://final-project-cc-bucket/INPUT_DATA/<fileName> so we can ignore the first 
    	fileName = fileName.substring(40);
    } 
  
    @Override
    public void map(Object key, Text value, 
       Context context) throws IOException,  
                      InterruptedException 
    { 
    	System.out.println("Started Mapping!");
    	// So we're being given one line of one file, which we'll want to strip of all punctuation and spaces
    	String[] words = value.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
    	
    	// Then for each word, add it to our hashmap
    	for(String oneWord: words) {
    		// If it's already in there, increment it
    		if (myMap.containsKey(oneWord)) {
    			myMap.get(oneWord).addOneOccurence();
    		} // Otherwise create it and add it
    		else {
        		NumFilenameCombiner mynfc = new NumFilenameCombiner(1L, fileName);
        		myMap.put(oneWord, mynfc);
    		}
    	}
    	System.out.println("Finished Mapping!");
    } 
  
    @Override
    public void cleanup(Context context) throws IOException, 
                                       InterruptedException 
    { 
        for (HashMap.Entry<String, NumFilenameCombiner> entry : myMap.entrySet())  
        { 
  
            String word = entry.getKey(); 
            NumFilenameCombiner nfc = entry.getValue();
  
            context.write(new Text(word), new Text(nfc.toString())); 
        } 
    } 
} 