package org.bbssh.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import net.rim.device.api.io.LineReader;

/**
 * This simple wrapper around LineReader processes text files in the same way, 
 * but excludes any blank and comment lines from the returned records. 
 * 
 */
public class ConfigLineReader extends LineReader {

	/**
	 * Constructor
	 * 
	 * @param stream input stream to read from.
	 */
	public ConfigLineReader(InputStream stream) {
		super(stream);
	}

	/**
	 * Reads lines from the input stream until it encounters EOF, or a valid line that is not a comment or blank (after
	 * trimming)
	 * 
	 * @return next line in the stream, not trimmed
	 *
	 * @throws IOException if file can't be read
	 * @throws EOFException when end of file is reached 
	 */
	public String readNextLine() throws IOException, EOFException {
		while (true) {
			String s = new String(readLine());
			String s2 = s.trim();

			if (s2.length() == 0 || s2.startsWith("#")) {
				continue;
			}
			return s;

		}

	}

}
