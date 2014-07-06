package sk.suska.csv_tools.tools;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by suska on 7/5/2014.
 */
public class CopyCsvTool {

	private final ToolFunction function;

	private final String inputFile;

	private final String inputFileEncoding;

	public CopyCsvTool(ToolFunction function, String inputFile, String inputFileEncoding) {
		this.function = function;
		this.inputFile = inputFile;
		this.inputFileEncoding = inputFileEncoding;
	}

	public void copy() throws IOException {
		InputStream inStream = new FileInputStream(this.inputFile);
		Reader in = null;
		if (this.inputFileEncoding != null) {
			in = new InputStreamReader(inStream, Charset.forName(this.inputFileEncoding));
		}
		int ch;
		try {
			while ((ch = in != null ? in.read() : inStream.read()) != -1) {
				this.function.copy(ch);
			}
		} finally {
			this.function.close();
			if (in != null) {
				in.close();
			} else {
				inStream.close();
			}
		}
	}

}
