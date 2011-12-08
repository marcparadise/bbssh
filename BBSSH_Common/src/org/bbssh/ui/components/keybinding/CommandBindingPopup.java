package org.bbssh.ui.components.keybinding;

import java.io.IOException;

import javax.microedition.lcdui.Font;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.command.CommandConstants;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.keybinding.TerminalKey;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.MacroManager;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.util.Tools;

/**
 * This popup screen allows the caller to select a command to bind to a key. It will display the help text for the
 * command and allow the user to enter a parameter if required.
 */
public class CommandBindingPopup extends PopupScreen implements FieldChangeListener {
	private ResourceBundleFamily res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	/**
	 * This member is intended to accurately reflect true if we've made a change <i>during the current edit</i> while
	 * this dialog is displayed. Conversely, member state.changesSaved tracks whether changes need to be committed to
	 * the persistent store, and is managed at the screen level.
	 */
	// during the current edit; while state.changesSaved will reflect if the binding has changed at all
	// since the last save of key bindings.
	private boolean changeSaved;
	boolean firstRun;
	Object[] macros;
	ObjectChoiceField availCommands;
	private int titleId = 0;
	private Field currentParamField;
	private LabelField helpField = new LabelField();
	private BasicEditField paramEditField = new BasicEditField(res.getString(BBSSHResource.KEYBIND_LBL_PARAMETER), "");
	private BasicEditField paramNumericField = new BasicEditField(res.getString(BBSSHResource.KEYBIND_LBL_PARAMETER),
			"", 32, BasicEditField.FILTER_NUMERIC);
	private ObjectChoiceField paramChoiceField = new ObjectChoiceField(res
			.getString(BBSSHResource.KEYBIND_LBL_PARAMETER), new Object[] { "" });
	private ClickableButtonField saveChanges = new ClickableButtonField(res.getString(BBSSHResource.GENERAL_LBL_OK));
	private ClickableButtonField cancelChanges = new ClickableButtonField(res
			.getString(BBSSHResource.GENERAL_LBL_CANCEL));
	private KeybindState state;
	HorizontalFieldManager okCancelHFM;
	HorizontalFieldManager parameterContainer = new HorizontalFieldManager();

	// private boolean macroMode;

