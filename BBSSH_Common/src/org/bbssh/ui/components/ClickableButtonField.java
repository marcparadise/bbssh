package org.bbssh.ui.components;

import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;

/**
 * A simple ClickableButtonField extension which defaults to constructing with the CONSUME_CLICK property -- simply sot hat we
 * don't have to specify that property each and every time we use a ClickableButtonField.
 * 
 * @author marc
 * 
 */
public class ClickableButtonField extends ButtonField {
	public ClickableButtonField(FieldChangeListener listener) {
		super(CONSUME_CLICK);
		setChangeListener(listener);
	}
	public ClickableButtonField(String text, FieldChangeListener listener) {
		super(text, CONSUME_CLICK);
		setChangeListener(listener);
	}
	public ClickableButtonField() {
		super(CONSUME_CLICK);
	}

	public ClickableButtonField(long style) {
		super(CONSUME_CLICK | style);
	}

	public ClickableButtonField(String text) {
		super(text, CONSUME_CLICK);
	}

	public ClickableButtonField(String text, long style) {
		super(text, CONSUME_CLICK | style);
	}

}
