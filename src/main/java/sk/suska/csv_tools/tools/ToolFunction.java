package sk.suska.csv_tools.tools;

import java.io.*;

/**
 * Decorator for CSV operating functions
 */
public interface ToolFunction {

	public void copy(int character) throws IOException;

	public void close() throws IOException;

	public WriterOrStream newFileRequest(String fileName) throws IOException;

}