	public CommandBindingPopup(boolean macroMode) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), PopupScreen.DEFAULT_CLOSE);
		// This title text is used whenever this screen is displayed.
		titleId = macroMode ? BBSSHResource.KEYBIND_MACRO_EDIT_TITLE : BBSSHResource.KEYBIND_SHORTCUT_EDIT_TITLE;
		setFont(getFont().derive(Font.STYLE_PLAIN, 17));
		// this.macroMode = macroMode;
		ExecutableCommand[] commands;

		if (macroMode) {
			commands = KeyBindingManager.getInstance().getMacroActionCommands();
		} else {
			commands = KeyBindingManager.getInstance().getBindableCommands();
		}

		availCommands = new ObjectChoiceField(res.getString(BBSSHResource.KEYBIND_LBL_ACTION), commands);
		availCommands.setChangeListener(this);

		// @todo - use menus instead? Or replace with our standard component?
		okCancelHFM = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
		okCancelHFM.add(saveChanges);
		okCancelHFM.add(cancelChanges);
		saveChanges.setChangeListener(this);
		cancelChanges.setChangeListener(this);
		currentParamField = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		if (field == availCommands) {
			int sel = availCommands.getSelectedIndex();
			if (sel == -1)
				return;
			state.command = (ExecutableCommand) availCommands.getChoice(sel);
			// @todo - handling for command.isParameterOptional
			helpField.setText(res.getString(state.command.getDescriptionResId()));
			if (state.command.isParameterRequired()) {
				setupParameterField();
			} else { //if (currentParamField != null && currentParamField.getManager() != null) {
				parameterContainer.deleteAll();
			}
		} else if (field == saveChanges) {
			if (isDataValid()) {
				try {
					save();
				} catch (IOException e) {
				}
				close();
			}
		} else if (field == cancelChanges) {
			setDirty(false);
			changeSaved = false;
			close();
		}
	}

	/**
	 * This method creates and appropriately populates the required parameter for teh currently active command. At the
	 * moment, this is more than a little ugly - this screen is manually mapping each command to its permitted values
	 * and ranges. In the longer term, we will need to set up a proper object model around executablecommand.
	 * 
	 */
	private void setupParameterField() {
		parameterContainer.deleteAll();
		currentParamField = null;
		// @todo optional parameter support! case CommandConstants.INPUT_MODE:

		// note that we already filtered out commands that don't support params,
		// so those won't be listed here in this switch
		switch (state.command.getId()) {
			case CommandConstants.MOVEMENT_KEY:
				currentParamField = paramChoiceField;
				paramChoiceField.setChoices(KeyBindingHelper.getDirectionalTerminalKeys());
				if (firstRun && state.parameter != null && state.parameter instanceof Integer) {
					int key = ((Integer) state.parameter).intValue();
					int idx = KeyBindingHelper.getMovementTerminalKeyIndex(key);
					if (idx > -1) {
						paramChoiceField.setSelectedIndex(idx);
					}
				}
				break;
			case CommandConstants.RUN_MACRO:
				currentParamField = paramChoiceField;
				macros = Tools.vectorToArray(MacroManager.getInstance().getMacros());
				paramChoiceField.setChoices(macros);
				if (macros.length > 0 && firstRun && state.parameter != null && state.parameter instanceof String) {
					// @todo - this SHOULD work if .equals is being used ...
					paramChoiceField.setSelectedIndex(state.parameter);
				}
				break;
			case CommandConstants.SHOW_DEBUG_MESSAGE:
			case CommandConstants.SCROLL_DOWN_LINES:
			case CommandConstants.SCROLL_UP_LINES:
			case CommandConstants.WAIT:
			case CommandConstants.WAIT_FOR_ACTIVITY: // @todo support to have a separate numeric field?
				currentParamField = paramNumericField;
				if (firstRun && state.parameter != null) {
					paramNumericField.setText(state.parameter.toString());
				}
				break;
			case CommandConstants.SEND_TEXT:
				currentParamField = paramEditField;
				if (firstRun && state.parameter != null) {
					paramEditField.setText(state.parameter.toString());
				}
				break;
			case CommandConstants.SEND_TERMINAL_KEY:
				// @todo this should be modified to sendStandardKeyAtIndex...
				currentParamField = paramChoiceField;
				paramChoiceField.setChoices(KeyBindingHelper.getTerminalKeys());
				if (firstRun && state.parameter != null && state.parameter instanceof Integer) {
					int key = ((Integer) state.parameter).intValue();
					int idx = KeyBindingHelper.getTerminalKeyIndex(key);
					if (idx > -1) {
						paramChoiceField.setSelectedIndex(idx);
					}
				}
				break;

			case CommandConstants.INCDEC_FONT_SIZE:
				currentParamField = paramChoiceField;
				paramChoiceField.setChoices(KeyBindingHelper.getFontChangeChoices());
				if (firstRun && state.parameter != null && state.parameter instanceof Integer) {
					int action = ((Integer) state.parameter).intValue();
					if (action > -1) {
						paramChoiceField.setSelectedIndex(action);
					} else {
						paramChoiceField.setSelectedIndex(0);
					}
				}
				break;
			default:
				currentParamField = null;
				break;

		}
		// If the param field we're needing now isn't the same as the last time we displayed,
		// replace the old one with the correct field.

		if (currentParamField != null) {
			parameterContainer.add(currentParamField);
		}
		state.parameter = null;
	}

	/**
	 * Returns state tracking for the the key-binding being managed by this field.
	 * 
	 * @return state instance for key being managed
	 */
	public KeybindState getKeybindState() {
		return this.state;
	}

	// @todo - freeform text is a problem here: fucking virtual kbd in horizontal
	// mode makes it unusable.

	/**
	 * Updates this popup to reflect the specific key binding state provided. Invoking this will cause fields to
	 * refresh/repaint, so it is up to the caller to ensure that it's invoked from the UI thread.
	 * 
	 * @param state
	 */
	public void setKeybindState(KeybindState state) {
		this.state = state;
		LabelField title = new LabelField(res.getString(titleId) + state.bindingDescription);

		changeSaved = false;
		firstRun = true;
		// @todo - do we really need to deleteAll? Or just refresh values...
		deleteAll();
		add(title); // 0
		add(availCommands); // 1
		add(parameterContainer);
		currentParamField = null;
		paramChoiceField.setChoices(new Object[] {});
		paramEditField.setText("");
		paramNumericField.setText("");
		// 2 - we are inserting the parameter field here, when parameter field is needed.
		add(new SeparatorField()); // 3
		add(helpField);// 4
		add(okCancelHFM); // 5
		if (state.command == null) {
			availCommands.setSelectedIndex(0);
		} else {
			// this will trigger fieldChanged, which populates the rest of the screen as needed.
			// Hmm. Unless it's already set from a previous display.
			int old = availCommands.getSelectedIndex();
			availCommands.setSelectedIndex(state.command);
			// If the command hasn't changed from last time we displayed, then the change event won't
			// fire. Let's fire it manually.
			if (old == availCommands.getSelectedIndex()) {
				fieldChanged(availCommands, 1);

			}
		}
		firstRun = false;
	}

	/**
	 * Invoked by framework when there are chanegs to be saved, this implementation simply updates local member objects
	 * to reflect the new parameter. Owning screen is responsible for determining whether the actual changes are to be
	 * persisted.
	 * 
	 * also @see net.rim.device.api.ui.Screen#save()
	 */
	public void save() throws IOException {
		changeSaved = true;
		if (currentParamField instanceof ObjectChoiceField) {
			// Note tha we're not checking for -1 -- because isDataValid guarantees that
			// if we reach this point, we have a valid selection
			int index = paramChoiceField.getSelectedIndex();
			// Now we have to do the same mess as when the field changes - we need to map the
			// enterd value to the real underlying type.
			switch (state.command.getId()) {
				case CommandConstants.MOVEMENT_KEY:
				case CommandConstants.SEND_TERMINAL_KEY:
					if (index > -1) {
						state.parameter = ((TerminalKey) paramChoiceField.getChoice(index)).getValue();
					}
					break;
				case CommandConstants.RUN_MACRO:
					// Here our parameter is the name of the macro - so we can use the string object in the list.
					if (index > -1) {
						state.parameter = paramChoiceField.getChoice(index).toString();
					}
					break;
				case CommandConstants.INCDEC_FONT_SIZE:
					state.parameter = new Integer(index);
					break;

			}
		} else {
			switch (state.command.getId()) {
				case CommandConstants.SCROLL_DOWN_LINES:
				case CommandConstants.SCROLL_UP_LINES:
				case CommandConstants.WAIT:
				case CommandConstants.WAIT_FOR_ACTIVITY:
					state.parameter = Integer.valueOf(paramNumericField.getText());
					break;
				default:
					state.parameter = paramEditField.getText();
					break;
			}
			paramEditField.setText("");
			paramNumericField.setText("");
		}
		super.save();
	}

	/**
	 * returns true if a change has been made on this screen and it was not canceled.
	 * 
	 * @return true if change was saved.
	 */
	public boolean isChangeSaved() {
		return this.changeSaved;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.Screen#isDataValid()
	 */
	public boolean isDataValid() {

		if (state.command != null && !state.command.isParameterRequired()) {
			return true;
		}
		int messageId = 0;
		int len1 = paramEditField.getTextLength();
		int len2 = paramNumericField.getTextLength();
		int len = len1 + len2;
		if (state.command == null) {
			messageId = BBSSHResource.MSG_KEYBIND_NO_SELECTION;
		} else if (currentParamField instanceof BasicEditField) {
			// currently the only special handling in place is for "WAIT" -
			// this value must be numeric.
			if (len == 0) {
				messageId = BBSSHResource.KEYBIND_MSG_NO_PARAM;
			} else {
				int minValue = 0;
				switch (state.command.getId()) {
					case CommandConstants.WAIT:
						minValue = 1;
						// no break intentional - both fields use numeric validation.
					case CommandConstants.WAIT_FOR_ACTIVITY:
						try {
							if (Integer.parseInt(paramNumericField.getText()) < minValue) {
								throw new NumberFormatException();
							}
						} catch (NumberFormatException e) {
							// might happen if the number is too big for an int...
							messageId = BBSSHResource.KEYBIND_MSG_INVALID_MILLISECONDS;
						}
				}
			}
		} else {
			if (paramChoiceField.getSelectedIndex() < 0) {
				messageId = BBSSHResource.KEYBIND_MSG_NO_PARAM;
			}
		}

		if (messageId > 0) {
			Dialog.ask(Dialog.D_OK, res.getString(messageId));
			currentParamField.setFocus();
			return false;
		}
		return super.isDataValid();
	}
}
