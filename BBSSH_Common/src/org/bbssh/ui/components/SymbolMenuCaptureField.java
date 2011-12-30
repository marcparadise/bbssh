package org.bbssh.ui.components;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Menu;

/**
 * Specialized basic edit field that automatically invokes the Symbol menu on request. The using class must register as
 * a FieldChangeListener instance
 * 
 * @author marc
 * 
 */
public class SymbolMenuCaptureField extends BasicEditField implements FieldChangeListener {
	private boolean capturing = false;
	private FieldChangeListener listener;

	/**
	 * Creates a new object instance. Registers self as field change listener in order to filter only desired events to
	 * our caller, who also must register as a listener.
	 */
	public SymbolMenuCaptureField() {
		super();
		// Capture our own changes -t his will let us filter
		// what we pass on to whoever is using us.
		super.setChangeListener(this);

	}

	/**
	 * Displays the symbol menu and sets state to indicate capture is enabled. Symbol menu is captured by ID.
	 * 
	 * @todo testing is required to ensure that ID stays consistent across different platform versions.
	 */
	public void captureSymbol(Menu menu) {
		for (int x = menu.getSize() - 1; x >= 0; x--) {
			MenuItem it = menu.getItem(x);
			if (it != null) {
				// OS 7.0 - switched ID to 441 
				int id =it.getId() ; 
				if (id == 10065 || id == 441) {
					capturing = true;
					it.run();
				}
			}
		}
	}

	protected void onExposed() {
		super.onExposed();
		// This combination of events will occur only
		// when the user cancels from the symbol screen.

		if (capturing && getText().length() == 0) {
			capturing = false;
			if (listener != null) {
				// This will signal our parent that a change occurred -
				// teh special value "1" indicates that the user canceled,
				// and so parent should close.
				listener.fieldChanged(this, 1);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 * 
	 * This will delegate the fieldChanged event to the registered listener only if the field is presently capturing the
	 * symbol operation. Otherwise the event is discarded.
	 * 
	 * In any case, if the field is non-blank it will be cleared, as this field is not intended for use as an edit
	 * field.
	 */
	public void fieldChanged(Field field, int context) {
		if (capturing) {
			capturing = false;
			if (listener != null) {
				listener.fieldChanged(field, context);
			}
		}
		// Ensure that we never retain a value in this field.
		if (getText().length() > 0) {
			setText("");
		}
	}

	/**
	 * Workaround for a weird RIM bug. Orderinally we would override setFieldListener, bceause we want to filter the
	 * events that the registered listener receives. However, when we do that we find that the BB framework is invoking
	 * "setListener" *after* the owning class calls it, and it's passing in this field itself as the listener. My best
	 * guess is that because we call super.setFieldListener(this) in the constructor, someone along the line is calling
	 * oru setFieldChangeListener(((Field)x).getFieldChangeListener)
	 * 
	 * Which is fairly stupid, but it's hte only explanation so far that works...
	 * 
	 */
	public void setListener(FieldChangeListener listener) {
		this.listener = listener;
	}
	public int getPreferredWidth() {
		return 1;
	}

}
