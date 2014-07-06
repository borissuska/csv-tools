package sk.suska.csv_tools.ui;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import sk.suska.csv_tools.tools.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class FormController implements Initializable {

	public static final String AUTODETECT = "- autodetect -";
	public static final String WITHOUT_CHANGE = "- without change -";
	public static final String SAME_AS_INPUT = "- same as input -";
	public static final String CUSTOM = "custom";
	public static final String TAB = "TAB";


	@FXML protected Button fileSearch;
	@FXML protected TextField filePath;
	@FXML protected ComboBox<String> inputEncoding;
	@FXML protected ComboBox<String> outputEncoding;
	@FXML protected TextField lines;
	@FXML protected TextField fileMask;
	@FXML protected Button splitButton;
	@FXML protected CheckBox splitCheck;
	@FXML protected Label linesLabel;
	@FXML protected Label fileMaskLabel;
	@FXML protected ComboBox<String> inputEscaping;
	@FXML protected ComboBox<String> outputEscaping;
	@FXML protected ComboBox<String> inputSeparator;
	@FXML protected ComboBox<String> outputSeparator;
	@FXML protected CheckBox repeatHeaderCheck;
	@FXML protected GridPane outputEscapingPane;
	@FXML protected TextField customInputEscaping;
	@FXML protected TextField customOutputEscaping;
	@FXML protected TextField customInputSeparator;
	@FXML protected GridPane outputSeparatorPane;
	@FXML protected TextField customOutputSeparator;
	@FXML protected Label fileNotExistsLabel;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.filePath.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				File f = new File(newValue);
				boolean fileExists = f.exists() && f.isFile();
				if (fileExists) {
					fileMask.setText("parts" + File.separator + f.getName().replace(".", "_{{index}}."));
				} else if (newValue.length() == 0) {
					fileMask.setText("");
				}
				fileNotExistsLabel.setVisible(newValue.length() > 0 && !fileExists);
			}
		});

		// initialize combo boxes
		List<String> items = new ArrayList<>(Charset.availableCharsets().keySet());
		for (Iterator<String> it = items.iterator(); it.hasNext();) {
			String value = it.next();
			if (value.startsWith("x-") || value.startsWith("X-")) {
				it.remove();
			}
		}
		Collections.reverse(items);

		ObservableList<String> inputEncoding = FXCollections.observableList(items);
		ObservableList<String> outputEncoding = FXCollections.observableList(new ArrayList<>(items));

		inputEncoding.add(0, WITHOUT_CHANGE);
		inputEncoding.add(1, AUTODETECT);
		outputEncoding.add(0, SAME_AS_INPUT);

		this.inputEncoding.setItems(inputEncoding);
		this.inputEncoding.setValue(WITHOUT_CHANGE);
		this.outputEncoding.setItems(outputEncoding);
		this.outputEncoding.setValue(SAME_AS_INPUT);

		this.inputEscaping.getItems().add(0, WITHOUT_CHANGE);
		this.inputEscaping.getItems().add(CUSTOM);
		this.inputEscaping.setValue(WITHOUT_CHANGE);
		this.outputEscaping.getItems().add(0, SAME_AS_INPUT);
		this.outputEscaping.getItems().add(CUSTOM);
		this.outputEscaping.setValue(SAME_AS_INPUT);

		this.inputSeparator.getItems().add(0, WITHOUT_CHANGE);
		this.inputSeparator.getItems().add(TAB);
		this.inputSeparator.getItems().add(CUSTOM);
		this.inputSeparator.setValue(WITHOUT_CHANGE);
		this.outputSeparator.getItems().add(0, SAME_AS_INPUT);
		this.outputSeparator.getItems().add(TAB);
		this.outputSeparator.getItems().add(CUSTOM);
		this.outputSeparator.setValue(SAME_AS_INPUT);

		// initialize number of lines input
		this.lines.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (!newValue.isEmpty()) {
					try {
						//noinspection ResultOfMethodCallIgnored
						Long.parseLong(newValue);
					} catch (NumberFormatException e) {
						lines.setText(oldValue);
					}
				}
			}
		});
		this.customInputEscaping.textProperty().addListener(new OneCharOnlyListener(this.customInputEscaping));
		this.customOutputEscaping.textProperty().addListener(new OneCharOnlyListener(this.customOutputEscaping));
		this.customInputSeparator.textProperty().addListener(new OneCharOnlyListener(this.customInputSeparator));
		this.customOutputSeparator.textProperty().addListener(new OneCharOnlyListener(this.customOutputSeparator));
	}

	public void handleChooseFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Comma Separated Values (CSV) files", "*.csv"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.*"));
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			this.filePath.setText(file.getAbsolutePath());
		}
	}

	public void handleInputEncodingChange(ActionEvent actionEvent) {
		boolean visible = !WITHOUT_CHANGE.equals(this.inputEncoding.getValue());
		this.outputEncoding.setVisible(visible);
	}

	public void handleSplit(ActionEvent actionEvent) {
		String inputEncodingValue = this.inputEncoding.getValue();
		File inputFile = new File(this.filePath.getText());

		if (!inputFile.exists()) {
			Dialogs.create()
					.title("Choose input file")
					.masthead("Choose input file first")
					.message("File does not exist, please choose an existing file.")
					.showError();

			return;
		}


		// autodetect charset
		if (AUTODETECT.equals(inputEncodingValue)) {
			// auto-detect input encoding
			InputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream(inputFile));
				CharsetDetector detector = new CharsetDetector();
				detector.setText(is);
				CharsetMatch charset = detector.detect();
				if (charset.getConfidence() > 50) {
					inputEncodingValue = charset.getName();
				} else {
					Dialogs.create()
							.title("Charset was not recognized")
							.masthead("Unable recognize charset")
							.message("Charset was not recognized, please choose a charset by yourself.")
							.showError();
					return;
				}
			} catch (IOException e) {
				Dialogs.create()
						.title("Unable read file")
						.masthead("Unable read file to detect charset")
						.message("Close all programs which can works with the file and try again.")
						.showException(e);

				return;
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

		// copy CSV
		try {
			int inputEscapingChar = this.getInputEscapingChar(),
				outputEscapingChar = this.getOutputEscapingChar(),
				inputSeparatorChar = this.getInputSeparatorChar(),
				outputSeparatorChar = this.getOutputSeparatorChar();
			File tempFile = null;
			ToolFunction tool = new WriteToFile(!SAME_AS_INPUT.equals(this.outputEncoding.getValue()) ? this.outputEncoding.getValue() : null);
			Map<Integer, Integer> replaceMap = new HashMap<>();

			if (this.outputEscaping.isVisible() && !WITHOUT_CHANGE.equals(this.inputEscaping.getValue()) && !SAME_AS_INPUT.equals(this.outputEscaping.getValue()) && inputEscapingChar != outputEscapingChar) {
				// replace escaping characters
				replaceMap.put(inputEscapingChar, outputEscapingChar);
			}
			if (this.outputSeparator.isVisible() && !WITHOUT_CHANGE.equals(this.inputSeparator.getValue()) && !SAME_AS_INPUT.equals(this.outputSeparator.getValue()) && inputSeparatorChar != outputSeparatorChar) {
				// replace separator characters
				replaceMap.put(inputSeparatorChar, outputSeparatorChar);
			}
			if (replaceMap.size() > 0) {
				if (inputEscapingChar <= 0 || inputSeparatorChar <= 0) {
					Dialogs.create()
							.title("Escaping and Separator characters are required")
							.masthead("Escaping and Separator characters are required")
							.message("Input Escaping and Separator characters are required for replacing characters in file.")
							.showError();
					return;
				}
				// decorate tool by ReplaceChars function
				tool = new ReplaceChars(tool, replaceMap, inputEscapingChar, inputSeparatorChar);
			}

			if (this.splitCheck.isSelected()) {
				if (inputEscapingChar <= 0) {
					Dialogs.create()
							.title("Escaping character is required")
							.masthead("Escaping character is required")
							.message("Escaping character is required when splitting the file.")
							.showError();
					return;
				}

				if (this.repeatHeaderCheck.isSelected()) {
					// decorate tool by CopyHeader function
					tool = new CopyHeader(tool);
				}

				// decorate tool by Split function
				File inputParentDir = inputFile.getParentFile();
				String outputPathMask;
				if (new File(this.fileMask.getText()).isAbsolute()) {
					outputPathMask = this.fileMask.getText();
				} else {
					outputPathMask = inputParentDir.getAbsolutePath() + File.separator + this.fileMask.getText();
				}
				// fix the file mask path
				outputPathMask = outputPathMask.replaceAll("[/\\\\]", "\\".equals(File.separator) ? "\\\\" : File.separator);

				tool = new Split(tool, inputEscapingChar, Long.parseLong(this.lines.getText()), this.repeatHeaderCheck.isSelected(), outputPathMask);
			} else {
				tempFile = File.createTempFile("csv_tools_", ".csv");
				tool.newFileRequest(tempFile.getAbsolutePath());
			}


			CopyCsvTool copyCsv = new CopyCsvTool(tool, this.filePath.getText(), !WITHOUT_CHANGE.equals(inputEncodingValue) ? inputEncodingValue : null);
			copyCsv.copy();

			// replace the original file
			if (tempFile != null) {
				Path originalFilePath = new File(this.filePath.getText()).toPath();
				Files.deleteIfExists(originalFilePath);
				Files.move(tempFile.toPath(), originalFilePath);
			}

			Dialogs.create()
					.title("File converted")
					.masthead("File was successfully converted")
					.message("Your file was successfully converted by your settings.")
					.showInformation();
		} catch (IOException e) {
			Dialogs.create()
					.title("Unable convert file")
					.masthead("Unable convert file")
					.message("Unable convert file due to some IO error.")
					.showException(e);
		}

	}

	public int getInputEscapingChar() {
		String value = this.inputEscaping.getValue();
		return !WITHOUT_CHANGE.equals(value) && value.length() > 0 ? value.charAt(0) : -1;
	}

	public int getOutputEscapingChar() {
		String value = this.outputEscaping.getValue();
		return !SAME_AS_INPUT.equals(value) && value.length() > 0 ? value.charAt(0) : -1;
	}

	public int getInputSeparatorChar() {
		String value = this.inputSeparator.getValue();
		if (TAB.equals(value)) {
			value = "\t";
		}
		return !WITHOUT_CHANGE.equals(value) && value.length() > 0 ? value.charAt(0) : -1;
	}

	public int getOutputSeparatorChar() {
		String value = this.outputSeparator.getValue();
		if (TAB.equals(value)) {
			value = "\t";
		}
		return !SAME_AS_INPUT.equals(value) && value.length() > 0 ? value.charAt(0) : -1;
	}

	public void handleSplitCheckChecked(ActionEvent actionEvent) {
		boolean value = this.splitCheck.isSelected();
		this.lines.setVisible(value);
		this.linesLabel.setVisible(value);
		this.fileMask.setVisible(value);
		this.fileMaskLabel.setVisible(value);
		this.repeatHeaderCheck.setVisible(value);
	}

	public void handleInputSeparatorChange(ActionEvent actionEvent) {
		boolean value = !WITHOUT_CHANGE.equals(inputSeparator.getValue());
		outputSeparatorPane.setVisible(value);

		boolean customInputVisible = CUSTOM.equals(inputSeparator.getValue());
		customInputSeparator.setVisible(customInputVisible);
	}

	public void handleOutputSeparatorChange(ActionEvent actionEvent) {
		boolean visible = CUSTOM.equals(outputSeparator.getValue());
		customOutputSeparator.setVisible(visible);
	}

	public void handleInputEscapingChange(ActionEvent actionEvent) {
		boolean value = !WITHOUT_CHANGE.equals(inputEscaping.getValue());
		outputEscapingPane.setVisible(value);

		boolean customInputVisible = CUSTOM.equals(inputEscaping.getValue());
		customInputEscaping.setVisible(customInputVisible);
	}

	public void handleOutputEscapingChange(ActionEvent actionEvent) {
		boolean visible = CUSTOM.equals(outputEscaping.getValue());
		customOutputEscaping.setVisible(visible);
	}

	private class OneCharOnlyListener implements ChangeListener<String>
	{
		private TextField field;

		private OneCharOnlyListener(TextField field) {
			this.field = field;
		}

		@Override
		public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
			if (newValue.length() > 0 && !newValue.equals(oldValue)) {
				this.field.setText(String.valueOf(newValue.charAt(newValue.length()-1)));
			}
		}
	}
}
