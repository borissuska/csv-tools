package sk.suska.csv_tools.tools;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by suska on 7/5/2014.
 */
public class WriteToFile implements ToolFunction {

	private final String outputFileEncoding;

	private WriterOrStream out;

	public WriteToFile(String outputFileEncoding) {
		this.outputFileEncoding = outputFileEncoding;
	}

	@Override
	public void copy(int character) {
		try {
			if (this.out != null) {
				this.out.write(character);
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable write data.", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (this.out != null) {
			this.out.close();
		}
	}

	@Override
	public WriterOrStream newFileRequest(String fileName) throws IOException {
		if (this.out != null) {
			this.close();
		}

		File outputFile = new File(fileName);
		if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
			throw new IOException("Unable create parent directories for file " + outputFile.getAbsolutePath());
		}
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		Writer out = outputFileEncoding != null ? new OutputStreamWriter(outStream, Charset.forName(outputFileEncoding)) : null;

		return this.out = out != null ? new WriterOrStream(out) : new WriterOrStream(outStream);
	}

}
