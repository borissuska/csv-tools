package sk.suska.csv_tools.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by suska on 7/6/2014.
 */
public class WriterOrStream {

	private final OutputStream outputStream;

	private final Writer writer;

	public WriterOrStream(OutputStream outputStream) {
		this.outputStream = outputStream;
		writer = null;
	}

	public WriterOrStream(Writer writer) {
		this.outputStream = null;
		this.writer = writer;
	}

	public void write(int b) throws IOException {
		if (writer != null) {
			this.writer.write(b);
		} else if (this.outputStream != null) {
			this.outputStream.write(b);
		}
	}

	public void close() throws IOException {
		if (writer != null) {
			this.writer.flush();
			this.writer.close();
		} else if (this.outputStream != null) {
			this.outputStream.flush();
			this.outputStream.close();
		}
	}

}
