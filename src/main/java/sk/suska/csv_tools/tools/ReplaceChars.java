package sk.suska.csv_tools.tools;

import java.io.IOException;
import java.util.*;


/**
 * Created by suska on 7/5/2014.
 */
public class ReplaceChars implements ToolFunction {

	private final ToolFunction decoratedFunction;

	private final Map<Integer, Integer> replace;

	private final int escapingCharacter;

	private final int separatorCharacter;

	private final int newSeparatorCharacter;

	private final int newEscapingCharacter;

	private List<Integer> buffer = new LinkedList<>();

	private boolean escapingNeeded = false;

	public static final int INITIAL_STATE = 0;
	public static final int NON_ESCAPED_CONTENT_STATE = 1;
	public static final int ESCAPED_CONTENT_STATE = 2;
	public static final int ESCAPING_END_OR_DOUBLED_STATE = 3;
	public static final int ESCAPED_CONTENT_END_STATE = 4;

	private int state = INITIAL_STATE;

	public ReplaceChars(ToolFunction decoratedFunction, Map<Integer, Integer> replace, int escapingCharacter, int separatorCharacter) {
		this.decoratedFunction = decoratedFunction;
		this.separatorCharacter = separatorCharacter;
		this.replace = replace != null ? replace : new HashMap<Integer, Integer>();
		Iterator<Map.Entry<Integer, Integer>> it = this.replace.entrySet().iterator();
		// remove entries where same value resolves to itself
		while (it.hasNext()) {
			Map.Entry<Integer, Integer> entry =  it.next();
			if (entry.getKey().equals(entry.getValue())) {
				it.remove();
			}
		}

		this.newSeparatorCharacter = this.replace.containsKey(separatorCharacter) ? this.replace.get(separatorCharacter) : -1;
		this.escapingCharacter = escapingCharacter;
		this.newEscapingCharacter = this.replace.containsKey(escapingCharacter) ? this.replace.get(escapingCharacter) : -1;
	}

	@Override
	public void copy(int character) throws IOException {
		switch(state) {
			case INITIAL_STATE:
				if (character == this.escapingCharacter) {
					this.state = ESCAPED_CONTENT_STATE;
				} else {
					this.state = NON_ESCAPED_CONTENT_STATE;
				}
				break;
			case ESCAPED_CONTENT_STATE:
				if (character == this.newEscapingCharacter) {
					// double escaping character
					this.buffer.add(character);
				} else if (character == this.separatorCharacter) {
					// do not replace separator in escaped content
					this.buffer.add(this.separatorCharacter);
					return;
				} else if (character == this.escapingCharacter) {
					// do not add character
					this.state = ESCAPING_END_OR_DOUBLED_STATE;
					return;
				}
				break;
			case NON_ESCAPED_CONTENT_STATE:
				if (character == this.newEscapingCharacter || character == this.newSeparatorCharacter) {
					if (character == this.newEscapingCharacter) {
						// double new escaping character
						this.buffer.add(character);
					}
					this.escapingNeeded = true;
				} else if (character == this.separatorCharacter || character == '\n') {
					this.flush();
					this.decoratedFunction.copy(this.resolveChar(character));
					return;
				}
				break;
			case ESCAPING_END_OR_DOUBLED_STATE:
				if (character == this.escapingCharacter) {
					// if escaping char will change do not copy doubled old escaping character
					if (this.newEscapingCharacter > 0 && this.escapingCharacter != this.newEscapingCharacter) {
						this.buffer.add(this.escapingCharacter);
						this.state = ESCAPED_CONTENT_STATE;
						return;
					}
				} else if (character == this.separatorCharacter || character == '\n') {
					this.buffer.add(this.resolveChar(this.escapingCharacter));
					this.flush();
					this.decoratedFunction.copy(this.resolveChar(character));
					return;
				} else {
					this.buffer.add(this.resolveChar(this.escapingCharacter));
					this.state = ESCAPED_CONTENT_END_STATE;
				}
				break;
			case ESCAPED_CONTENT_END_STATE:
				if (character == this.separatorCharacter || character == '\n') {
					this.flush();
					this.decoratedFunction.copy(this.resolveChar(character));
					return;
				} else {
					// ignore characters out of escaped field
					return;
				}
		}
		this.buffer.add(this.resolveChar(character));
	}

	private int resolveChar(int character) {
		if (this.replace.containsKey(character)) {
			return this.replace.get(character);
		}
		return character;
	}

	public void flush() throws IOException {
		if (escapingNeeded) {
			this.decoratedFunction.copy(this.resolveChar(this.escapingCharacter));
		}
		for (Integer ch : this.buffer) {
			this.decoratedFunction.copy(ch);
		}
		if (escapingNeeded) {
			this.decoratedFunction.copy(this.resolveChar(this.escapingCharacter));
		}

		this.buffer.clear();
		this.state = INITIAL_STATE;
		this.escapingNeeded = false;
	}

	@Override
	public void close() throws IOException {
		this.flush();
		this.decoratedFunction.close();
	}

	@Override
	public WriterOrStream newFileRequest(String fileName) throws IOException {
		this.flush();
		return this.decoratedFunction.newFileRequest(fileName);
	}
}
