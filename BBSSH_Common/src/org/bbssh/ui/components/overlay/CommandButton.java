package org.bbssh.ui.components.overlay;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.BoundCommand;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ui.components.SimpleButtonField;

/**
 * A special button type that will execute a bound command upon click.
 * 
 * @author marc
 * 
 */

public class CommandButton extends SimpleButtonField {
	
	BoundCommand command;
	String origText;
	String newText;

	public CommandButton(String label, FieldChangeListener listener, BoundCommand command) {
		super(label, FIELD_HCENTER);
		origText = label;
		setFont(getFont().derive(Font.BOLD));
		setChangeListener(listener);
		this.command = command;

	}

	public CommandButton(String label, FieldChangeListener listener, BoundCommand command, String textWhenFocused) {
		super(label, 0);
		origText = label;
		newText = textWhenFocused;
		this.command = command;
		setFont(getFont().derive(Font.BOLD));

	}

	public CommandButton(String label, FieldChangeListener listener, BoundCommand command, String textWhenFocused,
			int vPadding) {
		this(label, listener, command, textWhenFocused);
		setPadding(vPadding, getHorizontalPadding());

	}

	public void onClicked() {
		RemoteSessionInstance session = SessionManager.getInstance().activeSession;
		// @todo - once again, UGH. Still need to clean up emulator/session/connection/terminal ...
		command.execute(session, true);
		// @todo if (command.isMultiExecutable or something? (eg, allow button to stay visible without hiding... ) <-
		super.onClicked();

	}

	protected void onFocus(int direction) {
		if (newText == null) {
			newText = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME).
					getString(command.getCommand().getNameResId());
		}
		((OverlayShortcutBar) getManager()).getDetailInfoLine().setText(newText);
		// setLabel(newText) -- instead of expanding our content , set the detail info bar of manager to have this data.
		super.onFocus(direction);
	}

	protected void onUnfocus() {
		((OverlayShortcutBar) getManager()).getDetailInfoLine().setText("");
		super.onUnfocus();
	}

	protected void layout(int maxWidth, int maxHeight) {
		// Use max avail width.
		setExtent(maxWidth, Math.min(super.getPreferredHeight(), maxHeight));
	}

}
