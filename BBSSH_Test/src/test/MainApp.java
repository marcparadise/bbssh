package test;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.FullScreen;
import net.rim.device.api.ui.picker.FilePicker;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionManager;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.model.Key;
import org.bbssh.model.SettingsManager;
import org.bbssh.net.session.SessionListener;
import org.bbssh.net.session.TestSession;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.terminal.VT320;
import org.bbssh.terminal.VT320Debug;
import org.bbssh.terminal.fonts.BBSSHFontManager;
import org.bbssh.terminal.fonts.FontInitializationFailedException;
import org.bbssh.ui.components.TerminalField;
import org.bbssh.ui.components.overlay.OverlayManager;
import org.bbssh.util.Logger;
import org.bbssh.util.Version;

/**
 * Simple class that displays MyScreen,w hcih youc an fill with whatever testing
 * content you need.
 */
public class MainApp extends UiApplication {
	/**
	 * Main entry point for test app.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MainApp app = new MainApp();
		try {
			BBSSHFontManager.initialize();
		} catch (FileNotFoundException e) {
			Logger.fatal("File not found.", e);
		} catch (FontInitializationFailedException e) {
			Logger.fatal("Font init failed.", e);
		}

		app.enterEventDispatcher();
	}

	/**
	 * App instance constructor
	 */
	public MainApp() {
		// / pushScreen(new OverlayTestScreen());
		pushScreen(new SimpleScreen());
	}

}

/**
 * Simple test screen. By default, takes an action when nav button is clicked.
 */
