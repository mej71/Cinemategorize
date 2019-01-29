package application;

import java.io.Serializable;

public class FilePathInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	public String fPath;
	public String fName;
	public String fFolder;
	
	public FilePathInfo(String fp, String fn, String ff) {
		fPath = fp;
		fName = fn;
		fFolder = ff;
	}
}
