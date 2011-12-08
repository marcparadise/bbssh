/**
 * This file is part of "BBSSH" &copy; 2010 Marc A. Paradise. BBSSH is based upon MidpSSH by Karl von Randow MidpSSH was
 * based upon Telnet Floyd and FloydSSH by Radek Polak. This file Copyright &copy; 2010-2011 Marc A. Paradise, derived
 * from VT320.java &copy; Matthias L. Jugel, Marcus Meissner 1996-2011. All Rights Reserved. Please visit
 * http://javatelnet.org/ for updates and contact. --LICENSE NOTICE-- This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA. --LICENSE NOTICE--
 */
package org.bbssh.terminal;

import java.io.IOException;

import org.bbssh.util.Logger;

/**
 * Implementation of a VT terminal emulation plus ANSI compatible.
 * <P>
 * 
 * @author Matthias L. Jugel, Marcus Meiï¿½ner, Marc A. Paradise
 */
public abstract class VT320 {

	/* Virtual key codes. */
	public static final int VK_ENTER = '\n';
	public static final int VK_BACK_SPACE = '\b';
	public static final char VK_TAB = '\t';
	public static final int VK_CANCEL = 0x03;
	public static final int VK_CLEAR = 0x0C;
	public static final int VK_SHIFT = 0x10;
	public static final int VK_CONTROL = 0x11;
	public static final int VK_ALT = 0x12;
	public static final int VK_PAUSE = 0x13;
	public static final int VK_CAPS_LOCK = 0x14;
	public static final int VK_ESCAPE = 0x1B;
	public static final int VK_SPACE = 0x20;
	public static final int VK_PAGE_UP = 0x21;
	public static final int VK_PAGE_DOWN = 0x22;
	public static final int VK_END = 0x23;
	public static final int VK_HOME = 0x24;
	/**
	 * Constant for the non-numpad <b>left </b> arrow key.
	 */
	public static final int VK_LEFT = 0x25;
	/**
	 * Constant for the non-numpad <b>up </b> arrow key.
	 */
	public static final int VK_UP = 0x26;
	/**
	 * Constant for the non-numpad <b>right </b> arrow key.
	 */
	public static final int VK_RIGHT = 0x27;
	/**
	 * Constant for the non-numpad <b>down </b> arrow key.
	 */
	public static final int VK_DOWN = 0x28;
	public static final int VK_COMMA = 0x2C;
	/**
	 * Constant for the "-" key.
	 */
	public static final int VK_MINUS = 0x2D;
	public static final int VK_PERIOD = 0x2E;
	public static final int VK_SLASH = 0x2F;
	/** VK_0 thru VK_9 are the same as ASCII '0' thru '9' (0x30 - 0x39) */
	public static final int VK_0 = 0x30;
	public static final int VK_1 = 0x31;
	public static final int VK_2 = 0x32;
	public static final int VK_3 = 0x33;
	public static final int VK_4 = 0x34;
	public static final int VK_5 = 0x35;
	public static final int VK_6 = 0x36;
	public static final int VK_7 = 0x37;
	public static final int VK_8 = 0x38;
	public static final int VK_9 = 0x39;
	public static final int VK_SEMICOLON = 0x3B;
	public static final int VK_EQUALS = 0x3D;
	/** VK_A thru VK_Z are the same as ASCII 'A' thru 'Z' (0x41 - 0x5A) */
	public static final int VK_A = 0x41;
	public static final int VK_B = 0x42;
	public static final int VK_C = 0x43;
	public static final int VK_D = 0x44;
	public static final int VK_E = 0x45;
	public static final int VK_F = 0x46;
	public static final int VK_G = 0x47;
	public static final int VK_H = 0x48;
	public static final int VK_I = 0x49;
	public static final int VK_J = 0x4A;
	public static final int VK_K = 0x4B;
	public static final int VK_L = 0x4C;
	public static final int VK_M = 0x4D;
	public static final int VK_N = 0x4E;
	public static final int VK_O = 0x4F;
	public static final int VK_P = 0x50;
	public static final int VK_Q = 0x51;
	public static final int VK_R = 0x52;
	public static final int VK_S = 0x53;
	public static final int VK_T = 0x54;
	public static final int VK_U = 0x55;
	public static final int VK_V = 0x56;
	public static final int VK_W = 0x57;
	public static final int VK_X = 0x58;
	public static final int VK_Y = 0x59;
	public static final int VK_Z = 0x5A;
	public static final int VK_OPEN_BRACKET = 0x5B;
	public static final int VK_BACK_SLASH = 0x5C;
	public static final int VK_CLOSE_BRACKET = 0x5D;
	public static final int VK_NUMPAD0 = 0x60;
	public static final int VK_NUMPAD1 = 0x61;
	public static final int VK_NUMPAD2 = 0x62;
	public static final int VK_NUMPAD3 = 0x63;
	public static final int VK_NUMPAD4 = 0x64;
	public static final int VK_NUMPAD5 = 0x65;
	public static final int VK_NUMPAD6 = 0x66;
	public static final int VK_NUMPAD7 = 0x67;
	public static final int VK_NUMPAD8 = 0x68;
	public static final int VK_NUMPAD9 = 0x69;
	public static final int VK_MULTIPLY = 0x6A;
	public static final int VK_ADD = 0x6B;
	/**
	 * Constant for the Numpad Separator key.
	 */
	public static final int VK_SEPARATOR = 0x6C;
	public static final int VK_SUBTRACT = 0x6D;
	public static final int VK_DECIMAL = 0x6E;
	public static final int VK_DIVIDE = 0x6F;
	public static final int VK_DELETE = 0x7F; /* ASCII DEL */

	public static final int VK_NUM_LOCK = 0x90;
	public static final int VK_SCROLL_LOCK = 0x91;
	/** Constant for the F1 function key. */
	public static final int VK_F1 = 0x70;
	/** Constant for the F2 function key. */
	public static final int VK_F2 = 0x71;
	/** Constant for the F3 function key. */
	public static final int VK_F3 = 0x72;
	/** Constant for the F4 function key. */
	public static final int VK_F4 = 0x73;
	/** Constant for the F5 function key. */
	public static final int VK_F5 = 0x74;
	/** Constant for the F6 function key. */
	public static final int VK_F6 = 0x75;
	/** Constant for the F7 function key. */
	public static final int VK_F7 = 0x76;
	/** Constant for the F8 function key. */
	public static final int VK_F8 = 0x77;
	/** Constant for the F9 function key. */
	public static final int VK_F9 = 0x78;
	/** Constant for the F10 function key. */
	public static final int VK_F10 = 0x79;
	/** Constant for the F11 function key. */
	public static final int VK_F11 = 0x7A;
	/** Constant for the F12 function key. */
	public static final int VK_F12 = 0x7B;
	public static final int VK_INSERT = 0x9B;
	public final static int KEY_CONTROL = 0x00010000;
	public final static int KEY_SHIFT = 0x00020000;
	public final static int KEY_ALT = 0x00040000;
	public final static int KEY_ACTION = 0x00080000;
	private String terminalID = "vt320";
	// Current cursor marker.
	public int ROW, COLUMN;
	int attributes = 0;
	int Sc, Sr, Sa, Stm, Sbm;
	char Sgr, Sgl;
	char Sgx[];
	int insertmode = 0;
	int statusmode = 0;
	boolean vt52mode = false;
	// false - numeric, true - application
	boolean keypadmode = false;
	boolean output8bit = false;
	int normalcursor = 0;
	boolean moveoutsidemargins = true;
	boolean wraparound = true;
	boolean sendcrlf = true;
	boolean capslock = false;
	boolean numlock = false;
	int mouserpt = 0;
	byte mousebut = 0;
	int lastwaslf = 0;
	public int numScrollbackLines = 0;
	boolean usedcharsets = false;

	// Reference: http://en.wikipedia.org/wiki/C0_and_C1_control_codes

	/**
	 * The ESC key on the keyboard will cause this character to be sent on most
	 * systems. It can be used in software user interfaces to exit from a
	 * screen, menu, or mode, or in device-control protocols (e.g., printers and
	 * terminals) to signal that what follows is a special command sequence
	 * rather than normal text. In systems based on ISO/IEC 2022, even if
	 * another set of C0 control codes are used, this octet is required to
	 * always represent the escape character.
	 */
	private final static char ESC = 27;
	/**
	 * Move the active position one line down, to eliminate ambiguity about the
	 * meaning of LF. Deprecated in 1988 and withdrawn in 1992 from ISO/IEC 6429
	 * (1986 and 1991 respectively for ECMA-48).
	 */
	private final static char IND = 132;
	/**
	 * Equivalent to CR+LF. Used to mark end-of-line on some IBM mainframes.
	 */
	private final static char NEL = 133;
	/**
	 * reverse line feed
	 */
	private final static char RI = 141;
	/**
	 * Next character invokes a graphic character from the G2 graphic set. In
	 * systems that conform to ISO/IEC 4873 (ECMA-43), even if a C1 set other
	 * than the default is used, these two octets may only be used for this
	 * purpose.
	 */
	private final static char SS2 = 142;
	/**
	 * Next character invokes a graphic character from the G3 graphic set. In
	 * systems that conform to ISO/IEC 4873 (ECMA-43), even if a C1 set other
	 * than the default is used, these two octets may only be used for this
	 * purpose.
	 */
	private final static char SS3 = 143;
	/**
	 * Followed by a string of printable characters (0x20 through 0x7E) and
	 * format effectors (0x08 through 0x0D), terminated by ST (0x9C).
	 */
	private final static char DCS = 144;
	/**
	 * Causes a character tabulation stop to be set at the active position.
	 */
	private final static char HTS = 136;
	/**
	 * Used to introduce control sequences that take parameters.
	 */
	private final static char CSI = 155;
	/**
	 * Operating System Command - Followed by a string of printable characters
	 * (0x20 through 0x7E) and format effectors (0x08 through 0x0D), terminated
	 * by ST (0x9C). This along with PM and APC were intended for use to allow
	 * in-band signaling of protocol information, but are rarely used for that
	 * purpose.
	 * 
	 * @see VT320.OSC
	 * @see VT320.PM
	 */
	protected final static char OSC = 157;
	/**
	 * Privacy message
	 * 
	 * @see VT320.OSC
	 */
	protected final static char PM = 158;
	/**
	 * Application Program Command
	 * 
	 * @see VT320.OSC
	 */
	protected final static char APC = 159;

	// These are all control code that must be honored EVEN if tehy arrive in
	// the middle of an
	// escape sequence - so we check for them first.

	protected final static char BEL = 007;// bell
	protected final static char BS = 0x08; // backspace
	protected final static char HT = 0x09; // tab
	protected final static char LF = 0x0A; // line feed
	protected final static char VT = 0x0B; // line feed
	protected final static char FF = 0x0C; // line feed
	protected final static char CR = 0x0D;// gives a carriage return;
	protected final static char SO = 0x0E;// activates the G1 character set, and
	// if LF/NL (new line mode) is set
	// also a carriage return;
	protected final static char SI = 0x0F;// activates the G0 character set
	protected final static char CAN = 0x18; // interrupt escape sequence
	protected final static char SUB = 0x1A; // interrupt escape sequence
	// protected final static char ESC = 0x1B; // start an escape sequence (and
	// interrupt escape sequence)
	protected final static char DEL = 0x7F; // ignored
	// protected final static char CSI = 0x9B; // CSI - ESC [

