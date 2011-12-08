package org.bbssh.command;

import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.components.SymbolMenuCaptureScreen;
import org.bbssh.ui.screens.TerminalScreen;

/**
 * This command displays the symbol menu and captures the results via a special class
 * 
 */

public class PromptForAndSendSymbol extends SendText {
	public int getId() {
		return CommandConstants.SHOW_SYMBOLS;
	}

	/**
	 * This method will display a temporary screen containing only an edit field. It will then inject a menu request,
	 * then a request to show keys.
	 * 
	 */
	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		// When virtual keyboard is visible, the system does not provide us with a way to force it to show symbols.
		if (TerminalScreen.getInstance().isVirtualKeyboardVisible())
			return false;

		String sym = SymbolMenuCaptureScreen.captureSymbol();
		if (sym != null && sym.length() > 0) {
			// By using SendText, we also get proper handling
			// for modifier key state.
			return super.execute(rsi, sym);
		}
		return false;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SHOW_SYMBOL_MENU;
	}

	public int getNameResId() {
		return CMD_NAME_SHOW_SYMBOL_MENU;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isConnectionRequired() {
		return true;
	}

}
