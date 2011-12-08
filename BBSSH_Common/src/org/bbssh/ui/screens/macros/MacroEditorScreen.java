package org.bbssh.ui.screens.macros;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.PersistableCommandFactory;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.model.Macro;
import org.bbssh.model.MacroManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.components.BitmapButtonField;
import org.bbssh.ui.components.OKCancelControl;
import org.bbssh.ui.components.VectorListFieldCallback;
import org.bbssh.ui.components.keybinding.CommandBindingPopup;
import org.bbssh.ui.components.keybinding.KeybindState;
import org.bbssh.util.Tools;

public class MacroEditorScreen extends MainScreen implements FieldChangeListener, BBSSHResource {
	ResourceBundleFamily res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	private boolean saved = false;
	private CommandBindingPopup popup = new CommandBindingPopup(true);
	private Macro macro;
	private BasicEditField nameField;
	private MacroPartList partsList;
	private boolean editing;
	VectorListFieldCallback cb;
	// Even though this is a standard screen, we're still going to append "ok cancel"
	// control: easier fro touchscreen users...
	OKCancelControl okCancel;
	private BitmapButtonField moveUp, moveDown;
	private BitmapButtonField deletePart, editPart;
	private BitmapButtonField newPart;
	boolean ignoreNextListChange;

	// shortcuts: alt up/down - move up down
	// del - delete
	// c - add/create

	static class MacroPartList extends ListField {

		protected void paint(Graphics graphics) {
			super.paint(graphics);
			// always draw as if we have focus even when we don't.
			drawFocus(graphics, true);
		}

		protected void drawFocus(Graphics graphics, boolean on) {
			// XYRect rect = new XYRect();
			// // getFocusRect(rect);
			// int idx = getSelectedIndex();
			if (isFocus()) {
				// If we're drawing focus, there's a fair chance that our selection
				// has changed. Since the control doesn't provide any selection change notification
				// we'll use this as the closest substitute.
				fieldChangeNotify(1);
			}
			super.drawFocus(graphics, on);

		}
	}

