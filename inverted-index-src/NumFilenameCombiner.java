public class NumFilenameCombiner {
	Long occurences;
	String filename;
	
	public NumFilenameCombiner() {};
	public NumFilenameCombiner(long iio, String iif) {
		filename = iif;
		occurences = iio;
	}
	
	public Long getOccurences() { return occurences; }
	
	public String getFilename() { return filename; }
	
	public Long addOneOccurence() {
		occurences++;
		return occurences;
	}
	
	public String toString() {
		return filename + "    :    " + occurences.toString() + ", ";
	}
}
