package org.bbssh.ui.components;

import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

/**
 * Screen that will automatically display and capture the results of the Symbols page.
 * 
 * Invoke captureSymbol (blocking call) to use.
 */
public class SymbolMenuCaptureScreen extends PopupScreen implements FieldChangeListener {
	private SymbolMenuCaptureField captureField = new SymbolMenuCaptureField();
	private String capturedText;
	private static SymbolMenuCaptureScreen me;

	/**
	 * Constructor Note that key binding manager must have been initialized prior to invoking this.
	 * 
	 */
	public SymbolMenuCaptureScreen() {
		super(new VerticalFieldManager());
		add(captureField);
		captureField.setListener(this);
	}

	/**
	 * When this screen is actually displayed, immediately begin the symbol capture.
	 */
	protected void onUiEngineAttached(boolean attached) {
		super.onUiEngineAttached(attached);

		try {
			if (attached) {
				captureField.captureSymbol(getMenu(0));
			}

		} catch (ControlledAccessException e) {
			// In OS 6.0 on 9700 only, the call above gives a ControlledAccessException due to missing
			// RRI signature - however RRI is not required and so the signature is not applied.
			// we have no way to correct this, but we can at least prevent it from hanging the app.
			// This does seem to be a bug specific to an OS6 build

			// Because of this we have no way to show the symbol screen - tell the user and close.
			Logger.fatal("ControlledAccessException in getMenu", e);
			Status.show(Tools.getStringResource(BBSSHResource.SYMBOL_CAPTURE_ERROR));
			close();

		}

	}

	/**
	 * Display symbol menu and capture the result.
	 */
	public static String captureSymbol() {
		if (me == null) {
			me = new SymbolMenuCaptureScreen();
		}

		UiApplication.getUiApplication().pushModalScreen(me);
		String temp = me.capturedText;
		me.capturedText = "";
		return temp;

	}

	/**
	 * Receive notification when symbol capture is complete and close us out of our modal state.
	 */
	public void fieldChanged(Field field, int context) {
		if (field == captureField) {
			capturedText = captureField.getText();
			close();
		}

	}
	public int getPreferredWidth() {
		return 5; 
	}
}
