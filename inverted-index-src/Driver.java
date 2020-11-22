import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser; 
  
public class Driver { 
  
    public static void main(String[] args) throws Exception 
    { 
    	System.out.println("Started the program!");
        Configuration conf = new Configuration(); 
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs(); 
  
        // if less than two paths  
        // provided will show error 
        if (otherArgs.length < 2)  
        { 
            System.err.println("Error: please provide at least two paths"); 
            System.exit(2); 
        } 
  
        Job job = Job.getInstance(conf, "Inverted Indexing"); 
        job.setJarByClass(Driver.class); 
        
        job.setNumReduceTasks(1);
  
        job.setMapperClass(Inverted_Index_Mapper.class); 
        job.setReducerClass(Inverted_Index_Reducer.class); 
  
        job.setMapOutputKeyClass(Text.class); 
        job.setMapOutputValueClass(Text.class); 
  
        job.setOutputKeyClass(Text.class); 
        job.setOutputValueClass(Text.class); 
		
		for(int i = 0; i < otherArgs.length - 1; i++){
			FileInputFormat.addInputPath(job, new Path(otherArgs[i])); 
		}
  
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[otherArgs.length - 1])); 
  
        System.out.println("Finished the Driver and running Mapreduce!");
        
        System.exit(job.waitForCompletion(true) ? 0 : 1); 
    } 
}