	private final static int TSTATE_DATA = 0;
	// ESC - followed by nothing. not documented in ansi standard...
	private final static int TSTATE_ESC = 1;
	// ESC [ // Control Sequence Intro
	private final static int TSTATE_CSI = 2;
	// ESC P DCS Device Control String
	private final static int TSTATE_DCS = 3;
	// ESC [? -- eh? Not documented in vt100 - later?
	private final static int TSTATE_DCEQ = 4;
	// ESC #
	private final static int TSTATE_ESCSQUARE = 5;
	// ESC ]
	private final static int TSTATE_OSC = 6;
	// ESC (?
	private final static int TSTATE_SETG0 = 7;
	// ESC )? *
	private final static int TSTATE_SETG1 = 8;
	// ESC *?
	private final static int TSTATE_SETG2 = 9;
	// ESC +?
	private final static int TSTATE_SETG3 = 10;
	// ESC [ Pn $
	private final static int TSTATE_CSI_DOLLAR = 11;
	// ESC [ !
	private final static int TSTATE_CSI_EX = 12;
	// ESC <space>
	private final static int TSTATE_ESCSPACE = 13;
	private final static int TSTATE_VT52X = 14;
	private final static int TSTATE_VT52Y = 15;
	private final static int TSTATE_CSI_TICKS = 16;
	/*
	 * The graphics charsets B - default ASCII A - ISO Latin 1 0 - DEC SPECIAL <
	 * - User defined ....
	 */
	char gx[] = {// same initial set as in XTERM.
	'B', // g0
			'0', // g1
			'B', // g2
			'B', // g3
	};
	char gl = 0; // default GL to G0
	char gr = 2; // default GR to G2
	int onegl = -1; // single shift override for GL.
	private String FunctionKey[];
	private String TabKey[];
	private String KeyUp[], KeyDown[], KeyLeft[], KeyRight[];
	private String Insert[];
	private String KeyHome[], KeyEnd[], PrevScn[], NextScn[], Escape[],
			BackSpace[];
	// , NUMPlus[]; // NUMDot[],
	private String osc, dcs; /* to memorize OSC & DCS control sequence */

	/** vt320 state variable (internal) */
	protected int term_state = TSTATE_DATA;
	/** Tabulators */
	private byte[] Tabs;
	/** The list of integers as used by CSI */
	private int[] DCEvars = new int[30];
	private int DCEvar;
	private int writeBufferIndex = 0;

	private byte[] writeBuffer = new byte[256];
	private byte[] stringConversionBuffer = new byte[256];

	public long[][] terminalData;

	// Buffer sizes
	public int bufSize;
	public int maxBufSize;
	/** actual screen start */
	public int screenBase;
	/** where the screen starts displaying - viewport? */
	public int windowBase;

	// Scrolling margins - these indicate the scrolling region WITHIN the
	// screen, in terms of visible rows. scrolling can occur in a region
	// that is smaller than the visible screen.
	/** Top scroll margin */
	private int topMargin;
	/** Bottom scroll margin */
	private int bottomMargin;

	// cursor variable
	// indicates whether or not to show the cursor
	public boolean showcursor = true;
	// visual cursor current xpos
	public int cursorX;
	// visual cursor current xpos
	public int cursorY;
	/** Scroll up when inserting a line. */
	public final static boolean SCROLL_UP = false;
	/** Scroll down when inserting a line. */
	public final static boolean SCROLL_DOWN = true;

	// Note that our XATTR values are stored in the termData as part of the CHAR
	// section - char needs 16 bits, leaving
	// us 16 for extended values.

	/**
	 * Found only int he first column of a row, this attribute menas that the
	 * row needs to be redrawn.
	 */

	/** If found in position 0 of theline, indicates that the line is dirty. */
	public final static long XATTR_DIRTY = 0x00010000;
	/** An automatic wrap occurs at this character */
	public final static long XATTR_WRAP = 0x00020000;

	/** Character is selected */
	public static final int XATTR_SELECTED = 0x10000000;

	/** Make character normal. */
	public final static int NORMAL = 0x00;
	/** Make character bold. */
	public final static int BOLD = 0x01;
	/** Underline character. */
	public final static int UNDERLINE = 0x02;
	/** Invert character. */
	public final static int INVERT = 0x04;
	/** Lower intensity character. */
	public final static int LOW = 0x08;
	/** Character has color **/
	public final static int COLOR = 0xff0;
	/** FG color indicator **/
	public final static int COLOR_FG = 0x0f0;
	public final static int COLOR_BG = 0xf00;
	private Object termBufferMutex = new Object();
	public String debugName;
	public final static int BLINK = 0x1000;
	public static final byte FK_VT100 = 0;
	public static final byte FK_LINUX_APP_KEYPAD = 1;
	public static final byte FK_LINUX = 2;

	// @todo can we replace dual arrays with Character[rows][cols]
	// (attribute,value)?
	/**
	 * Create a new vt320 terminal and intialize it with useful settings.
	 * 
	 * @param width
	 *            terminal width
	 * @param height
	 *            terminal height
	 */
	public VT320(int width, int height) {
		debugName = "";
		setScreenSize(width, height, false);
		setBufferSize(height);
		resetTabs();

		Insert = new String[4];
		KeyHome = new String[4];
		KeyEnd = new String[4];
		NextScn = new String[4];
		PrevScn = new String[4];
		Escape = new String[4];
		BackSpace = new String[4];
		TabKey = new String[4];

		// @note - Array of objects for 80x24
		// 1920 * (4 + 2 + 4 + 4) = 26880 + 12 for array.
		// obj reference + char value + attributes + more attributes
		// Current setup = 1920 * (

		// @todo - if these modes are always equal, do we really need to
		// keep these paralell arrays?
		Insert[0] = Insert[1] = Insert[2] = Insert[3] = "\u001b[2~";
		KeyHome[0] = KeyHome[1] = KeyHome[2] = KeyHome[3] = "\u001b[H";
		KeyEnd[0] = KeyEnd[1] = KeyEnd[2] = KeyEnd[3] = "\u001b[F";
		PrevScn[0] = PrevScn[1] = PrevScn[2] = PrevScn[3] = "\u001b[5~";
		NextScn[0] = NextScn[1] = NextScn[2] = NextScn[3] = "\u001b[6~";
		Escape[0] = Escape[1] = Escape[2] = Escape[3] = "\u001b";
		BackSpace[0] = BackSpace[1] = BackSpace[2] = BackSpace[3] = "\b";
		FunctionKey = new String[21];
		FunctionKey[0] = "";
		/*
		 * Note that this is VT100 compliant - see enableFunctionKeyWorkaround
		 * for option compliant with "linux", "ansi", and several others.
		 */
		FunctionKey[1] = "\u001bOP";
		FunctionKey[2] = "\u001bOQ";
		FunctionKey[3] = "\u001bOR";
		FunctionKey[4] = "\u001bOS";
		/* following are defined differently for vt220 / vt132 ... */
		FunctionKey[5] = "\u001b[15~";
		FunctionKey[6] = "\u001b[17~";
		FunctionKey[7] = "\u001b[18~";
		FunctionKey[8] = "\u001b[19~";
		FunctionKey[9] = "\u001b[20~";
		FunctionKey[10] = "\u001b[21~";
		FunctionKey[11] = "\u001b[23~";
		FunctionKey[12] = "\u001b[24~";

		// @todo - support and alternative modes for F13-F20, as per here; and
		// handling for shift keys:
		// http://aperiodic.net/phil/archives/Geekery/term-function-keys.html

		TabKey[0] = "\u0009";
		TabKey[1] = "\u001bOP\u0009";
		TabKey[2] = TabKey[3] = "";

		KeyUp = new String[4];
		KeyUp[0] = "\u001b[A";
		KeyDown = new String[4];
		KeyDown[0] = "\u001b[B";
		KeyRight = new String[4];
		KeyRight[0] = "\u001b[C";
		KeyLeft = new String[4];
		KeyLeft[0] = "\u001b[D";
	}

	private void resetTabs() {
		int nw = width;
		if (nw < 132) {
			nw = 132; // catch possible later 132/80 resizes
		}
		Tabs = new byte[nw];
		for (int i = 0; i < nw; i += 8) {
			Tabs[i] = 1;
		}

	}

	/**
	 * Create a default vt320 terminal with 80 columns and 24 lines.
	 */
	public VT320() {
		this(80, 24);
	}

	public VT320(String name) {
		this();
		this.debugName = name;

	}

	/**
	 * Send data to remote host
	 * 
	 * @param b
	 *            the array of bytes to be sent
	 * @param offset
	 *            offset from start of array to write
	 * @param length
	 *            number bytes to write
	 */
	protected void write(byte[] b, int offset, int length) {
		// Logger.debug("VT320.write(b, o, l) begin");
		if (writeBufferIndex + length > writeBuffer.length) {
			// Logger.debug("VT320.write recursing - write length too large");
			if (writeBufferIndex > 0) {
				// First write out what we have and then try to add this to the
				// buffer
				flush();
				write(b, offset, length);
			} else {
				// Buffer is too small so write out in parts
				System.arraycopy(b, offset, writeBuffer, 0, writeBuffer.length);
				writeBufferIndex = writeBuffer.length;
				flush();
				write(b, offset + writeBuffer.length, length
						- writeBuffer.length);
			}
			// Logger.debug("VT320.write recursing - outcall");
		} else {
			// Logger.debug("VT320.write non-recursive begin");
			System.arraycopy(b, offset, writeBuffer, writeBufferIndex, length);
			// Logger.debug("VT320.write non-recursive complete");
			writeBufferIndex += length;
		}
		// Logger.debug("VT320.write(b, o, l) end");
	}

	protected void flush() {
		// Logger.debug("VT320.flush invoked - idx = " + writeBufferIndex);

		try {
			sendData(writeBuffer, 0, writeBufferIndex);
		} catch (IOException e) {
			Logger.error("VT320.flush: IOException received - "
					+ e.getMessage());
		}
		writeBufferIndex = 0;
		// Logger.debug("VT320.flush complete.");
	}

	public abstract void sendData(byte[] b, int offset, int length)
			throws IOException;

	/**
	 * Play the beep sound ...
	 */
	public abstract void beep();

	/**
	 * Put string at current cursor position. Moves cursor according to the
	 * String. Does NOT wrap.
	 * 
	 * @param s
	 *            the string
	 */
	public void putString(String s) {
		int len = s.length();

		if (len > 0) {
			synchronized (termBufferMutex) {
				setLineDirty(ROW - 1);
				for (int i = 0; i < len; i++) {
					putChar(s.charAt(i), false);
				}
				// Move to end of page for cursor.
				setCursorPosition(COLUMN, ROW);
			}
		}
	}

	public void putStringStartLine(String s) {
		int len = s.length();

		if (len > 0) {
			synchronized (termBufferMutex) {
				setCursorPosition(0, ROW);
				setLineDirty(ROW - 1, 1);
				for (int i = 0; i < len; i++) {
					putChar(s.charAt(i), false);
				}
				setCursorPosition(0, ROW);
			}
		}

	}

	/** we should do localecho (passed from other modules). false is default */
	public boolean localecho = false;
	public long handledCharCount = 0;

	/**
	 * Enable or disable the local echo property of the terminal.
	 * 
	 * @param echo
	 *            true if the terminal should echo locally
	 */
	public void setLocalEcho(boolean echo) {
		localecho = echo;
	}

	/**
	 * Set the terminal id used to identify this terminal.
	 * 
	 * @param terminalID
	 *            the id string
	 */
	public void setTerminalID(String terminalID) {
		this.terminalID = terminalID;
	}

	// // public void setAnswerBack( String ab ) {
	// // this.answerBack = unEscape( ab );
	// // }
	/**
	 * Get the terminal id used to identify this terminal.
	 */
	public String getTerminalID() {
		return terminalID;
	}

