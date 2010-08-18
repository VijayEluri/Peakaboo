package peakaboo.common;

public class Version {

	public final static boolean release = false;
	public final static boolean rc = true;
	
	
	public final static int versionNo = 3;
	public final static String buildDate = "2010-08-17";
	public final static String longVersionNo = ( (release) ? "3.0.0" : rc ? "3.0.0 RC4" : "2.99.19");
	public final static String logo = (release) ? "logo" : rc ? "rclogo" : "devlogo";
	public final static String icon = (release) ? "icon" : rc ? "rcicon" : "devicon";
	public final static String title = "Peakaboo" + ((release) ? "" : rc ? " [Release Candidate]" : " [Development Release]");
	
}
