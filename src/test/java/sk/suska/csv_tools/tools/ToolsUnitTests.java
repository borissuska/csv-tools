package sk.suska.csv_tools.tools;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by suska on 7/5/2014.
 */
public class ToolsUnitTests {

	@Test
	public void testCopyWithoutEncoding() throws IOException {
		ToolFunction tool = new WriteToFile(null);
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testCopyWithoutEncoding.csv");
		File tempFile = File.createTempFile("testCopyWithoutEncoding-", ".csv");

		tool.newFileRequest(tempFile.getAbsolutePath());
		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath()),
				this.getFileStream("sk/suska/csv_tools/tools/testCopyWithoutEncoding.csv")
		));
	}

	@Test
	public void testWin1250ToUTF8Encoding() throws IOException {
		ToolFunction tool = new WriteToFile("UTF-8");
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testWin1250ToUTF8.csv");
		File tempFile = File.createTempFile("testWin1250ToUTF8-", ".csv");

		tool.newFileRequest(tempFile.getAbsolutePath());
		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, "windows-1250");
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath()),
				this.getFileStream("sk/suska/csv_tools/tools/testWin1250ToUTF8-result.csv")
		));
	}

	@Test
	public void testSplitFile() throws IOException {
		File tempFile = File.createTempFile("testSplitFile-", ".csv");
		ToolFunction tool = new Split(new WriteToFile(null), '"', 1, false, tempFile.getAbsolutePath().replace(".csv", "_{{index}}.csv"));
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testSplitFile.csv");

		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_1.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitFile-result1.csv")
		));
		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_2.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitFile-result2.csv")
		));
	}

	@Test
	public void testSplitFileAdv() throws IOException {
		File tempFile = File.createTempFile("testSplitFileAdv-", ".csv");
		ToolFunction tool = new Split(new WriteToFile(null), '"', 1, false, tempFile.getAbsolutePath().replace(".csv", "_{{index}}.csv"));
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testSplitFileAdv.csv");

		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_1.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitFileAdv-result1.csv")
		));
		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_2.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitFileAdv-result2.csv")
		));
	}

	@Test
	public void testCopyHeader() throws IOException {
		File tempFile = File.createTempFile("testCopyHeader-", ".csv");
		ToolFunction tool = new CopyHeader(new WriteToFile(null));
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testCopyHeader.csv");

		tool.newFileRequest(tempFile.getAbsolutePath());
		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath()),
				this.getFileStream("sk/suska/csv_tools/tools/testCopyHeader-result.csv")
		));
	}

	@Test
	public void testSplitCopyHeader() throws IOException {
		File tempFile = File.createTempFile("testSplitCopyHeader-", ".csv");
		ToolFunction tool = new Split(new CopyHeader(new WriteToFile(null)), '"', 1, true, tempFile.getAbsolutePath().replace(".csv", "_{{index}}.csv"));
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testSplitCopyHeader.csv");

		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_1.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitCopyHeader-result1.csv")
		));
		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath().replace(".csv", "_2.csv")),
				this.getFileStream("sk/suska/csv_tools/tools/testSplitCopyHeader-result2.csv")
		));
	}

	@Test
	public void testReplaceChars() throws IOException {
		File tempFile = File.createTempFile("testReplaceChars-", ".csv");
		ToolFunction tool = new ReplaceChars(new WriteToFile(null), null, '"', ',');
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testCopyWithoutEncoding.csv");

		tool.newFileRequest(tempFile.getAbsolutePath());
		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath()),
				this.getFileStream("sk/suska/csv_tools/tools/testCopyWithoutEncoding.csv")
		));
	}

	@Test
	public void testReplaceCharsEscaping() throws IOException {
		File tempFile = File.createTempFile("testReplaceCharsEscaping-", ".csv");
		Map<Integer,Integer> replaceMap = new HashMap<>();
		replaceMap.put((int) '"', (int) '\'');
		replaceMap.put((int) ',', (int) ';');
		ToolFunction tool = new ReplaceChars(new WriteToFile(null), replaceMap, '"', ',');
		String inputFile = this.getFilePath("sk/suska/csv_tools/tools/testReplaceCharsEscaping.csv");

		tool.newFileRequest(tempFile.getAbsolutePath());
		CopyCsvTool copyCsv = new CopyCsvTool(tool, inputFile, null);
		copyCsv.copy();

		Assert.assertTrue(IOUtils.contentEquals(
				new FileInputStream(tempFile.getAbsolutePath()),
				this.getFileStream("sk/suska/csv_tools/tools/testReplaceCharsEscaping-result.csv")
		));
	}


	private String getFilePath(String fileOnCP) {
		URL url = this.getClass().getClassLoader().getResource(fileOnCP);
		return url != null ? url.getPath() : null;
	}

	private InputStream getFileStream(String fileOnCP) {
		return this.getClass().getClassLoader().getResourceAsStream(fileOnCP);
	}

}
