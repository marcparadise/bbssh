package org.bbssh.command;

import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;

import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.ui.components.FileSelectorPopupScreen;

public class TakeScreenShot extends ExecutableCommand {

	String lastLocation;

	public int getId() {
		return CommandConstants.TAKE_SCREENSHOT;
	}

	public boolean execute(RemoteSessionInstance rsi, Object parameter) {
		if (rsi == null || rsi.backingStore == null)
			return false;
		FileSelectorPopupScreen fs = PlatformServicesProvider.getFileSelectorPopup();
		fs.setType(FileSelectorPopupScreen.TYPE_SAVE_AS);
		fs.setDefaultSaveAsFileName("screenshot.png");
		fs.setStartPath(lastLocation);
		String file = fs.pickFile();
		if (file == null || file.length() == 0)
			return true;
		if (!file.toLowerCase().endsWith(".png")) {
			file = file + ".png";
		}
		lastLocation = fs.getLocation();
		PNGEncodedImage img = PNGEncodedImage.encode(rsi.backingStore);
		try {
			FileConnection fc = (FileConnection) Connector.open(file);
			// Note we are not checking permissions, etc -- we'll rely on the thrown exception
			// to tell us what happened.
			if (!fc.exists()) {
				fc.create();
			}
			OutputStream o = fc.openOutputStream(0);
			o.write(img.getData());
			o.flush();
			o.close();
			fc.close();
			Status.show("Image saved.");
		} catch (ControlledAccessException e) {
			Dialog.ask(Dialog.D_OK, res.getString(MSG_NO_PERMISSIONS));
		} catch (Throwable e) {
			Dialog.ask(Dialog.D_OK, e.getMessage() + " (" + file + ")");
		}

		return true;

	}

	public int getDescriptionResId() {
		return CMD_DESC_TAKE_SCREENSHOT;
	}

	public int getNameResId() {

		return CMD_NAME_TAKE_SCREENSHOT;
	}

	public boolean isParameterRequired() {
		return false;
	}

	public boolean isKeyBindable() {
		return true;
	}

	public boolean isMacroAction() {
		return false;
	}

	public boolean isConnectionRequired() {
		return false;
	}

	public boolean isUILockRequired() {
		// We display a UI component and potentiall display a message. 
		return true; 
	}
}
