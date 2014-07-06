package sk.suska.csv_tools.tools;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suska on 7/5/2014.
 */
public class CopyHeader implements ToolFunction {

	private final ToolFunction decoratedFunction;

	private List<Integer> header = new ArrayList<>();

	private boolean headerRead = false;

	public CopyHeader(ToolFunction decoratedFunction) {
		this.decoratedFunction = decoratedFunction;
	}

	@Override
	public void copy(int character) throws IOException {
		if (!this.headerRead) {
			this.header.add(character);
		}
		if (character == 10) {
			this.headerRead = true;
		}
		this.decoratedFunction.copy(character);
	}

	@Override
	public void close() throws IOException {
		this.decoratedFunction.close();
	}

	@Override
	public WriterOrStream newFileRequest(String fileName) throws IOException {
		WriterOrStream out = this.decoratedFunction.newFileRequest(fileName);
		for (Integer character : this.header) {
			out.write(character);
		}
		return out;
	}
}