	/**
	 * A small conveniance method that converts the string to a byte array for
	 * sending.
	 * 
	 * @param s
	 *            the string to be sent
	 */
	private boolean write(String s, boolean doecho) {
		// Logger.debug("VT320.write(s,b) begin");
		if (s == null) // aka the empty string.
		{
			// Logger.debug("VT320.write(s,b) end empty string");
			return true;
		}

		// @todo UTF8 UTF-8 ?
		/*
		 * NOTE: getBytes() honours some locale, it *CONVERTS* the string.
		 * However, we output only 7bit stuff towards the target, and *some* 8
		 * bit control codes. We must not mess up the latter, so we do hand by
		 * hand copy.
		 */

		// Maybe extend writeBuffer
		if (stringConversionBuffer.length < s.length()) {
			stringConversionBuffer = new byte[s.length()];
		}

		// Fill writeBuffer
		for (int i = 0; i < s.length(); i++) {
			stringConversionBuffer[i] = (byte) s.charAt(i);
		}
		write(stringConversionBuffer, 0, s.length());

		if (doecho) {
			putString(s);
		}
		// Logger.debug("VT320.write(s,b) end");

		return true;
	}

	private boolean write(String s) {
		return write(s, localecho);
	}

	/**
	 * A small conveniance method thar converts a 7bit string to the 8bit
	 * version depending on VT52/Output8Bit mode.
	 * 
	 * @param s
	 *            the string to be sent
	 */
	private boolean writeSpecial(String s) {
		if (s == null) {
			return true;
		}
		if (((s.length() >= 3) && (s.charAt(0) == 27) && (s.charAt(1) == 'O'))) {
			if (vt52mode) {
				if ((s.charAt(2) >= 'P') && (s.charAt(2) <= 'S')) {
					s = "\u001b" + s.substring(2); /* ESC x */
				} else {
					s = "\u001b?" + s.substring(2); /* ESC ? x */
				}
			} else {
				if (output8bit) {
					s = "\u008f" + s.substring(2); /* SS3 x */
				} /* else keep string as it is */
			}
		}
		if (((s.length() >= 3) && (s.charAt(0) == 27) && (s.charAt(1) == '['))) {
			if (output8bit) {
				s = "\u009b" + s.substring(2); /* CSI ... */
			} /* else keep */
		}
		return write(s, false);
	}

	/**
	 * Handle key Typed events for the terminal, this will get all normal key
	 * types, but no shift/alt/control/numlock.
	 */
	public void keyTyped(int keyCode, char keyChar, int modifiers) {
		_keyTyped(keyCode, keyChar, modifiers);
		flush();
	}

	// Provides appropriate handling for special keys if shift/alt/ctrl is
	// in use, but otherwise sends normally (?)
	// @todo look into terminal spec to see what really has to happen here and
	// why.
	public void dispatchKey(int keyCode, char keyChar, int modifiers) {
		if ((keyCode >= VK_PAGE_UP && keyCode <= VK_DOWN)
				|| keyCode == VK_BACK_SPACE || keyCode == VK_INSERT
				|| (keyCode >= VK_F1 && keyCode <= VK_F12)) {
			keyPressed(keyCode, modifiers);
		} else {
			keyTyped(keyCode, keyChar, modifiers);
		}
	}

	/**
	 * Properly sends only the following (along with any modifiers) VK_F1
	 * through VK_F12 up/dn/left/right/backspace/esc/home/end/insert
	 */
	public void keyPressed(int keyCode, int modifiers) {
		boolean control = (modifiers & KEY_CONTROL) != 0;
		boolean shift = (modifiers & KEY_SHIFT) != 0;
		boolean alt = (modifiers & KEY_ALT) != 0;

		int xind;
		xind = 0;
		// @todo - key COMBINATIONS such as ctrl+shift+alt are not possible
		// using this.

		if (shift) {
			xind = 1;
		}
		if (control) {
			xind = 2;
		}

		if (alt) {
			if (altPrefixesMeta) {
				writeSpecial(Escape[0]);
			} else {
				xind = 3;
			}
		}

		// @todo ctrl/alt/shift FN key is not working
		if (keyCode >= VT320.VK_F1 && keyCode <= VT320.VK_F12) {
			writeSpecial(FunctionKey[keyCode - VT320.VK_F1 + 1]);
		} else {
			// @todo this could be done with a map look up instead of
			// conditional/branching code.
			switch (keyCode) {
			case VT320.VK_UP:
				writeSpecial(KeyUp[xind]);
				break;
			case VT320.VK_DOWN:
				writeSpecial(KeyDown[xind]);
				break;
			case VT320.VK_LEFT:
				writeSpecial(KeyLeft[xind]);
				break;
			case VT320.VK_RIGHT:
				writeSpecial(KeyRight[xind]);
				break;
			case VT320.VK_PAGE_DOWN:
				writeSpecial(NextScn[xind]);
				break;
			case VT320.VK_PAGE_UP:
				writeSpecial(PrevScn[xind]);
				break;
			case VT320.VK_INSERT:
				writeSpecial(Insert[xind]);
				break;
			case VT320.VK_BACK_SPACE:
				writeSpecial(BackSpace[xind]);
				if (localecho) {
					if (BackSpace[xind].equals("\b")) {
						putString("\b \b"); // make the last char 'deleted'
					} else {
						putString(BackSpace[xind]); // echo it
					}
				}
				break;
			case VT320.VK_HOME:
				writeSpecial(KeyHome[xind]);
				break;
			case VT320.VK_END:
				writeSpecial(KeyEnd[xind]);
				break;
			}
		}

		flush();
	}

	public void stringTyped(String str) {
		// Logger.debug("VT320.stringTyped  " + str);
		for (int i = 0; i < str.length(); i++) {
			_keyTyped(0, str.charAt(i), 0);
		}
		flush();
	}

	// @todo we need to present a common interface for sending character --
	// there's no clear distinction for the end user about when we should send
	// keyTyped vs keyPressed.

	private void _keyTyped(int keyCode, char keyChar, int modifiers) {
		// Logger.debug("VT320._keyTyped: " + (int) keyChar); @todo DebugVT320
		boolean control = (modifiers & KEY_CONTROL) != 0;
		boolean shift = (modifiers & KEY_SHIFT) != 0;
		boolean alt = (modifiers & KEY_ALT) != 0;

		if (keyChar == '\t') {
			if (shift) {
				write(TabKey[1], false);
			} else {
				if (control) {
					write(TabKey[2], false);
				} else {
					if (alt) {
						if (altPrefixesMeta) {
							writeSpecial(Escape[0]);
							write(TabKey[0], false);
						} else {
							write(TabKey[3], false);

						}

					} else {
						write(TabKey[0], false);
					}
				}
			}
			return;
		}
		if (alt && !altPrefixesMeta) {
			if (keyChar == VT320.VK_ESCAPE) {
				writeSpecial(Escape[3]);
			} else {
				write("" + ((char) (keyChar | 0x80)));
			}
			return;
		}

		if (((keyCode == VT320.VK_ENTER) || (keyChar == 10)) && !control) {
			write("\r", false);
			if (localecho) {
				putString("\r\n"); // bad hack
			}
			return;
		}

		int xind = 0;

		// @todo only one at a time, but what if we need to combine?
		if (shift) {
			xind = 1;
		}
		if (control) {
			xind = 2;
		}
		if (alt && altPrefixesMeta) {
			writeSpecial(Escape[0]);
		}

		if (keyCode == VT320.VK_ESCAPE) {
			writeSpecial(Escape[xind]);
			return;
		}
		// enter, tab, esc, backspace,

		if (!((keyChar == 8) || (keyChar == 127) || (keyChar == '\r') || (keyChar == '\n'))) {
			// @todo - map - hw much of this is needed? Shift keys are not, at
			// least.
			// KARL support for control codes and shift keys
			if (control) {
				if (keyChar >= 'a' && keyChar <= 'z') {
					keyChar = (char) (keyChar - 'a' + 'A');
				}
				if (keyChar >= 'A' && keyChar <= 'Z') {
					keyChar = (char) (keyChar - 'A' + 1);
					write("" + keyChar);
				} else {
					switch (keyChar) {
					case '@':
						keyChar = (char) 0;
						break;
					case '[':
						keyChar = (char) 27;
						break;
					case '\\':
						keyChar = (char) 28;
						break;
					case ']':
						keyChar = (char) 29;
						break;
					case '^':
						keyChar = (char) 30;
						break;
					case '_':
						keyChar = (char) 31;
						break;
					}
					write("" + keyChar);
				}
			} else if (shift) {
				if (keyChar >= 'a' && keyChar <= 'z') {
					keyChar = (char) (keyChar - 'a' + 'A');
				}
				write("" + keyChar);
			} else {
				write("" + keyChar);
			}
			return;
		}
	}

	private void _SetCursor(int row, int col) {
		int maxr = height;
		int tm = getTopMargin();

		ROW = (row < 0) ? 0 : row;
		COLUMN = (col < 0) ? 0 : col;

		if (!moveoutsidemargins) {
			ROW += tm;
			maxr = getBottomMargin();
		}
		if (ROW > maxr) {
			ROW = maxr;
		}
	}

	// StringBuffer buf = new StringBuffer(1024);

