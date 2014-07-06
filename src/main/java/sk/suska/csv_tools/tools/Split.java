package sk.suska.csv_tools.tools;

import java.io.*;

/**
 * Created by suska on 7/5/2014.
 */
public class Split implements ToolFunction {

	protected static final int INITIAL_STATE = 0;
	protected static final int IN_SECTION_STATE = 1;
	protected static final int ESCAPING_OR_END_OF_SECTION_STATE = 2;

	private final ToolFunction decoratedFunction;

	private final int escapingChar;

	private final long linesPerFile;

	private boolean skipHeader;

	private final String fileMask;

	private int state = INITIAL_STATE;

	private long linesRead = 0;

	private int filesCreated = 0;

	public Split(ToolFunction decoratedFunction, int escapingChar, long linesPerFile, boolean skipHeader, String fileMask) throws IOException {
		this.decoratedFunction = decoratedFunction;
		this.escapingChar = escapingChar;
		this.linesPerFile = linesPerFile;
		this.skipHeader = skipHeader;
		this.fileMask = fileMask;
		this.newFileRequest(this.fileMask.replace("{{index}}", String.valueOf(++this.filesCreated)));
	}

	@Override
	public void copy(int character) throws IOException {
		this.decoratedFunction.copy(character);
		switch (this.state) {
			case INITIAL_STATE:
				if (character == 10) {
					this.linesRead++;
				} else if (character == this.escapingChar) {
					this.state = IN_SECTION_STATE;
				}
				break;
			case IN_SECTION_STATE:
				if (character == this.escapingChar) {
					this.state = ESCAPING_OR_END_OF_SECTION_STATE;
				}
				break;
			case ESCAPING_OR_END_OF_SECTION_STATE:
				if (character == this.escapingChar) {
					this.state = IN_SECTION_STATE;
				} else {
					if (character == 10) {
						this.linesRead++;
					}
					this.state = INITIAL_STATE;
				}
		}
		if (this.skipHeader && this.linesRead == 1) {
			this.linesRead = 0;
			this.skipHeader = false;
		}
		if (this.linesRead >= this.linesPerFile) {
			this.newFileRequest(this.fileMask.replace("{{index}}", String.valueOf(++this.filesCreated)));
			this.linesRead = 0;
		}
	}

	@Override
	public void close() throws IOException {
		this.decoratedFunction.close();
	}

	@Override
	public WriterOrStream newFileRequest(String fileName) throws IOException {
		return this.decoratedFunction.newFileRequest(fileName);
	}

}
