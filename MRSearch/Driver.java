
import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.io.LongWritable; 
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser; 
  
public class Driver { 
	
	// Usage: java -jar Top_N.jar WORD [INPUT FILES] OUTPUT_DIRECTORY
  
    public static void main(String[] args) throws Exception 
    { 
        Configuration conf = new Configuration(); 
  
        /* here we set our own custom parameter  myValue with  
         * default value 10. We will overwrite this value in CLI 
         * at runtime. 
         * Remember that both parameters are Strings and we  
         * have convert them to numeric values when required. 
         */
  
        String[] otherArgs = new GenericOptionsParser(conf, 
                                  args).getRemainingArgs(); 
  
        // if less than two paths provided will show error 
        if (otherArgs.length < 2)  
        { 
            System.err.println("Error: please provide two paths"); 
            System.exit(2); 
        }
        
        // The first element of 'other args' is N
        conf.set("Word", otherArgs[0]); 
  
        Job job = Job.getInstance(conf, "search program"); 
        job.setJarByClass(Driver.class); 
  
        job.setMapperClass(MRSearch_Mapper.class); 
        job.setReducerClass(MRSearch_Reducer.class); 
  
        job.setMapOutputKeyClass(Text.class); 
        job.setMapOutputValueClass(Text.class);
        
        job.setNumReduceTasks(1);
  
        job.setOutputKeyClass(Text.class); 
        job.setOutputValueClass(Text.class);  
  
        job.setOutputKeyClass(LongWritable.class); 
        job.setOutputValueClass(Text.class);  
        
        
		for(int i = 1; i < otherArgs.length - 1; i++){
			FileInputFormat.addInputPath(job, new Path(otherArgs[i])); 
		}
  
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1])); 
  
        System.exit(job.waitForCompletion(true) ? 0 : 1); 
    } 
}