	public void putChar(char c, boolean doshowcursor) {
		handledCharCount++;
		int rows = height;
		int columns = width;
		int tm = getTopMargin();
		int bm = getBottomMargin();
		setLineDirty(ROW, 1);
		// if (buf.length() >= 1000) {
		// Logger.error(buf.toString());
		// buf = new StringBuffer(1024);
		// } else {
		// buf.append('<').append(c).append(" / ").append((int) c).append(">");
		// }

		// These control sequences are required to take precedence - even if
		// another escape sequence is
		// in process. So this means in TSTATE_DATA, TSTATE_CSI, and TSTATE_ESC
		// we must pause and process.

		// Reference:
		// http://vt100.net/docs/vt100-ug/chapter3.html
		// "Terminal Control Commands"
		// Control characters (codes 08 to 378 inclusive) are specifically
		// excluded from the control sequence syntax,
		// but may be embedded within a control sequence. Embedded control
		// characters are executed as soon as they are
		// encountered by the VT100. The processing of the control sequence then
		// continues with the next character
		// received. The exceptions are: if the character ESC occurs, the
		// current control sequence is aborted, and a new
		// one commences beginning with the ESC just received. If the character
		// CAN (308) or the character SUB (328)
		// occurs, the current control sequence is aborted. The ability to embed
		// control characters allows the
		// synchronization characters XON and XOFF to be interpreted properly
		// without affecting the control sequence.
		//
		boolean done = false;
		if (term_state == TSTATE_DATA || term_state == TSTATE_CSI
				|| term_state == TSTATE_ESC) {
			done = true;
			switch (c) {
			case SS3:
				onegl = 3;
				break;
			case SS2:
				onegl = 2;
				break;
			case SUB:
			case CAN: // interrupt escape sequnce
				setTermState(TSTATE_DATA);
				break;
			case DEL: // ignored
				break;
			case CSI: // should be in the 8bit section, but some BBS use this
				DCEvar = 0;
				DCEvars[0] = 0;
				DCEvars[1] = 0;
				DCEvars[2] = 0;
				DCEvars[3] = 0;
				setTermState(TSTATE_CSI); // also aborts previous mode
				break;
			case ESC:
				setTermState(TSTATE_ESC); // aborts previous mode
				lastwaslf = 0;
				break;
			case FF:
				// FormFeed, - implementation depednent - some BBSs use it
				// as Home, so we will too.
				deleteArea(0, 0, columns, rows, attributes);
				COLUMN = ROW = 0;
				break;
			case BS: /* 8 */
				COLUMN--;
				if (COLUMN < 0) {
					COLUMN = 0;
				}
				lastwaslf = 0;
				break;
			case HT: // tabs are movement, non-destructive...
				do {
					COLUMN++;
				} while (COLUMN < columns && (Tabs[COLUMN] == 0));
				// lastwaslf = 0;
				break;
			case LF:
			case VT:
				if (ROW == bm || ROW >= rows - 1) {
					insertLine(ROW, 1, SCROLL_UP);
				} else {
					ROW++;
				}
				break;
			case CR:
				COLUMN = 0;
				// putty:
				// CR: term->curs.x = 0;
				// term->wrapnext = FALSE;
				// ff: if compat, clear
				// vt, lf scroll and add line

				// if (lastwaslf != 0 && lastwaslf != c) {
				// break;
				// }
				// lastwaslf = c;
				break;
			case BEL:
				beep();
				break;
			case SO:
				/* ^N, Shift out - Put G1 into GL */
				gl = 1;
				usedcharsets = true;
				// @todo this is also supposed to add lf/nl if newline mode
				// enabled
				break;
			case SI:
				/* ^O, Shift in - Put G0 into GL */
				// ondocumented as to whether this adds lf/nl like SO, so assume
				// not
				gl = 0;
				usedcharsets = true;
				break;
			default:
				done = false;
				break;

			} // switch c

		}
		// @todo Serious kludge for now -- will have to revisit.
		if (!done) {
			switch (term_state) {
			case TSTATE_DATA:
				/*
				 * FIXME: we shouldn't use chars with bit 8 set if ibmcharset.
				 * probably... but some BBS do anyway...
				 */
				boolean doneflag = true;
				switch (c) {
				// @todo are these 8 bit control codes also escape
				// sequence
				// interrupting?
				case OSC:
					osc = "";
					setTermState(TSTATE_OSC);
					break;
				case RI:
					if (ROW > tm) {
						ROW--;
					} else {
						insertLine(ROW, 1, SCROLL_DOWN);
					}
					break;
				case IND:
					if (ROW == bm || ROW == rows - 1) {
						insertLine(ROW, 1, SCROLL_UP);
					} else {
						ROW++;
					}
					break;
				case NEL:
					if (ROW == bm || ROW == rows - 1) {
						insertLine(ROW, 1, SCROLL_UP);
					} else {
						ROW++;
					}
					COLUMN = 0;
					break;
				case HTS:
					Tabs[COLUMN] = 1;
					break;
				case DCS:
					dcs = "";
					setTermState(TSTATE_DCS);
					break;
				default:
					doneflag = false;
					break;
				}
				if (doneflag) {
					break;
				}
				if (onegl >= 0) {
					onegl = -1;
				}
				lastwaslf = 0;
				if (c < 32) {
					if (c != 0) /*
								 * break; some BBS really want those characters,
								 * like hearst etc.
								 */{
						if (c == 0) /* print 0 ... you bet */{
							break;
						}
					}
				}
				if (COLUMN >= columns) {
					if (wraparound) {

						// Mark the current line as wrapped
						if (ROW > -1 && ROW < height - 1) {
							terminalData[ROW][columns - 1] |= XATTR_WRAP;
						}

						if (ROW < rows - 1) {
							ROW++;
						} else {
							insertLine(ROW, 1, SCROLL_UP);
						}

						COLUMN = 0;
					} else {
						// cursor stays on last character.
						COLUMN = columns - 1;
					}
				}

				// Mapping if DEC Special is chosen charset
				/* if(true || (statusmode == 0)) { */
				if (insertmode == 1) {
					insertChar(COLUMN, ROW, c, attributes);
				} else {
					putChar(COLUMN, ROW, c, attributes);
				}
				COLUMN++;
				break; // TSTATE_DATA

			case TSTATE_OSC:
				// @todo OSC is where we'll look to implement window title
				// changes
				if ((c < 0x20) && (c != ESC)) {// NP - No printing
					// character handle_osc( osc );
					setTermState(TSTATE_DATA);
					break;
				}
				// but check for vt102 ESC \
				if (c == '\\' && osc.charAt(osc.length() - 1) == ESC) {
					// handle_osc( osc );
					setTermState(TSTATE_DATA);
					break;
				}
				osc = osc + c;
				break;
			case TSTATE_ESCSPACE:
				setTermState(TSTATE_DATA);
				switch (c) {
				case 'F': /*
						 * S7C1T, Disable output of 8-bit controls, use 7-bit
						 */
					output8bit = false;
					break;
				case 'G': /* S8C1T, Enable output of 8-bit control codes */
					output8bit = true;
					break;
				default:
				}
				break;
			case TSTATE_ESC:
				setTermState(TSTATE_DATA);
				switch (c) {
				case ' ':
					setTermState(TSTATE_ESCSPACE);
					break;
				case '#':
					setTermState(TSTATE_ESCSQUARE);
					break;
				case 'c':
					resetTerminal();
					break;
				case '[':
					DCEvar = 0;
					DCEvars[0] = 0;
					DCEvars[1] = 0;
					DCEvars[2] = 0;
					DCEvars[3] = 0;
					setTermState(TSTATE_CSI);
					break;
				case ']':
					osc = "";
					setTermState(TSTATE_OSC);
					break;
				case 'P':
					dcs = "";
					setTermState(TSTATE_DCS);
					break;
				case 'A': /* CUU */
					ROW--;
					if (ROW < 0) {
						ROW = 0;
					}
					break;
				case 'B': /* CUD */
					ROW++;
					if (ROW > rows - 1) {
						ROW = rows - 1;
					}
					break;
				case 'C':
					COLUMN++;
					if (COLUMN >= columns) {
						COLUMN = columns - 1;
					}
					break;
				case 'I': // RI
					insertLine(ROW, 1, SCROLL_DOWN);
					break;
				case 'E': /* NEL */
					if (ROW == bm || ROW == rows - 1) {
						insertLine(ROW, 1, SCROLL_UP);
					} else {
						ROW++;
					}
					COLUMN = 0;
					break;
				case 'D': /* IND */
					if (ROW == bm || ROW == rows - 1) {
						insertLine(ROW, 1, SCROLL_UP);
					} else {
						ROW++;
					}
					break;
				case 'J': /* erase to end of screen */
					if (ROW < rows - 1) {
						deleteArea(0, ROW + 1, columns, rows - ROW - 1,
								attributes);
					}
					if (COLUMN < columns - 1) {
						deleteArea(COLUMN, ROW, columns - COLUMN, 1, attributes);
					}
					break;
				case 'K':
					if (COLUMN < columns - 1) {
						deleteArea(COLUMN, ROW, columns - COLUMN, 1, attributes);
					}
					break;
				case 'M': // RI
					if (ROW > bm) // outside scrolling region
					{
						break;
					}
					if (ROW > tm) { // just go up 1 line.
						ROW--;
					} else { // scroll down
						insertLine(ROW, 1, SCROLL_DOWN);
					}
					/* else do nothing ; */
					break;
				case 'H':
					/* right border probably ... */
					if (COLUMN >= columns) {
						COLUMN = columns - 1;
					}
					Tabs[COLUMN] = 1;
					break;
				case 'N': // SS2
					onegl = 2;
					break;
				case 'O': // SS3
					onegl = 3;
					break;
				case '=':
					/* application keypad */
					keypadmode = true;
					break;
				case '<': /* vt52 mode off */
					vt52mode = false;
					break;
				case '>': /* normal keypad */
					keypadmode = false;
					break;
				case '7': /* save cursor, attributes, margins */
					Sc = COLUMN;
					Sr = ROW;
					Sgl = gl;
					Sgr = gr;
					Sa = attributes;
					Sgx = new char[4];
					for (int i = 0; i < 4; i++) {
						Sgx[i] = gx[i];
					}
					Stm = getTopMargin();
					Sbm = getBottomMargin();
					break;
				case '8': /* restore cursor, attributes, margins */
					COLUMN = Sc;
					ROW = Sr;
					gl = Sgl;
					gr = Sgr;
					for (int i = 0; i < 4; i++) {
						gx[i] = Sgx[i];
					}
					setTopMargin(Stm);
					setBottomMargin(Sbm, false);
					attributes = Sa;
					break;
				case '(': /* Designate G0 Character set (ISO 2022) */
					setTermState(TSTATE_SETG0);
					usedcharsets = true;
					break;
				case ')': /* Designate G1 character set (ISO 2022) */
					setTermState(TSTATE_SETG1);
					usedcharsets = true;
					break;
				case '*': /* Designate G2 Character set (ISO 2022) */
					setTermState(TSTATE_SETG2);
					usedcharsets = true;
					break;
				case '+': /* Designate G3 Character set (ISO 2022) */
					setTermState(TSTATE_SETG3);
					usedcharsets = true;
					break;
				case '~': /* Locking Shift 1, right */
					gr = 1;
					usedcharsets = true;
					break;
				case 'n': /* Locking Shift 2 */
					gl = 2;
					usedcharsets = true;
					break;
				case '}': /* Locking Shift 2, right */
					gr = 2;
					usedcharsets = true;
					break;
				case 'o': /* Locking Shift 3 */
					gl = 3;
					usedcharsets = true;
					break;
				case '|': /* Locking Shift 3, right */
					gr = 3;
					usedcharsets = true;
					break;
				case 'Y': /* vt52 cursor address mode , next chars are x,y */
					setTermState(TSTATE_VT52Y);
					break;
				default:
					Logger.debug("Unknown ESC: " + c + " / " + (int) c);
					break;
				}
				break;
			case TSTATE_VT52X:
				COLUMN = c - 37;
				setTermState(TSTATE_VT52Y);
				break;
			case TSTATE_VT52Y:
				ROW = c - 37;
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_SETG0:
				if (!(c != '0' && c != 'A' && c != 'B' && c != '<')) {
					gx[0] = c;
				}
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_SETG1:
				if (!(c != '0' && c != 'A' && c != 'B' && c != '<')) {
					gx[1] = c;
				}
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_SETG2:
				if (!(c != '0' && c != 'A' && c != 'B' && c != '<')) {
					gx[2] = c;
				}
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_SETG3:
				if (!(c != '0' && c != 'A' && c != 'B' && c != '<')) {
					gx[3] = c;
				}
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_ESCSQUARE:
				switch (c) {
				case '8':
					for (int i = 0; i < columns; i++) {
						for (int j = 0; j < rows; j++) {
							putChar(i, j, 'E', 0);
						}
					}
					break;
				default:
					break;
				}
				setTermState(TSTATE_DATA);
				break;
			case TSTATE_DCS:
				if (c == '\\' && dcs.charAt(dcs.length() - 1) == ESC) {
					// handle_dcs( dcs );
					setTermState(TSTATE_DATA);
					break;
				}
				dcs = dcs + c;
				break;

			case TSTATE_DCEQ:
				setTermState(TSTATE_DATA);
				switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					DCEvars[DCEvar] = DCEvars[DCEvar] * 10 + ((int) c) - 48;
					setTermState(TSTATE_DCEQ);
					break;
				case ';':
					DCEvar++;
					DCEvars[DCEvar] = 0;
					setTermState(TSTATE_DCEQ);
					break;
				case 's': // XTERM_SAVE missing!
					break;
				case 'r': // XTERM_RESTORE
					/* DEC Mode restore - I believe we should be toggling here? */
					for (int i = 0; i <= DCEvar; i++) {
						switch (DCEvars[i]) {
						case 3: /* 80 columns */
							setScreenSize(80, height, false);
							break;
						case 4: /* scrolling mode, smooth */
							break;
						case 5: /* light background */
							break;
						case 6: /*
								 * DECOM (Origin Mode) move inside margins.
								 */
							moveoutsidemargins = true;
							break;
						case 7: /* DECAWM: Autowrap Mode */
							wraparound = false;
							break;
						case 12:/* local echo off */
							break;
						case 9: /* X10 mouse */
						case 1000: /* xterm style mouse report on */
						case 1001:
						case 1002:
						case 1003:
							mouserpt = DCEvars[i];
							break;
						default:
						}
					}
					break;
				case 'h': // DECSET
					/* DEC Mode set */
					for (int i = 0; i <= DCEvar; i++) {
						switch (DCEvars[i]) {
						case 1: /* Application cursor keys */
							KeyUp[0] = "\u001bOA";
							KeyDown[0] = "\u001bOB";
							KeyRight[0] = "\u001bOC";
							KeyLeft[0] = "\u001bOD";
							break;
						case 2: /* DECANM */
							vt52mode = false;
							break;
						case 3: /* 132 columns */
							setScreenSize(132, height, false);
							break;
						case 6: /* DECOM: move inside margins. */
							moveoutsidemargins = false;
							break;
						case 7: /* DECAWM: Autowrap Mode */
							wraparound = true;
							break;
						case 25: /* turn cursor on */
							showCursor(true);
							break;
						case 9: /* X10 mouse */
						case 1000: /* xterm style mouse report on */
						case 1001:
						case 1002:
						case 1003:
							mouserpt = DCEvars[i];
							break;

						/* unimplemented stuff, fall through */
						/* 4 - scrolling mode, smooth */
						/* 5 - light background */
						/* 12 - local echo off */
						/*
						 * 18 - DECPFF - Printer Form Feed Mode -> On
						 */
						/*
						 * 19 - DECPEX - Printer Extent Mode -> Screen
						 */
						default:
							break;
						}
					}
					break;
				case 'i': // DEC Printer Control, autoprint, echo
					// screenchars to
					// printer
					// This is different to CSI i!
					// Also: "Autoprint prints a final display line only
					// when the
					// cursor is moved off the line by an autowrap or
					// LF,
					// FF, or
					// VT (otherwise do not print the line)."
					switch (DCEvars[0]) {
					case 1:
						break;
					case 4:
						break;
					case 5:
						break;
					}
					break;
				case 'l': // DECRST
					/* DEC Mode reset */
					for (int i = 0; i <= DCEvar; i++) {
						switch (DCEvars[i]) {
						case 1: /* Application cursor keys */
							KeyUp[0] = "\u001b[A";
							KeyDown[0] = "\u001b[B";
							KeyRight[0] = "\u001b[C";
							KeyLeft[0] = "\u001b[D";
							break;
						case 2: /* DECANM */
							vt52mode = true;
							break;
						case 3: /* 80 columns */
							setScreenSize(80, height, false);
							break;
						case 6: /* DECOM: move outside margins. */
							moveoutsidemargins = true;
							break;
						case 7: /* DECAWM: Autowrap Mode OFF */
							wraparound = false;
							break;
						case 25: /* turn cursor off */
							showCursor(false);
							break;
						/*
						 * ESC [ 1 l Keyboard action KAM Unlocked ESC [ 2 l
						 * Insertion-replacement IRM Replace ESC [ 4 l
						 * Send-receive SRM On ESC [ 1 2 l Linefeed/new line LMN
						 * Linefeed ESC [ 2 0 l Cursor key DECCKM Cursor ESC [ ?
						 * 1 l ANSI/VT52 DECANM VT52 ESC [ ? 2 l Scrolling
						 * DECSCLM Jump ESC [ ? 4 l Screen DECSCNM Normal ESC [
						 * ? 5 l Origin DECOM Absolute ESC [ ? 6 l Auto wrap
						 * DECAWM Off ESC [ ? 7 l Auto repeat DECARM Off ESC [ ?
						 * 8 l Print form feed DECPFF Off ESC [ ? 1 8 l Print
						 * extent DECPEX Scrolling region ESC [ ? 1 9 l /* 4 -
						 * scrolling mode, jump
						 */

						/*
						 * 12 - local echo on
						 */
						case 9: /* X10 mouse */
						case 1000: /* xterm style mouse report OFF */
						case 1001:
						case 1002:
						case 1003:
							mouserpt = 0;
							break;
						default:
							break;
						}
					}
					break;
				case 'n':
					switch (DCEvars[0]) {
					case 15:
						/* printer? no printer. */
						write(((char) ESC) + "[?13n", false);
						flush();
						break;
					default:
						break;
					}
					break;
				default:
					Logger.debug("Unknown TSTATE_DCEQ value: " + c + " / "
							+ (int) c);

					break;
				}
				break;
			case TSTATE_CSI_EX:
				setTermState(TSTATE_DATA);
				switch (c) {
				case ESC:
					setTermState(TSTATE_ESC);
					break;
				default:
					break;
				}
				break;
			case TSTATE_CSI_TICKS:
				setTermState(TSTATE_DATA);
				switch (c) {
				case 'p':
					if (DCEvars[0] == 61) {
						output8bit = false;
						break;
					}
					if (DCEvars[1] == 1) {
						output8bit = false;
					} else {
						output8bit = true; /* 0 or 2 */
					}
					break;
				default:
					Logger.debug("Unknown TSTATE_CSI_TICKS: " + c + " / "
							+ (int) c);

					break;
				}
				break;
			case TSTATE_CSI_DOLLAR:
				setTermState(TSTATE_DATA);
				switch (c) {
				case '}':
					statusmode = DCEvars[0];
					break;
				case '~':
					break;
				default:
					Logger.debug("Unknown TSTATE_CSI_DOLLAR: " + c + " / "
							+ (int) c);
					break;
				}
				break;
			case TSTATE_CSI:
				setTermState(TSTATE_DATA);
				switch (c) {
				case '"':
					setTermState(TSTATE_CSI_TICKS);
					break;
				case '$':
					setTermState(TSTATE_CSI_DOLLAR);
					break;
				case '!':
					setTermState(TSTATE_CSI_EX);
					break;
				case '?':
					DCEvar = 0;
					DCEvars[0] = 0;
					setTermState(TSTATE_DCEQ);
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					DCEvars[DCEvar] = DCEvars[DCEvar] * 10 + ((int) c) - 48;
					// buf.append("\r\nvar[" + DCEvar + "] = " +
					// DCEvars[DCEvar]);
					setTermState(TSTATE_CSI);
					break;
				case ';':
					DCEvar++;
					DCEvars[DCEvar] = 0;
					setTermState(TSTATE_CSI);
					break;
				// @note This can never be reached here - CSI + c is not a valid
				// combination
				// only CSI + ? + c (DA), or ESC + c (reset). Because '?' above
				// puts us in state DCEQ,
				// 'c' will only be reached in that state.
				// Leaving it here in case it was initially placed due to a
				// discovered
				// incompatibility/host incorrect behavior.
				case 'c':/* send primary device attributes */
					replyDA();
					break;
				case 'q':
					break;
				case 'g':
					/* used for tabsets */
					switch (DCEvars[0]) {
					case 3:/* clear them */
						Tabs = new byte[width];
						break;
					case 0:
						Tabs[COLUMN] = 0;
						break;
					}
					break;
				case 'h':
					switch (DCEvars[0]) {
					case 4:
						insertmode = 1;
						break;
					case 20:
						sendcrlf = true;
						break;
					default:
						break;
					}
					break;
				case 'i': // Printer Controller mode.
					// "Transparent printing sends all output, except
					// the
					// CSI 4 i
					// termination string, to the printer and not the
					// screen,
					// uses an 8-bit channel if no parity so NUL and DEL
					// will be
					// seen by the printer and by the termination
					// recognizer
					// code,
					// and all translation and character set selections
					// are
					// bypassed."
					switch (DCEvars[0]) {
					case 0:
						break;
					case 4:
						break;
					case 5:
						break;
					default:
					}
					break;
				case 'l':
					switch (DCEvars[0]) {
					case 4:
						insertmode = 0;
						break;
					case 20:
						sendcrlf = false;
						break;
					default:
						break;
					}
					break;
				case 'A': // CUU
				{
					int limit;
					/* FIXME: xterm only cares about 0 and topmargin */
					if (ROW > bm) {
						limit = bm + 1;
					} else if (ROW >= tm) {
						limit = tm;
					} else {
						limit = 0;
					}
					if (DCEvars[0] == 0) {
						ROW--;
					} else {
						ROW -= DCEvars[0];
					}
					if (ROW < limit) {
						ROW = limit;
					}
					break;
				}
				case 'B': // CUD
					/* cursor down n (1) times */{
					int limit;
					if (ROW < tm) {
						limit = tm - 1;
					} else if (ROW <= bm) {
						limit = bm;
					} else {
						limit = rows - 1;
					}
					if (DCEvars[0] == 0) {
						ROW++;
					} else {
						ROW += DCEvars[0];
					}
					if (ROW > limit) {
						ROW = limit;
					}
					break;
				}
				case 'C':
					if (DCEvars[0] == 0) {
						COLUMN++;
					} else {
						COLUMN += DCEvars[0];
					}
					if (COLUMN > columns - 1) {
						COLUMN = columns - 1;
					}
					break;
				case 'd': // CVA
					ROW = DCEvars[0];
					break;
				case 'D':
					if (DCEvars[0] == 0) {
						COLUMN--;
					} else {
						COLUMN -= DCEvars[0];
					}
					if (COLUMN < 0) {
						COLUMN = 0;
					}
					break;
				case 'r': // DECSTBM
					if (DCEvar > 0) // Ray: Any argument is optional
					{
						ROW = DCEvars[1] - 1;
						if (ROW < 0) {
							ROW = rows - 1;
						} else if (ROW >= rows) {
							ROW = rows - 1;
						}
					} else {
						ROW = rows - 1;
					}
					setBottomMargin(ROW, true);
					if (ROW >= DCEvars[0]) {
						ROW = DCEvars[0] - 1;
						if (ROW < 0) {
							ROW = 0;
						}
					}
					setTopMargin(ROW);
					_SetCursor(0, 0);
					// System.out.println("DECSTBM " + DCEvars[0] + " ; " +
					// DCEvars[1]);

					break;
				case 'G': /* Horizontal Position Absolute (CHA) */
				case '`': // Same - SCO
				case '\'': // Same - SCO
					COLUMN = DCEvars[0] - 1;
					break;
				case 'H': /* CUP / cursor position */
					/* gets 2 arguments */
					_SetCursor(DCEvars[0] - 1, DCEvars[1] - 1);
					break;
				case 'f': /* move cursor 2 */
					/* gets 2 arguments */
					ROW = DCEvars[0] - 1;
					COLUMN = DCEvars[1] - 1;
					if (COLUMN < 0) {
						COLUMN = 0;
					}
					if (ROW < 0) {
						ROW = 0;
					}
					break;
				case 'S': /* ind aka 'scroll forward' */
					if (DCEvars[0] == 0) {
						insertLine(rows - 1, SCROLL_UP);
					} else {
						insertLine(rows - 1, DCEvars[0], SCROLL_UP);
					}
					break;
				case 'L':
					/* insert n lines */
					if (DCEvars[0] == 0) {
						insertLine(ROW, SCROLL_DOWN);
					} else {
						insertLine(ROW, DCEvars[0], SCROLL_DOWN);
					}
					break;
				case 'T': /* 'ri' aka scroll backward */
					if (DCEvars[0] == 0) {
						insertLine(0, SCROLL_DOWN);
					} else {
						insertLine(0, DCEvars[0], SCROLL_DOWN);
					}
					break;
				case 'M':
					if (DCEvars[0] == 0) {
						deleteLine(ROW);
					} else {
						for (int i = 0; i < DCEvars[0]; i++) {
							deleteLine(ROW);
						}
					}
					break;
				case 'K':
					/* clear in line */
					switch (DCEvars[0]) {
					case 6: /*
							 * 97801 uses ESC[6K for delete to end of line
							 */
					case 0:/* clear to right */
						if (COLUMN < columns - 1) {
							deleteArea(COLUMN, ROW, columns - COLUMN, 1,
									attributes);
						}
						break;
					case 1:/* clear to the left, including this */
						if (COLUMN > 0) {
							deleteArea(0, ROW, COLUMN + 1, 1, attributes);
						}
						break;
					case 2:/* clear whole line */
						deleteArea(0, ROW, columns, 1, attributes);
						break;
					}
					break;
				case 'J':
					/* clear below current line */
					switch (DCEvars[0]) {
					case 0:
						if (ROW < rows - 1) {
							deleteArea(0, ROW + 1, columns, rows - ROW - 1,
									attributes);
						}
						if (COLUMN < columns - 1) {
							deleteArea(COLUMN, ROW, columns - COLUMN, 1,
									attributes);
						}
						break;
					case 1:
						if (ROW > 0) {
							deleteArea(0, 0, columns, ROW, attributes);
						}
						if (COLUMN > 0) {
							deleteArea(0, ROW, COLUMN + 1, 1, attributes);// include
						} // up
							// to
							// and including
							// current
						break;
					case 2:
						deleteArea(0, 0, columns, rows, attributes);
						break;
					}
					break;
				case '@':
					for (int i = 0; i < DCEvars[0]; i++) {
						insertChar(COLUMN, ROW, ' ', attributes);
					}
					break;
				case 'X': {
					int toerase = DCEvars[0];
					if (toerase == 0) {
						toerase = 1;
					}
					if (toerase + COLUMN > columns) {
						toerase = columns - COLUMN;
					}
					deleteArea(COLUMN, ROW, toerase, 1, attributes);
					// does not change cursor position
					break;
				}
				case 'P':
					if (DCEvars[0] == 0) {
						DCEvars[0] = 1;
					}
					for (int i = 0; i < DCEvars[0]; i++) {
						deleteChar(COLUMN, ROW);
					}
					break;
				case 'n':
					switch (DCEvars[0]) {
					case 5: /* malfunction? No malfunction. */
						writeSpecial(((char) ESC) + "[0n");
						flush();
						break;
					case 6:
						writeSpecial(((char) ESC) + "[" + ROW + ";" + COLUMN
								+ "R");
						flush();
						break;
					default:
						break;
					}
					break;
				case 's': /* DECSC - save cursor */
					Sc = COLUMN;
					Sr = ROW;
					Sa = attributes;
					break;
				case 'u': /* DECRC - restore cursor */
					COLUMN = Sc;
					ROW = Sr;
					attributes = Sa;
					break;
				case 'm': /* attributes as color, bold , blink, */// SGR ESC [ nn
																// m
					if (DCEvar == 0 && DCEvars[0] == 0) {
						attributes = 0;
					}
					for (int i = 0; i <= DCEvar; i++) {
						switch (DCEvars[i]) {
						case 0:
							if (DCEvar > 0) {
								if (terminalID.equals("scoansi")) {
									attributes &= COLOR; /*
														 * Keeps color. Strange
														 * but true.
														 */
								} else {
									attributes = 0;
								}
							}
							break;
						case 1:
							attributes |= BOLD;
							attributes &= ~LOW;
							break;
						case 2:
							/* SCO color hack mode */
							if (terminalID.equals("scoansi")
									&& ((DCEvar - i) >= 2)) {
								int ncolor;
								attributes &= ~(COLOR | BOLD);

								ncolor = DCEvars[i + 1];
								if ((ncolor & 8) == 8) {
									attributes |= BOLD;
								}
								ncolor = ((ncolor & 1) << 2) | (ncolor & 2)
										| ((ncolor & 4) >> 2);
								attributes |= ((ncolor) + 1) << 4;
								ncolor = DCEvars[i + 2];
								ncolor = ((ncolor & 1) << 2) | (ncolor & 2)
										| ((ncolor & 4) >> 2);
								attributes |= ((ncolor) + 1) << 8;
								i += 2;
							} else {
								attributes |= LOW;
							}
							break;
						case 4:
							attributes |= UNDERLINE;
							break;
						case 7:
							attributes |= INVERT;
							break;
						case 5: /* blink on */
							attributes |= BLINK;
							break;
						/*
						 * 10 - ANSI X3.64-1979, select primary font, don't
						 * display control chars, don't set bit 8 on output
						 */
						case 10:
							gl = 0;
							usedcharsets = true;
							break;
						/*
						 * 11 - ANSI X3.64-1979, select second alt. font,
						 * display control chars, set bit 8 on output
						 */
						case 11: /* SMACS , as */
						case 12:
							gl = 1;
							usedcharsets = true;
							break;
						case 21: /* normal intensity */
							attributes &= ~(LOW | BOLD);
							break;
						case 25: /* blinking off */
							break;
						case 27:
							attributes &= ~INVERT;
							break;
						case 24:
							attributes &= ~UNDERLINE;
							break;
						case 22:
							attributes &= ~BOLD;
							break;
						case 30:
						case 31:
						case 32:
						case 33:
						case 34:
						case 35:
						case 36:
						case 37:
							attributes &= ~COLOR_FG;
							attributes |= ((DCEvars[i] - 30) + 1) << 4;
							break;
						case 39:
							attributes &= ~COLOR_FG;
							break;
						case 40:
						case 41:
						case 42:
						case 43:
						case 44:
						case 45:
						case 46:
						case 47:
							attributes &= ~COLOR_BG;
							attributes |= ((DCEvars[i] - 40) + 1) << 8;
							break;
						case 49:
							attributes &= ~COLOR_BG;
							break;

						default:
							break;
						}
					}
					break;
				default:
					Logger.debug("Unknown CSI: " + c + " / " + (int) c);
					break;
				}
				break;
			default:
				setTermState(TSTATE_DATA);
				break;
			}
		}
		if (COLUMN > columns) {
			COLUMN = columns;
		}
		if (ROW > rows) {
			ROW = rows;
		}
		if (COLUMN < 0) {
			COLUMN = 0;
		}
		if (ROW < 0) {
			ROW = 0;
		}
		if (doshowcursor) {
			setCursorPosition(COLUMN, ROW);
		}