class SimpleScreen extends FullScreen implements SessionListener,
		FieldChangeListener {
	ResourceBundleFamily res = ResourceBundle.getBundle(
			BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);

	RemoteSessionInstance inst;
	TerminalField term = new TerminalField();
	// For now, hard-code a specific connection isntance to use for
	// our terminal settings.

	// Some basic setup
	SettingsManager sm = SettingsManager.getInstance();
	ConnectionManager cm = ConnectionManager.getInstance();
	boolean stop = false;
	boolean running = false;

	private final MenuItem start = new MenuItem(res,
			BBSSHResource.MENU_CONNECT, 0x00100000, 0) {
		public void run() {
			if (running)
				return;
			new Thread(termpump).start();
		}
	};
	private final MenuItem exit = new MenuItem("Exit", 0x00100000, 1) {
		public void run() {
			stop = true;
			UiApplication.getUiApplication().popScreen(SimpleScreen.this);
		}
	};
	private final MenuItem halt = new MenuItem(res,
			BBSSHResource.MENU_DISCONNECT, 0x00100000, 1) {
		public void run() {
			if (!running)
				return;
			running = false;
			stop = true;
		}
	};

	private OverlayManager overlayManager;
	private DataInputStream indexIn;
	private DataInputStream dataIn;
	FileConnection fconn;
	RemoteSessionInstance rsi = new RemoteSessionInstance();

	public void close() {
		closeStuff();
	}

	Runnable termpump = new Runnable() {
		protected boolean utf8;

		private short speedFactor = 10;
		String fileName;
		long offset;

		// private long startTime;

		private ConnectionProperties readProperties() throws IOException {
			byte event;
			ConnectionProperties prop = new ConnectionProperties(false);
			prop.setName("Session Replay");
			while ((event = indexIn.readByte()) != VT320Debug.EVENT_INIT_DONE) {
				switch (event) {
				case VT320Debug.EVENT_TERMATTR_COLORS:
					prop.setBackgroundColorIndex(indexIn.readInt());
					prop.setForegroundColorIndex(indexIn.readInt());
					break;
				case VT320Debug.EVENT_TERMATTR_FONT:
					prop.setFontSettings(new FontSettings(indexIn.readByte(),
							indexIn.readByte(), indexIn.readByte()));
					break;
				case VT320Debug.EVENT_MARKER:
					// startTime = indexIn.readLong();
					break;
				}

			}
			// params to init done that we don't use.
			utf8 = indexIn.readInt() == 1;
			indexIn.readInt();
			return prop;
		}

		public void run() {
			running = true;
			UiApplication.getUiApplication().invokeAndWait(new Runnable() {
				public void run() {
					FilePicker picker = FilePicker.getInstance();
					picker.setPath("file:///SDCard/");
					picker.setFilter(".txt");

					fileName = picker.show();

					if (fileName == null) {
						running = false;

					}

					// updateLayout();

				}
			});

			if (!running)
				return;
			// sloppy sloppy...
			try {
				String indexName = fileName.substring(0,
						fileName.lastIndexOf('.') + 1)
						+ "idx";
				FileConnection fconn = (FileConnection) Connector
						.open(indexName);
				indexIn = fconn.openDataInputStream();
				fconn.close();
				fconn = (FileConnection) Connector.open(fileName);
				dataIn = fconn.openDataInputStream();
				fconn.close();
				ConnectionProperties prop = readProperties();
				rsi.state = new TerminalStateData(prop);
				rsi.session = new TestSession(prop, 0,
						(SessionListener) SimpleScreen.this);
				SessionManager.getInstance().activeSession = rsi;
				// @todo why do we not use a single emulator source?
				rsi.emulator = rsi.session.getEmulator();
				try {
					term.attachInstance(rsi);
				} catch (FontNotFoundException e1) {
					Logger.error("Unable to load font.");
					return;

				}
			} catch (Exception e) {
				running = false;
				System.out.println("Abort due to IOException: "
						+ e.getMessage());
				closeStuff();
				return;
			}

			int delay;
			int count = 0;
			long last = System.currentTimeMillis();
			try {
				VT320 emulator = rsi.session.getEmulator();
				while (!stop) {
					switch (indexIn.readByte()) {
					case VT320Debug.EVENT_COMMENT:
						term.showExpiringMessage(indexIn.readUTF());
						continue;
					case VT320Debug.EVENT_MARKER:
						term.showExpiringMessage("User Mark @ " + offset + " ("
								+ count + ")");
						// @todo auto pause on mark option. or readahead and
						// auto slow donw on approach?
						continue;

					case VT320Debug.EVENT_TERMATTR_FONT:
						// @todo clean this up in main app - fs should be RSI
						// level perhaps? not state? Why seprate
						// updateFontSettings w/ arg?
						rsi.state.fs = new FontSettings(indexIn.readByte(),
								indexIn.readByte(), indexIn.readByte());
						term.updateFontSettings(rsi.state.fs);
						continue;

					case VT320Debug.EVENT_TERMATTR_SIZE:
						emulator.setScreenSize(indexIn.readInt(),
								indexIn.readInt(), false);
						continue;
					case VT320Debug.EVENT_TERMATTR_SCROLLBACK:
						emulator.setScrollbackBufferSize(indexIn.readInt());
						continue;
					case VT320Debug.EVENT_TERMATTR_TYPE:
						emulator.setTerminalID(indexIn.readUTF());
						continue;
					case VT320Debug.EVENT_DATA:
						break;

					}
					count++;
					delay = indexIn.readInt();
					offset += delay;

					if (delay > 150 || System.currentTimeMillis() - last > 500) {
						last = System.currentTimeMillis();

						// safe bet that if 150 ms pass w/ no input, we're
						// pausing long enough that a csreen update would have
						// occurred normally.
						emulator.refreshCursorPosition();
						term.redraw(false);

					} else if (delay > speedFactor) {
						try {
							Thread.sleep(delay / speedFactor);
						} catch (InterruptedException e) {
							stop = true;
							System.out
									.println("Unexpected interruption, aborting.");
							return;
						}
					}

					emulator.putChar(
							utf8 ? dataIn.readChar() : (char) dataIn.read(),
							false);

				}
				emulator.refreshCursorPosition();
				term.redraw(false);
			} catch (EOFException e) {
				Logger.info("Completed playback.");
			} catch (IOException e) {
				Logger.fatal("Unexpected ioexception.");
			} catch (FontNotFoundException e) {
				Logger.fatal("Changed font, but not not valid.");
			} finally {
				closeStuff();
				running = false;

			}

			// Close the terminal
		}
	};

	public SimpleScreen() {
		super(new OverlayManager(), DEFAULT_MENU);

		String name = TerminalField.class.getName();
		term = (TerminalField) Version.createOSObjectInstance(name);
		overlayManager = (OverlayManager) getDelegate();
		overlayManager.setChangeListener(this);
		overlayManager.setCentralField(term);
	}

	protected void makeMenu(net.rim.device.api.ui.component.Menu menu,
			int instance) {
		super.makeMenu(menu, instance);
		menu.add(halt);
		menu.add(start);
		menu.add(exit);
	}

	public boolean isDirty() {
		return false;
	}

	protected void sublayout(int width, int height) {
		super.sublayout(width, height);
		term.sizeChanged(false);

	}

	public void onSessionConnected(int sessionId) {

	}

	public void onSessionDisconnected(int sessionId, int bytesWritten,
			int bytesRead) {

	}

	public void onSessionError(int sessionId, String errorMessage) {

	}

	public void onSessionRemoteAlert(int sessionId) {

	}

	public String getKeyPassword(int sessionId, Key key) {
		return null;
	}

	public void onDisplayDirty(int sessionId) {
		term.redraw(false);

	}

	public void onDisplayInvalid(int sessionId) {
		term.redraw(true);

	}

	public void fieldChanged(Field arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	protected void closeStuff() {
		try {
			if (dataIn != null)
				dataIn.close();
			dataIn = null;
		} catch (IOException e) {
		}

		try {
			if (indexIn != null)
				indexIn.close();
			indexIn = null;
		} catch (IOException e) {
		}

	}
}
