package org.bbssh.ui.components;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import org.bbssh.ui.components.ClickableButtonField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;

/**
 * Embeddedable class that encapsulates OK/Cancel pushbutton pair. Notifies the registered listener when either button
 * is pressed; context indicates whether OK or Cancel was pressed (see CONTEXT_* consts)
 * 
 * @author marc
 * 
 */
public class OKCancelControl extends VerticalFieldManager implements FieldChangeListener {
	/** Value passed into fieldChanged of registered listener when user presses OK */
	public static final int CONTEXT_OK_PRESS = 100;
	/** Value passed into fieldChanged of registered listener when user presses Cancel */
	public static final int CONTEXT_CANCEL_PRESS = 101;

	ResourceBundle res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	ClickableButtonField okButton = new ClickableButtonField();
	ClickableButtonField cancelButton = new ClickableButtonField();

	/**
	 * Constructor for OKCancelControl
	 */
	public OKCancelControl() {
		okButton.setLabel(res.getString(BBSSHResource.GENERAL_LBL_OK));
		cancelButton.setLabel(res.getString(BBSSHResource.GENERAL_LBL_CANCEL));
		okButton.setChangeListener(this);
		cancelButton.setChangeListener(this);
		HorizontalFieldManager hfm = new HorizontalFieldManager();
		hfm.add(okButton);
		hfm.add(cancelButton);
		add(new SeparatorField());
		add(hfm);
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		if (field == okButton) {
			getChangeListener().fieldChanged(this, CONTEXT_OK_PRESS);
		} else if (field == cancelButton) {
			getChangeListener().fieldChanged(this, CONTEXT_CANCEL_PRESS);
		}

	}
	/**
	 *  The user pressing one of our buttons should never set state to dirty .
	 * @see net.rim.device.api.ui.Manager#isDirty()
	 */
	public boolean isDirty() {
		return super.isDirty();
	}

}