		setLineDirty(ROW, 1);
	}

	private String daReply;

	private void replyDA() {

		// the only proper ways to ask are ESC [0c or ESC [c
		// so if we re ceived a value preceding c that wasn't 0,
		// it's not right.
		if (DCEvars[0] != 0) {
			Logger.warn("Received sequence ESC[?nc incorrectly, where n should be 0 and was "
					+ DCEvars[0]);
			return;
		}

		// @todo make sure 1c is not a valid sequence? we don't haev a behavior
		// defined for it.
		// [?0c or [?c
		/*
		 * reference http://computer-refuge.org/classiccmp/dec94mds/vt520rma.txt
		 * "DA1" reference: http://vt100.net/docs/vt220-rm/chapter4.hs
		 * "4.17.1.1" CSI ? 1; 2 c VT100 terminal ID DECTID CSI ? 1; 0 c VT101
		 * terminal ID DECTID CSI ? 6 c VT102 terminal ID DECTID CSI ? 62; 1; 2;
		 * 7; 8 c VT220 North American DECTID CSI ? 62; 1; 2; 7; 8; 9 c VT220
		 * International DECTID CSI ? 63; 1; 2; 7; 8 c VT320 North American
		 * DECTID CSI ? 63; 1; 2; 7; 8; 9 c VT320 International DECTID
		 * reference: http://vt100.net/docs/vt100-ug/chapter3.html
		 * "DA - Device Attributes" Response to the request described above
		 * (VT100 to host) is generated by the VT100 as a DA control sequence
		 * with the numeric parameters as follows: Option Present Sequence Sent
		 * No options ESC [?1;0c Processor option (STP) ESC [?1;1c Advanced
		 * video option (AVO) ESC [?1;2c AVO and STP ESC [?1;3c Graphics option
		 * (GPO) ESC [?1;4c GPO and STP ESC [?1;5c GPO and AVO ESC [?1;6c GPO,
		 * STP and AVO ESC [?1;7c *
		 * 
		 * AVO = (132 character AND 24 lines - we can support that even if we
		 * have to fake it. So - except for "ansi" emulation, our reply always
		 * be include 1;2c
		 */

		if (daReply == null) {

			// @todo when we introduce line-drawing support, we must also
			// include:
			//
			String subcode = "61"; // default: vt100
			String code = "1;2c"; // default: AVO

			if (terminalID.equalsIgnoreCase("vt320")) {
				subcode = "63;";
			} else if (terminalID.equalsIgnoreCase("vt220")
					|| terminalID.equalsIgnoreCase("linux")) {
				subcode = "62;";
			} else if (terminalID.equalsIgnoreCase("ansi")) {
				code = "1;0c";
				subcode = "";
			}
			daReply = ((char) ESC) + "[?" + subcode + code;
		}

		write(daReply, false);
		flush();

	}

	protected void setTermState(int term_state) {
		this.term_state = term_state;

	}

	/* hard reset the terminal */
	public void resetTerminal() {
		gx[0] = 'B';
		gx[1] = '0';
		gx[2] = 'B';
		gx[3] = 'B';
		gl = 0; // default GL to G0
		gr = 1; // default GR to G1

		// reset tabs
		resetTabs();
	}

	private int height, width; /* rows and columns */
	private boolean altPrefixesMeta;
	private boolean remoteMarginSet;

	/**
	 * Put a character on the screen with normal font and outline. The character
	 * previously on that position will be overwritten.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate (line)
	 * @param ch
	 *            the character to show on the screen
	 * @see #insertChar
	 * @see #deleteChar
	 */
	public void putChar(int c, int l, char ch) {
		putChar(c, l, ch, NORMAL);
	}

	/**
	 * Put a character on the screen with specific font and outline. The
	 * character previously on that position will be overwritten.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate (line)
	 * @param ch
	 *            the character to show on the screen
	 * @param attributes
	 *            the character attributes
	 * @see #BOLD
	 * @see #UNDERLINE
	 * @see #INVERT
	 * @see #NORMAL
	 * @see #insertChar
	 * @see #deleteChar
	 */
	public void putChar(int c, int l, char ch, long attributes) {

		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);
		terminalData[screenBase + l][c] = (attributes << 32) | ch;
		setLineDirty(screenBase + l);
	}

	/**
	 * Get the character at the specified position.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate viewport/window (line)
	 * @see #putChar
	 */
	public char getChar(int c, int l) {
		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);
		return (char) (terminalData[screenBase + l][c]);
	}

	/**
	 * Get the attributes for the specified position.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate of viewport/window (line)
	 * @return character attributes for specified location
	 * @see #putChar
	 */
	public int getAttributes(int c, int l) {
		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);
		return (int) ((terminalData[screenBase + l][c] >> 32));
	}

	/**
	 * Insert a character at a specific position on the screen. All character
	 * right to from this position will be moved one to the right.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate viewport/window (line)
	 * @param ch
	 *            the character to insert
	 * @param attributes
	 *            the character attributes
	 * @see #BOLD
	 * @see #UNDERLINE
	 * @see #INVERT
	 * @see #NORMAL
	 * @see #putChar
	 * @see #deleteChar
	 */
	private void insertChar(int c, int l, char ch, int attributes) {
		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);
		System.arraycopy(terminalData[screenBase + l], c,
				terminalData[screenBase + l], c + 1, width - c - 1);
		putChar(c, l, ch, attributes);
	}

	/**
	 * Delete a character at a given position on the screen. All characters
	 * right to the position will be moved one to the left.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate (line)
	 * @see #putChar
	 * @see #insertChar
	 */
	private void deleteChar(int c, int l) {
		// Logger.debug("emu.deleteChar: " + l);

		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);
		if (c < width - 1) {
			System.arraycopy(terminalData[screenBase + l], c + 1,
					terminalData[screenBase + l], c, width - c - 1);
		}
		putChar(width - 1, l, (char) 0);
	}

	/**
	 * Put a String at a specific position giving all characters the same
	 * attributes. Any characters previously on that position will be
	 * overwritten.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate (line)
	 * @param s
	 *            the string to be shown on the screen
	 * @param attributes
	 *            character attributes
	 * @see #BOLD
	 * @see #UNDERLINE
	 * @see #INVERT
	 * @see #NORMAL
	 * @see #putChar
	 * @see #insertLine
	 * @see #deleteLine
	 */
	// private void putString(int c, int l, String s, int attributes) {
	// for (int i = 0; i < s.length() && c + i < width; i++) {
	// putChar(c + i, l, s.charAt(i), attributes);
	// }
	// }

	/**
	 * Insert a blank line at a specific position. Scroll text according to the
	 * argument.
	 * 
	 * @param l
	 *            the y-coordinate to insert the line
	 * @param scrollDown
	 *            scroll down
	 * @see #deleteLine
	 * @see #SCROLL_UP
	 * @see #SCROLL_DOWN
	 */
	private void insertLine(int l, boolean scrollDown) {
		insertLine(l, 1, scrollDown);
	}

	/**
	 * Insert blank lines at a specific position. The current line and all
	 * previous lines are scrolled one line up. The top line is lost.
	 * 
	 * @param line
	 *            the y-coordinate to insert the line
	 * @param n
	 *            number of lines to be inserted
	 * @param scrollDown
	 *            scroll down
	 * @see #deleteLine
	 * @see #SCROLL_UP
	 * @see #SCROLL_DOWN
	 */
	private void insertLine(int line, int n, boolean scrollDown) {
		// Logger.debug("emu.insertLine: " + line + " / " + n + " / " +
		// scrollDown);

		line = checkBounds(line, 0, height - 1);

		long cbuf[][] = null;
		int offset = 0;
		int oldBase = screenBase;

		// We do not scroll below bottom margin (below the scrolling region).
		if (line > bottomMargin) {
			return;
		}
		int top = (line < topMargin ? 0
				: (line > bottomMargin ? (bottomMargin + 1 < height ? bottomMargin + 1
						: height - 1)
						: topMargin));
		int bottom = (line > bottomMargin ? height - 1
				: (line < topMargin ? (topMargin > 0 ? topMargin - 1 : 0)
						: bottomMargin));

		// System.out.println("l is " + l + ", top is " + top + ", bottom is " +
		// bottom +
		// "bottomargin is " + bottomMargin + ", topMargin is " + topMargin);
		if (scrollDown) {
			if (n > (bottom - top)) {
				n = (bottom - top);
			}
			cbuf = new long[bottom - line - (n - 1)][width];
			// WTF - why are we reallocating for a simple insert?
			System.arraycopy(terminalData, oldBase + line, cbuf, 0, bottom
					- line - (n - 1));
			System.arraycopy(cbuf, 0, terminalData, oldBase + line + n, bottom
					- line - (n - 1));
			cbuf = terminalData;
		} else {
			try {
				if (n > (bottom - top) + 1) {
					n = (bottom - top) + 1;
				}
				if (bufSize < maxBufSize) {
					// So what are we doing here...
					// if the buffer will exceed teh allowed size after the
					// insertion,
					// we need to push the topmost content of the buffer out
					// from
					// underneath.
					if (bufSize + n > maxBufSize) {
						offset = n - (maxBufSize - bufSize);
						bufSize = maxBufSize;
						screenBase = maxBufSize - height - 1;
						windowBase = screenBase;
					} else {
						screenBase += n;
						windowBase += n;
						bufSize += n;
					}
					// @todo - always allocate max buffsize
					// this way we can shift without a re-alloc/copy every time.
					// ? use a linked list for easy insertion and deletion of
					// lines?
					// the only drawback is that we often start at a non-zero
					// position - can maintain this with a topRow reference.

					cbuf = new long[bufSize][width];
				} else {
					offset = n;
					cbuf = terminalData;
				}
				// copy anything from the top of the buffer (+offset) to the new
				// top up to the screenBase.
				if (oldBase > 0) {
					System.arraycopy(terminalData, offset, cbuf, 0, oldBase
							- offset);
				}
				// copy anything from the top of the screen (screenBase) up to
				// the
				// topMargin to the new screen
				if (top > 0) {
					System.arraycopy(terminalData, oldBase, cbuf, screenBase,
							top);
				}
				// copy anything from the topMargin up to the amount of lines
				// inserted
				// to the gap left over between scrollback buffer and screenBase
				if (oldBase > 0) {
					System.arraycopy(terminalData, oldBase + top, cbuf, oldBase
							- offset, n);
				}
				for (int i = 0; i < line - top - (n - 1); i++) {
					cbuf[screenBase + top + i] = terminalData[oldBase + top + n
							+ i];
				}

				//
				// copy the all lines next to the inserted to the new buffer
				if (line < height - 1) {
					System.arraycopy(terminalData, oldBase + line + 1, cbuf,
							screenBase + line + 1, (height - 1) - line);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				Logger.error(e + " : " + e.getMessage());
				// (comment from Meissner, apparently)
				// this should not happen anymore, but I will leave the code
				// here in case something happens anyway. That code above is
				// so complex I always have a hard time understanding what
				// I did, even though there are comments
			}
		}

		// this is a little helper to mark the scrolling
		// @todo - how many times do we reallocate this w/in the one routine?!
		for (int i = 0; i < n; i++) {
			int add = (scrollDown ? i : -i);
			cbuf[(screenBase + line) + add] = new long[width];
		}

		terminalData = cbuf;

		if (scrollDown) {
			setLineDirty(line, bottom - line + 1);
		} else {
			setLineDirty(top, line - top + 1);
		}
	}

	/**
	 * Delete a line at a specific position. Subsequent lines will be scrolled
	 * up to fill the space and a blank line is inserted at the end of the
	 * screen.
	 * 
	 * @param l
	 *            the y-coordinate to insert the line
	 * @see #deleteLine
	 */
	private void deleteLine(int l) {
		// Logger.debug("emu.deleteLine: " + l);
		l = checkBounds(l, 0, height - 1);

		int bottom = (l > bottomMargin ? height - 1
				: (l < topMargin ? topMargin : bottomMargin + 1));
		System.arraycopy(terminalData, screenBase + l + 1, terminalData,
				screenBase + l, bottom - l - 1);
		terminalData[screenBase + bottom - 1] = new long[width];
		setLineDirty(l, bottom - l);
	}

	/**
	 * Clear the specified attribute for the given line and column. Line and
	 * column are assumed to be valid
	 * 
	 * @param line
	 *            a valid line at buffer (not viewport) level.
	 * @param col
	 *            a valid column.
	 * @param attribute
	 *            attribute to clear.
	 */
	private void clearAttribute(int line, int col, int attribute) {
		terminalData[line][col] &= (((~attribute) << 32) | 0xFFFFFFFF);
	}

	/**
	 * Clear the specified attribute for the given line and column. Line and
	 * column are assumed to be valid
	 * 
	 * @param line
	 *            a valid line at the buffer (not viewport) level.
	 * @param col
	 *            a valid column.
	 * @param attribute
	 *            attribute to set.
	 * @see #BOLD
	 * @see #INVERT
	 * @see #UNDERLINE
	 */
	private void setAttribute(int line, int col, int attribute) {
		terminalData[line][col] |= (attribute << 32);
	}

	/*
	 * private void setXAttr(int line, int col, int attribute) {
	 * terminalData[line][col] |= attribute; }
	 * 
	 * private void clearXAttr(int line, int col, int attribute) {
	 * terminalData[line][col] &= ~attribute; }
	 */
	/**
	 * Toggle the state of the specifeid attribute at the specified position.
	 * This requires valid inputs, as it performs no validation itself.
	 * 
	 * @param line
	 * @param col
	 * @param attribute
	 */
	private void toggleAttribute(int line, int col, int attribute) {
		if ((terminalData[screenBase + line][col] & (attribute << 32)) > 0) {
			clearAttribute(line, col, attribute);

		} else {
			setAttribute(line, col, attribute);
		}
	}

	/**
	 * Sets the provided attribute across multiple lines, in a wrapping fashion.
	 * That is, every character between startline,startcol and endline,endcol
	 * will be marked as selected/deselected. Lines that fall in between
	 * startline and endline will be selected /deselected in full.
	 * 
	 * @param startLine
	 *            line to begin toggle, WRT the viewport.
	 * @param startCol
	 *            starting column of region, within startLine
	 * @param endLine
	 *            last line to toggle, WRT the viewport.
	 * @param endCol
	 *            ending column to toggle, within endLine
	 * @param attribute
	 *            the attribute to reverse
	 * @see #setAttribute
	 * @see #clearAttribute
	 * @author mparadise
	 */
	public void toggleAttributeStateForRange(int startLine, int startCol,
			int endLine, int endCol, int attribute) {
		// @todo for performance, might a vector be better for lines only?
		// This would prevent a lot of array copies when we scroll/move
		startLine = checkBounds(startLine, 0, height - 1);
		endLine = checkBounds(endLine, 0, height - 1);
		startCol = checkBounds(startCol, 0, width - 1);
		endCol = checkBounds(endCol, 0, width - 1);
		int lineStep = endLine < startLine ? -1 : 1;
		int colStep = endCol < startCol ? -1 : 1;

		// Sanity check - don't allow invalid selection range.
		if (startLine == endLine) {
			int line = screenBase + startLine;
			for (int col = startCol; col < endCol; col += colStep) {
				toggleAttribute(line, col, attribute);
			}
		} else {
			int stopLine = endLine + screenBase;
			for (int line = startLine + screenBase; line < stopLine; line += lineStep) {
				int stopCol;
				int beginCol;
				if (line == startLine) {
					stopCol = width;
					beginCol = startCol;
				} else if (line == stopLine) {
					stopCol = endCol;
					beginCol = 0;
				} else {
					stopCol = width;
					beginCol = 0;
				}
				for (int col = beginCol; col < stopCol; col += colStep) {
					toggleAttribute(line, col, attribute);
				}
			}
		}
		setLineDirty(startLine, endLine - startLine);
	}

	/**
	 * Returns a StringBuffer containing characters that have the specified
	 * attribute(s).
	 * 
	 * @param attribute
	 *            attribute(s) to check for.
	 * @param addNewlines
	 *            if true, a newline will be appended to the return string every
	 *            time a character included is on a different line than the last
	 *            character included.
	 * @return a string containing all characters with the specified attribute.
	 * @author mparadise
	 */
	public synchronized String getCharactersWithAttributes(long attribute,
			boolean addNewlines) {
		// Over-allocate string buffer to contain the matching chars region,
		// with a newline separating, on the theory that it's better to
		// allocate too much than not enough (and have to do multiple
		// array copies internally to the stringbuffer.

		// Note that we're copying the visiible vertical range only.
		StringBuffer output = new StringBuffer((width + 1) * height);
		attribute = attribute << 32;
		int last = -1;
		int max = Math.min(windowBase + height, bufSize);
		for (int y = screenBase; y < max; y++) {
			for (int x = 0; x < width; x++) {
				if ((terminalData[y][x] & attribute) > 0) {
					// Append newline for each gap between lines
					// that have matching attributes.
					if (addNewlines) {
						if (last == -1) {
							last = y;
						} else if (last != y) {
							output.append("\n");
							last = y;
						}
					}
				}

				output.append((char) (terminalData[y][x]));

			}
		}
		return output.toString();
	}

	/**
	 * Delete a rectangular portion of the screen.
	 * 
	 * @param c
	 *            x-coordinate (column)
	 * @param l
	 *            y-coordinate (row)
	 * @param w
	 *            with of the area in characters
	 * @param h
	 *            height of the area in characters
	 * @param curAttr
	 *            attribute to fill
	 * @see #deleteChar
	 * @see #deleteLine
	 */
	private void deleteArea(int c, int l, int w, int h, long curAttr) {
		// Logger.debug("emu.deleteArea: " + c + " " + l + " " + w + " " + h);

		c = checkBounds(c, 0, width - 1);
		l = checkBounds(l, 0, height - 1);

		long attr = curAttr << 32;

		long cbuf[] = new long[w];
		for (int i = 0; i < w; i++) {
			cbuf[i] = attr;
		}
		for (int i = 0; i < h && l + i < height; i++) {
			System.arraycopy(cbuf, 0, terminalData[screenBase + l + i], c, w);
		}
		setLineDirty(l, h);
	}

	/**
	 * Sets whether the cursor is visible or not.
	 * 
	 * @param doshow
	 */
	private void showCursor(boolean doshow) {
		// Logger.debug("emu.showCursor: " + doshow);
		if (doshow != showcursor) {
			setLineDirty(cursorY, 1);
		}
		showcursor = doshow;
	}

	/**
	 * Puts the cursor at the specified position , assuming the values will be
	 * interpreted as relative to the current viewport.
	 * 
	 * @param c
	 *            column
	 * @param l
	 *            line
	 */
	private void setCursorPosition(int c, int l) {
		if (l != cursorY || c != cursorX)
			setLineDirty(cursorY, 1);
		cursorX = checkBounds(c, 0, width - 1);
		cursorY = checkBounds(l, 0, height - 1);
		setLineDirty(cursorY, 1);
	}

	/**
	 * @return total columns
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Set the top scroll margin for the screen. If the current bottom margin is
	 * smaller it will become the top margin and the line will become the bottom
	 * margin.
	 * 
	 * @param l
	 *            line that is the margin
	 */
	public void setTopMargin(int l) {
		// Logger.debug("emu.setTopMargin: " + l);
		if (l > bottomMargin) {
			topMargin = bottomMargin;
			bottomMargin = l;
		} else {
			topMargin = l;
		}
		if (topMargin < 0) {
			topMargin = 0;
		}
		if (bottomMargin > height - 1) {
			bottomMargin = height - 1;
		}

	}

	/**
	 * Get the top scroll margin.
	 */
	public int getTopMargin() {
		return topMargin;
	}

	/**
	 * Set the bottom scroll margin for the screen. If the current top margin is
	 * bigger it will become the bottom margin and the line will become the top
	 * margin.
	 * 
	 * @param l
	 *            line that is the margin
	 */
	public void setBottomMargin(int l, boolean remote) {
		// Logger.debug("emu.setBottomMargin: " + l);

		if (l < topMargin) {
			bottomMargin = topMargin;
			topMargin = l;
		} else {
			bottomMargin = l;
		}
		if (topMargin < 0) {
			topMargin = 0;
		}
		if (bottomMargin > height - 1) {
			bottomMargin = height - 1;
		}
		remoteMarginSet = remoteMarginSet || remote;
	}

	/**
	 * Get the bottom scroll margin.
	 */
	private int getBottomMargin() {
		return bottomMargin;
	}

	public void setScrollbackBufferSize(int scrollbackSize) {
		if (scrollbackSize < 1)
			return;

		setBufferSize(scrollbackSize + height);
	}

	/**
	 * @param amount
	 *            new size of the buffer
	 */
	public void setBufferSize(int amount) {
		Logger.debug("emu.setBufferSize: " + amount);

		if (amount < height) {
			amount = height;
		}
		if (amount < maxBufSize) {
			long cbuf[][] = new long[amount][width];
			int copyStart = bufSize - amount < 0 ? 0 : bufSize - amount;
			int copyCount = bufSize - amount < 0 ? bufSize : amount;
			if (terminalData != null) {
				System.arraycopy(terminalData, copyStart, cbuf, 0, copyCount);
			}
			terminalData = cbuf;
			bufSize = copyCount;
			screenBase = bufSize - height;
			windowBase = screenBase;
		}
		maxBufSize = amount;
		numScrollbackLines = maxBufSize - height;

	}

	/**
	 * Retrieve current scrollback buffer size.
	 * 
	 * @see #setBufferSize
	 */
	public int getBufferSize() {
		return bufSize;
	}

	/**
	 * Retrieve maximum buffer Size.
	 * 
	 * @see #getBufferSize
	 */
	public int getMaxBufferSize() {
		return maxBufSize;
	}

	/*
	 * private void dumpKeyData(String message) { System.out.println(message);
	 * System.out.println("      bufSize: " + bufSize);
	 * System.out.println("   maxBufSize: " + maxBufSize);
	 * System.out.println("   screenBase: " + screenBase);
	 * System.out.println("   windowBase: " + windowBase);
	 * System.out.println("        width: " + width);
	 * System.out.println("       height: " + height);
	 * System.out.println("    topMargin: " + topMargin);
	 * System.out.println(" bottomMargin: " + bottomMargin);
	 * System.out.println("          ROW: " + ROW);
	 * System.out.println("          COL: " + COLUMN);
	 * 
	 * }
	 */

	/**
	 * Change the size of the screen. This will include adjustment of the
	 * scrollback buffer.
	 * 
	 * @param w
	 *            of the screen
	 * @param h
	 *            of the screen
	 * @param updateRemote
	 *            if true indicate that we should request a remote resizing.
	 */
	public void setScreenSize(int w, int h, boolean updateRemote) {
		Logger.debug("emu.setScreenSize: " + w + " / " + h);
		// dumpKeyData("setScreenSize: PRE RESIZE");
		long cbuf[][];
		int bsize = bufSize;
		if (w < 1 || h < 1) {
			return;
		}
		if (w == width && h == height) {
			Logger.debug("emu.setScreenSize: no change, not resizing");
			return;
		}
		if (h > maxBufSize) {
			maxBufSize = h;
		}

		if (h > bufSize) {
			bufSize = h;
			screenBase = 0;
			windowBase = 0;
		}

		// int diff = screenBase - windowBase;

		if (windowBase + h >= bufSize)
			windowBase = bufSize - h;

		if (screenBase + h >= bufSize)
			screenBase = bufSize - h;

		// @todo - this is my attempt at cleaning up artifacts when we have a
		// smaller
		// screen ... of coruse this won't work, the artifacts are
		// Also, what happens when our screen gets resized to below our margins
		// - margins must move up.
		// but we have to look at the response to a resize - do we handle that,
		// or doe sthe remote host tell us our new
		// margins?
		// if (screenBase + h >= bufSize) {
		// screenBase = bufSize - h;
		// } else {
		// // Put the screen base in the same position relative to the bottom
		// // fo the buffer that it used to be.
		// screenBase = bufSize - (bsize - screenBase);
		// }
		//
		// if (windowBase + h >= bufSize) {
		// windowBase = bufSize - h;
		// } else {
		// // Put the window base in the same relative position to the
		// // screenbase that it was.
		// windowBase = screenBase - diff;
		// }

		cbuf = new long[bufSize][w];

		int len = w < width ? w : width;

		if (terminalData != null) {
			for (int i = 0; i < bsize && i < bufSize; i++) {
				System.arraycopy(terminalData[i], 0, cbuf[i], 0, len);
				terminalData[i][0] |= XATTR_DIRTY;
			}
			long[] empty = new long[w];
			// Mark the remaining un-used lines dirty as well - and clear them
			// out sot aht they don't remain on screen.
			for (int i = bsize; i < bufSize; i++) {
				System.arraycopy(empty, 0, cbuf[i], 0, w);
				cbuf[i][0] |= XATTR_DIRTY;

			}
		}

		terminalData = cbuf;

		// Don't send an update unless we actually change dimensions.
		updateRemote = updateRemote && (width != w || height != h);

		// Okay - if height changes, we need to modify our bottom margin
		// so that it keeps a relative position that's the same
		width = w;
		height = h;
		topMargin = 0;

		// First reset our top margin. The new screen dimensions could run afoul
		// of other limts - this makes sure those boundaries get applied
		setTopMargin(topMargin);

		// Similarly - if we have not already set up the bottom margin, we'll do
		// that now; but
		// if we hae alrady set it we can't adjust it -- because the remote
		// client wouldn't know
		// about the adjustment., Instead, we'll re-set it to its current value
		// -- this will
		// ensure that we check to make sure it's within boundaries. (Such as
		// when the terminal size is smaller
		// than it was.)
		if (remoteMarginSet) {
			setBottomMargin(bottomMargin, false);
		} else {
			// if we neever set margin remotely, we're maintaining it manualy
			// until then,.
			bottomMargin = height - 1;
		}

		// Force a refresh
		if (updateRemote) {
			resize();
		}
		// dumpKeyData("setScreenSize: POST RESIZE");

	}

	/**
	 * Mark lines to be updated by the renderer
	 * 
	 * @param l
	 *            starting line
	 * @param n
	 *            amount of lines to be updated
	 */
	public void setLineDirty(int l, int n) {
		// @todo by expanding attributes to long, we can just use
		// a attribute here and save the extra array.
		// Or by using a Character class... either way will give us
		// character-level dirty controls.
		l = checkBounds(l, 0, height - 1);
		int max = Math.min(l + n, height);

		for (int line = l; line < max; line++) {
			terminalData[line][0] |= XATTR_DIRTY;
		}

	}

	public boolean isLineDirty(int line) {
		line = checkBounds(line, 0, height - 1);
		return (terminalData[line][0] & XATTR_DIRTY) > 0;
	}

	public void setLineDirty(int line) {

		line = checkBounds(line, 0, height - 1);
		terminalData[line][0] |= XATTR_DIRTY;
	}

	public void setLineClean(int line) {
		int newline = line = checkBounds(line, 0, height - 1);
		terminalData[newline][0] &= (~XATTR_DIRTY);
	}

	private int checkBounds(int value, int lower, int upper) {
		if (value < lower) {
			return lower;
		}
		if (value > upper) {
			return upper;
		}
		return value;
	}

	public int getTerminalWidth() {
		return width;
	}

	public int getTerminalHeight() {
		return height;
	}

	/**
	 * By default we use VT100 settings for function keys - which use a
	 * different code for F1-F4. However, many other emulations use a different
	 * set that's more consistent with F5-F12. This will enable that
	 * alternative.
	 */
	public void setFunctionKeyMode(int mode) {
		switch (mode) {
		case FK_VT100:
			FunctionKey[1] = "\u001bOP";
			FunctionKey[2] = "\u001bOQ";
			FunctionKey[3] = "\u001bOR";
			FunctionKey[4] = "\u001bOS";
			break;
		case FK_LINUX_APP_KEYPAD:
			FunctionKey[1] = "\u001b[11~";
			FunctionKey[2] = "\u001b[12~";
			FunctionKey[3] = "\u001b[13~";
			FunctionKey[4] = "\u001b[14~";
			break;

		case FK_LINUX:
			FunctionKey[1] = "\u001b[[A";
			FunctionKey[2] = "\u001b[[B";
			FunctionKey[3] = "\u001b[[C";
			FunctionKey[4] = "\u001b[[D";
			break;
		}
	}

	public void enableAltSendsMeta() {
		altPrefixesMeta = true;
	}

	public String getBufferString() {
		StringBuffer output = new StringBuffer((width + 1) * (height + 1));
		char c;
		int max = Math.min(windowBase + height, bufSize);
		for (int y = windowBase; y < max; y++) {
			for (int x = 0; x < width; x++) {
				c = (char) terminalData[y][x];
				if (c < ' ')
					continue;
				output.append(c);
			}
			// As long as this isn't a soft-wrapped line, insert a newline
			if ((terminalData[y][width - 1] & XATTR_WRAP) == 0)
				output.append("\n");

		}
		return output.toString();
	}

	/**
	 * Invoked when dimensions have changed Temporarily kludge perhaps...
	 */
	public abstract void resize();

	/**
	 * Synchronize on this mutex when performing read operations out of the
	 * terminal backing store.
	 * 
	 * @return mutex object
	 */
	public Object getTermBufferMutex() {
		return this.termBufferMutex;
	}

	public void refreshCursorPosition() {
		setCursorPosition(COLUMN, ROW);
	}

	public void terminate() {
		// Perform any cleanup.
	}

}