	// Alt+nav moves selection up/down.
	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		if ((status & Keypad.KEY_ALT) > 0) {
			int idx = partsList.getSelectedIndex();
			if (idx > -1) {
				if (dy > 0) {
					moveDown(idx);
				} else if (dy < 0) {
					moveUp(idx);
				}
			}
		}
		return super.navigationMovement(dx, dy, status, time);
	}

	MenuItem itemDuplicate = new MenuItem(res, MENU_DUPLICATE, 10000, 10) {
		public void run() {
			int idx = partsList.getSelectedIndex();
			if (idx == -1)
				return;
			Vector commands = macro.getCommandVector();
			PersistableCommandFactory pf = (PersistableCommandFactory) commands.elementAt(idx);
			commands.insertElementAt(new PersistableCommandFactory(pf), idx + 1);
			partsList.setSize(commands.size(), idx + 1);
			enableControlsForSelection(true);

		};
	};

	MenuItem itemMoveDown = new MenuItem(res, MENU_MOVEDOWN, 20000, 10) {
		public void run() {
			moveDown(partsList.getSelectedIndex());
		};
	};
	MenuItem itemMoveUp = new MenuItem(res, MENU_MOVEUP, 20000, 10) {
		public void run() {
			moveUp(partsList.getSelectedIndex());
		};
	};

	MenuItem itemDelete = new MenuItem(res, MENU_DELETE_ACTION, 10000, 10) {
		public void run() {
			deleteAction(partsList.getSelectedIndex());
		};
	};
	MenuItem itemEdit = new MenuItem(res, MENU_EDIT_ACTION, 10000, 10) {
		public void run() {
			editAction(partsList.getSelectedIndex());
		};
	};

	MenuItem itemAddAction = new MenuItem(res, MENU_ADD_ACTION, 10000, 10) {
		public void run() {
			add(partsList.getSelectedIndex());
		};
	};

	private void deleteAction(int index) {
		macro.getCommandVector().removeElementAt(index);
		enableControlsForSelection(true);
	}

	private void add(int idx) {
		Vector commands = macro.getCommandVector();
		KeybindState state = new KeybindState(-1, "Add Macro Action", null, null);
		popup.setKeybindState(state);
		UiApplication.getUiApplication().pushModalScreen(popup);
		if (popup.isChangeSaved()) {
			PersistableCommandFactory data = new PersistableCommandFactory(0, state.command.getId(), state.parameter);
			if (idx == -1) {
				commands.addElement(data);
				idx = commands.size() - 1;
			} else {
				commands.insertElementAt(data, idx + 1);
			}
			partsList.setSize(commands.size(), idx);
		}
		enableControlsForSelection(true);

	}

	private void editAction(int idx) {
		Vector commands = macro.getCommandVector();
		PersistableCommandFactory pf = (PersistableCommandFactory) commands.elementAt(partsList.getSelectedIndex());
		KeybindState state = new KeybindState(-1, "Edit Macro Action",
				KeyBindingManager.getInstance().getExecutableCommandById(pf.getExecutableCommandId()),
				pf.getParam());
		popup.setKeybindState(state);
		UiApplication.getUiApplication().pushModalScreen(popup);
		if (popup.isChangeSaved()) {
			state.changed = true;
			commands.insertElementAt(new PersistableCommandFactory(0, state.command.getId(), state.parameter), idx);
			commands.removeElementAt(idx + 1);
		}
		enableControlsForSelection(true);

	}

	private void moveUp(int index) {
		if (index > 0) {
			Tools.swapVectorElements(macro.getCommandVector(), index, index - 1);
			enableControlsForSelection(true);
		}

	}

	private void moveDown(int index) {
		if (index < macro.getCommandVector().size() - 1) {
			Tools.swapVectorElements(macro.getCommandVector(), index, index + 1);
			enableControlsForSelection(true);

		}
	}

	protected void makeMenu(Menu menu, int instance) {
		int idx = partsList.getSelectedIndex();
		menu.add(itemAddAction);
		if (idx > -1) {
			if (idx > 0) {
				menu.add(itemMoveUp);
			}
			if (idx < macro.getCommandVector().size() - 1) {
				menu.add(itemMoveDown);
			}

			menu.add(itemDuplicate);
			menu.add(itemDelete);
			menu.add(itemEdit);
			menu.setDefault(itemEdit);
		} else {
			menu.setDefault(itemAddAction);
		}
		if (instance != Menu.INSTANCE_CONTEXT) {
			super.makeMenu(menu, instance);
		}

	}

	public MacroEditorScreen(Macro macro) {
		super(DEFAULT_CLOSE | DEFAULT_MENU);
		editing = true;
		this.macro = macro;
		// instructions
		setTitle(res.getString(MACRO_TITLE_EDIT) + macro.getName());
		nameField = new BasicEditField(res.getString(MACRO_EDIT_NAME), "", 32,
				BasicEditField.NO_NEWLINE);
		nameField.setText(macro.getName());
		// @todo -- we need to copy our original values - what if the user doens't want to save
		// changes but modifiers the vector or command parameters?
		// @todo -- use new Macro(Macro m) constructor here - and update the backingmacro store
		// only after changes are saved.  Only problm here is that we ALSO need to preserve GUID

		// Next, a list of commands in this macro
		partsList = new MacroPartList();
		Vector commands = macro.getCommandVector();
		cb = new VectorListFieldCallback(commands);
		partsList.setCallback(cb);
		partsList.setSize(commands.size());
		// @todo add(instructionFIeld)

		add(nameField);
		add(new SeparatorField());
		add(partsList);

		if (PlatformServicesProvider.getInstance().hasTouchscreen()) {
			moveUp = new BitmapButtonField("arrowup");
			moveDown = new BitmapButtonField("arrowdown");
			deletePart = new BitmapButtonField("minus");
			editPart = new BitmapButtonField("write");
			newPart = new BitmapButtonField("plus");
			moveUp.setEditable(false);
			moveDown.setEditable(false);
			deletePart.setEditable(false);
			editPart.setEditable(false);
			HorizontalFieldManager moveButtons = new HorizontalFieldManager();
			moveButtons.add(newPart);
			moveButtons.add(editPart);
			moveButtons.add(deletePart);
			moveButtons.add(moveUp);
			moveButtons.add(moveDown);
			add(moveButtons);
			// These get set last so that we don't trigger notifications during
			// our setup above.
			moveDown.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					moveDown(partsList.getSelectedIndex());
				}
			});
			deletePart.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					deleteAction(partsList.getSelectedIndex());
				}
			});
			moveUp.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					moveUp(partsList.getSelectedIndex());
				}
			});
			editPart.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					editAction(partsList.getSelectedIndex());
				}
			});
			newPart.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					add(partsList.getSelectedIndex());
				}
			});
		}
		partsList.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (ignoreNextListChange) {
					ignoreNextListChange = false;
				} else {
					enableControlsForSelection(false);
				}
			}
		});
		okCancel = new OKCancelControl();

		add(okCancel);
		okCancel.setChangeListener(this);

	}

	public MacroEditorScreen() {
		this(new Macro());
		editing = false;
		setTitle(res, MACRO_TITLE_NEW);
	}

	public boolean isDataValid() {
		String name = nameField.getText().trim();
		if (name.length() == 0) {
			Status.show(res.getString(MACRO_EDIT_MSG_NO_NAME));
			nameField.setFocus();
			return false;
		}

		if (macro.getCommandVector().size() == 0) {
			Status.show(res.getString(MACRO_EDIT_MSG_NO_CONTENT));
			return false;
		}

		if (!editing || !macro.getName().equals(name)) {
			// if this is a new macro, or if we've changed the name of an existing macro
			// make sure that we don't conflict
			if (MacroManager.getInstance().getMacro(name) != null) {
				Status.show(res.getString(MACRO_EDIT_MSG_NAME_IN_USE));
				nameField.setText(macro.getName());
				nameField.setFocus();
				return false;
			}
		}

		try {
			save();
		} catch (IOException e) {
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.FieldChangeListener#fieldChanged(net.rim.device.api.ui.Field, int)
	 */
	public void fieldChanged(Field field, int context) {
		// Logger.debug("MacroEditorScreen: fieldChange: " + field + " context: " + context);
		if (field == okCancel) {
			if (context == OKCancelControl.CONTEXT_OK_PRESS) {
				if (!isDataValid()) {
					return;
				}
			} else {
				if (isDirty()) {
					if (Dialog.ask(Dialog.D_YES_NO, res.getString(MACRO_EDIT_CONFIRM_CANCEL)) == Dialog.NO) {
						return;
					}
				}
			}
			try {
				save();
			} catch (IOException e) {
			}
			close();
		}
	}

	private void enableControlsForSelection(boolean invalidateList) {
		int size = macro.getCommandVector().size();
		if (partsList.getSize() != size) {
			partsList.setSize(size);
			// ignore the insert event.
			ignoreNextListChange = true;
		}
		int sel = partsList.getSelectedIndex();
		// Logger.debug("MacroEditorScreen: list selection = " + sel);
		boolean validSelection = sel > -1;
		if (PlatformServicesProvider.getInstance().hasTouchscreen()) {
			moveUp.setEditable(sel > 0);
			moveDown.setEditable(validSelection && sel < (partsList.getSize() - 1));
			deletePart.setEditable(validSelection);
			editPart.setEditable(validSelection);
		}
		if (invalidateList) {
			partsList.invalidate();
		}
	}

	/**
	 * Get the macro that was created by this dialog.
	 * 
	 * @return the edited and validated macro, as long as "show" returns true
	 */
	public Macro getMacro() {
		return this.macro;
	}

	/*
	 * (non-Javadoc)
	 * @see net.rim.device.api.ui.Screen#save()
	 */
	public void save() throws java.io.IOException {
		macro.setName(nameField.getText());
		// Really all of our other stuff is done...
		saved = true;
	}

	public boolean isSaved() {
		return this.saved;
	};

}
