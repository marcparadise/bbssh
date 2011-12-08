package org.bbssh.terminal;

import java.io.DataOutputStream;
import java.io.IOException;

import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

abstract public class VT320Debug extends VT320 {
	public static final byte EVENT_DATA = 0;
	public static final byte EVENT_TERMATTR_SIZE = 1;
	public static final byte EVENT_TERMATTR_FONT = 2;

	// @todo - allow a command to insert a marker and/or a comment into the index file at any point.
	public static final byte EVENT_MARKER = 3;
	public static final byte EVENT_COMMENT = 4;
	public static final byte EVENT_TERMATTR_COLORS = 5;
	public static final byte EVENT_TERMATTR_SCROLLBACK = 7;
	public static final byte EVENT_INIT_DONE = 8;
	public static final byte EVENT_TERMATTR_TYPE = 9; 

	// @todo in the future capture foreground/background/focus gain/loss, backlight on off. will probably want
	// come up with a separate interface for TerminalLogger?

	// StringBuffer buf = new StringBuffer(512);
	DataOutputStream indexLog;
	DataOutputStream dataLog;
	long last = 0;
	short delay;
	long temp;

	public VT320Debug(String name, ConnectionProperties prop) {
		super(name);
		String file = "TERM_" + name;
		indexLog = Tools.openNewOutput(file + ".idx");
		dataLog = Tools.openNewOutput(file + ".txt");
		if (dataLog == null) {
			closeStreams();
			prop.setCaptureEnabled(false);
		}
		// @todo - ultimately we want to move this all into a TerminalSessionLogger that can be attached
		// to any TerminalSession (or detached on demand)
		writeColors(prop.getBackgroundColorIndex(), prop.getForegroundColorIndex());
		writeMarker();
		writeFontChange(prop.getFontSettings());
		writeString(EVENT_TERMATTR_TYPE, prop.getTermType());
		// first param means "utf-8 data outpout" if it's one. Secnod param currently unused.
		writeIntInt(EVENT_INIT_DONE, 0, 0);
		last = System.currentTimeMillis();

	}

	public void setScreenSize(int w, int h, boolean updateRemote) {
		super.setScreenSize(w, h, updateRemote);
		writeLocalResize(w, h);
	}

	public void setScrollbackBufferSize(int scrollbackSize) {
		writeInt(EVENT_TERMATTR_SCROLLBACK, scrollbackSize);
		super.setScrollbackBufferSize(scrollbackSize);
	}

	private void writeIntInt(byte tag, int value1, int value2) {
		if (indexLog == null)
			return;

		try {
			indexLog.writeByte(tag);
			indexLog.writeInt(value1);
			indexLog.writeInt(value2);
		} catch (IOException e) {
			closeStreams();

		}

	}

	public void writeLocalResize(int x, int y) {
		writeIntInt(EVENT_TERMATTR_SIZE, x, y);

	}

	public void writeColors(int bg, int fg) {
		writeIntInt(EVENT_TERMATTR_COLORS, bg, fg);
	}

	public void writeComment(String comment) {
		writeString(EVENT_COMMENT, comment);

	}

	private void writeString(byte tag, String string) {
		if (indexLog == null)
			return;
		try {
			indexLog.write(tag);
			indexLog.writeUTF(string);
		} catch (IOException e) {
			closeStreams();
		}

	}

	public void writeFontChange(FontSettings fs) {
		if (indexLog == null)
			return;
		try {
			indexLog.writeByte(EVENT_TERMATTR_FONT);
			indexLog.writeByte(fs.getFontType());
			indexLog.writeByte(fs.getFontSize());
			indexLog.writeByte(fs.getFontId());
		} catch (IOException e) {
			closeStreams();
		}

	}

	private void writeInt(byte tag, int value) {
		if (indexLog == null)
			return;
		try {
			indexLog.write(tag);
			indexLog.writeInt(value);
		} catch (IOException e) {
			closeStreams();

		}
	}

	private void writeLong(byte tag, long value) {
		if (indexLog == null)
			return;
		try {
			indexLog.write(tag);
			indexLog.writeLong(value);
		} catch (IOException e) {
			closeStreams();

		}
	}

	private void writeMarker() {
		writeLong(EVENT_MARKER, System.currentTimeMillis());

	}

	private void closeStreams() {

		closeStream(dataLog);
		closeStream(indexLog);
		dataLog = null;
		indexLog = null;

	}

	private void closeStream(DataOutputStream os) {
		if (os == null)
			return;
		try {
			os.flush();
		} catch (IOException e) {
		} finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}

	}

	public void putChar(char c, boolean doshowcursor) {
		super.putChar(c, doshowcursor);
		writeData(c);
	}

	int time;

	private void writeData(char c) {
		if (dataLog == null)
			return;

		try {
			long time = System.currentTimeMillis();
			if (indexLog != null) {
				indexLog.writeByte(EVENT_DATA);
				indexLog.writeInt((int) (last - time));
			}
			last = time;
			// for now we need to capture only 1 byte, not the 2-byte UTF-8 character
			// because our terminal does not yet support UTF-8
			dataLog.write(c);

		} catch (IOException e) {
			Logger.fatal("Unexpected ioexception in writeData (idx)", e);
			closeStreams();
		}

	}

	protected void setTermState(int term_state) {
		super.setTermState(term_state);
	//	Logger.debug("TS : " + term_state);
	}

	public void terminate() {
		closeStreams();
		super.terminate();
	}
}
