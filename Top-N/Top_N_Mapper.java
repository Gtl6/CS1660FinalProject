import java.io.*; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.io.LongWritable; 
import org.apache.hadoop.mapreduce.Mapper; 
  
public class Top_N_Mapper extends Mapper<Object, 
                              Text, LongWritable, Text> { 
  
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
        String part2 = tokens[1];
        String[] fnAndCounts = part2.split(",");
        long word_count = 0;
        
        // Have to extract the weird data format that I decided to use
        for(String fnAndCount : fnAndCounts) {
        	// Just ignore it if it's empty, which it might be at the end of the line
        	if(fnAndCount.equals("")) continue;
        	// Please don't ask why I decided to do it like this, it was a stupid decision
        	String[] dataz = fnAndCount.split("    :    ");

        	//If there's less than two things in it then we're screwed, otherwise we might be okay
        	if(dataz.length < 2) {
        		System.out.println("For word: "  + word + " got");
        		System.out.println(dataz.length);
        		continue;
        	} 
        	
        	long myNum = Long.parseLong(dataz[1]);
        	word_count += myNum;
        }  
        
        
        word_count *= -1;
        context.write(new LongWritable(word_count), 
                              new Text(word)); 
    } 
}