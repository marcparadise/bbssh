package org.bbssh.ui.screens;

import java.io.IOException;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.component.TreeField;
import net.rim.device.api.ui.component.TreeFieldCallback;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.IntEnumeration;
import net.rim.device.api.util.IntIntHashtable;
import net.rim.device.api.util.IntLongHashtable;
import net.rim.device.api.util.IntVector;
import net.rim.device.api.util.LongEnumeration;
import net.rim.device.api.util.LongHashtable;

import org.bbssh.BBSSHApp;
import org.bbssh.command.CommandConstants;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.keybinding.BoundCommand;
import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.model.KeyBindingManager;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.components.IndexedListFieldItem;
import org.bbssh.ui.components.keybinding.CommandBindingPopup;
import org.bbssh.ui.components.keybinding.KeybindState;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

/**
 * Screen that displays a list of keyboard shortcuts and the commands they are bound to.
 * 
 * @author marc
 * 
 */
public class KeybindingScreen extends MainScreen implements FieldChangeListener {
	public static final int FILTER_SHOW_BOUND = 0;
	public static final int FILTER_SHOW_MODIFIED = 1;
	private ResourceBundleFamily res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	private CommandBindingPopup popup = new CommandBindingPopup(false);
	private ObjectChoiceField filterList;

	private TreeField eventTree;
	private IndexedListFieldItem[] categoryData = null;
	private IntLongHashtable treeNodeMapping;
	private int lastRootNodeId = -1;
	private Font fontBase;
	private Font line1Font;
	private Font line2Font;
	// List of items which need to be updated and saved.
	private LongHashtable saveList = new LongHashtable(128);
	private Font line2FontAlt;

	/**
	 * create a BoundCommandList instance and initializes the filter to the selection
	 */
	public KeybindingScreen() {
		super(DEFAULT_CLOSE | Screen.NO_VERTICAL_SCROLL | Screen.NO_VERTICAL_SCROLLBAR);
		setTitle(res.getString(BBSSHResource.KEYBIND_TITLE));
		treeNodeMapping = new IntLongHashtable(128);
		fontBase = getFont();
		line1Font = fontBase.derive(Font.BOLD, (fontBase.getHeight() / 4) * 3);
		line2Font = fontBase.derive(Font.PLAIN, (fontBase.getHeight() / 4) * 3);
		line2FontAlt = line2Font.derive(Font.ITALIC);
		fontBase = getFont().derive(Font.BOLD);
		// Font font = orig.derive(Font.PLAIN, 12);
		// setFont(font);
		setupCategoryList();
		filterList = new ObjectChoiceField(res.getString(BBSSHResource.KEYBIND_LBL_FILTER), categoryData);
		eventTree = new TreeField(callback, TreeField.FOCUSABLE);
		eventTree.setDefaultExpanded(false);
		eventTree.setRowHeight(line1Font.getHeight() + line2Font.getHeight() + 4);
		add(filterList);
		add(new SeparatorField());
		VerticalFieldManager scroller = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		scroller.add(eventTree);
		add(scroller);

		// For some reason in 6.0 ObjectChoiceFields and ObjectListFields somtimes come with a fieldChangeListener
		// already set.
		filterList.setChangeListener(null); // clear existing one to avoid exception
		filterList.setChangeListener(this);
		// This will also cause the initial list population to complete... unless default index is 0,
		filterList.setSelectedIndex(0);
		eventTree.setChangeListener(null);
		eventTree.setChangeListener(this);
		refreshTable();

	}

	private void addCategoryItemIfAvailable(Vector v, int catId, int resId) {
		IntVector temp = PlatformServicesProvider.getInstance().getEventsForCategory(catId);
		if (temp != null && temp.size() > 0)
			v.addElement(new IndexedListFieldItem(res.getString(resId), catId));

	}

	private void setupCategoryList() {
		Vector v = new Vector(16);
		v.addElement(new IndexedListFieldItem(res.getString(BBSSHResource.KEYBIND_FILTER_LBL_BOUND), -1));
		// v.addElement(new IndexedListFieldItem(res.getString(BBSSHResource.KEYBIND_FILTER_LBL_MODIFIED), -1));
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_KEYBOARD, BBSSHResource.BIND_CATEGORY_KEYBOARD);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_PHONE, BBSSHResource.BIND_CATEGORY_PHONE);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_NAV, BBSSHResource.BIND_CATEGORY_NAV);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_MEDIA, BBSSHResource.BIND_CATEGORY_MEDIA);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_GESTURE, BBSSHResource.BIND_CATEGORY_TOUCH_OTHER);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_CLICK, BBSSHResource.BIND_CATEGORY_TOUCH_CLICK);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_DOUBLE_CLICK,
				BBSSHResource.BIND_CATEGORY_TOUCH_DOUBLE_CLICK);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_TAP, BBSSHResource.BIND_CATEGORY_TOUCH_TAP);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_DOUBLE_TAP, BBSSHResource.BIND_CATEGORY_TOUCH_DOUBLE_TAP);
		addCategoryItemIfAvailable(v, KeyBindingHelper.CAT_TOUCH_HOVER, BBSSHResource.BIND_CATEGORY_TOUCH_HOVER);
		categoryData = new IndexedListFieldItem[v.size()];
		v.copyInto(categoryData);
	}

	/**
	 * Adds entry to the table for the specified key code and modifier.
	 * 
	 * @param line
	 * 
	 * @param keyCode
	 * @param modifier
	 */
	private String getNodeText(int nodeId, int line) {
		long packed = treeNodeMapping.get(nodeId);
		boolean boundOnly = filterList.getSelectedIndex() == FILTER_SHOW_BOUND;

		// Shouldn't actually happen, this is just a sanity check...
		if (packed == -1 && !boundOnly)
			return "Invalid Node";

		StringBuffer bindingDesc = new StringBuffer(20);
		if (boundOnly) {
			int parent = eventTree.getParent(nodeId);
			if (parent == 0) {
				if (line > 0) {
					return "";
				}
				return ((IndexedListFieldItem) eventTree.getCookie(nodeId)).getName();
			}
		}
		// Line 0 is the binding name / name+modifier
		if (line == 0) {
			String mod = KeyBindingHelper.getModifierFriendlyName((int) (packed & 0xFFFFFFFF));
			boolean hasmod = (mod != null && mod.length() > 0);
			if (hasmod) {
				bindingDesc.append(mod);
				bindingDesc.append(" + ");
			}
			// if (boundOnly && hasmod) {
			// bindingDesc.append(" + ");
			// }
			// All one line for filter 0 (bound only) - no separately liens for modifiers.
			// if (boundOnly || !hasmod) {
			int resId = PlatformServicesProvider.getInstance().getEventResourceId((int) (packed >> 32));
			if (resId == -1) {
				// with our existing filters in populating the tree this should not happen, but again
				// just a sanity check
				bindingDesc.append("Unknown event: " + (int) (packed >> 32));
			} else
				bindingDesc.append(res.getString(resId));
			// }

		} else {
			// Line 1 is the command that's bound to this.
			// int filter = filterList.getSelectedIndex();
			KeyBindingManager man = KeyBindingManager.getInstance();
			// First make sure we don't have a locally modified version of the binding.
			KeybindState state = (KeybindState) saveList.get(packed);
			Object param = null;
			ExecutableCommand ecmd = null;
			if (state == null) {
				BoundCommand cmd = man.getKeyBinding(packed);
				if (cmd != null) {
					ecmd = cmd.getCommand();
					param = cmd.getParam();
				}
			} else {
				ecmd = state.command;
				param = state.parameter;
			}
			if (ecmd != null) {
				bindingDesc.append("    ");
				bindingDesc.append(res.getString(ecmd.getNameResId()));
				if (ecmd.isParameterRequired() || (ecmd.isParameterOptional() && param != null)) {
					bindingDesc.append(": ");
					bindingDesc.append(ecmd.translateParameter(param));
				}
			}
		}
		return bindingDesc.toString();
	}

	/**
	 * Populates key binding table based on selected filter; if filters are not set, then all bindable keys are added.
	 */
	private void populateKeybindTable() {
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		int filter = filterList.getSelectedIndex();
		if (filter == -1)
			return;
		filter = categoryData[filter].getIndex();

		treeNodeMapping.clear();

		if (filter > 0) {
			eventTree.setDefaultExpanded(false);
			populateEvents(filter, (IntVector) psp.getEventsForCategory(filter));
		} else {
			eventTree.setDefaultExpanded(true);
			populateBoundEvents();
		}

	}

	// private int getFilterIndexFromCategory(int categoryId) {
	// for (int x = 0; x < categoryData.length; x++) {
	// if (categoryData[x].getIndex() == categoryId) {
	// return x;
	// }
	// }
	// return -1;
	// }

	/**
	 * Populates the event tree with all bound events, within a tree structure by type.
	 */
	private void populateBoundEvents() {
		// Create a node for each category. Go through all bindings and assign to appropriate nodes.
		// Finally, remove those categories that are empty.

		int nodeid = -1;
		IntIntHashtable rootNodes = new IntIntHashtable(categoryData.length); // simple tracking of category odes.
		for (int x = 0; x < categoryData.length; x++) {
			nodeid = (nodeid == -1 ? eventTree.addChildNode(0, categoryData[x]) : eventTree.addSiblingNode(nodeid,
					categoryData[x]));
			rootNodes.put(categoryData[x].getIndex(), nodeid);
		}

		nodeid = -1;
		KeyBindingManager mgr = KeyBindingManager.getInstance();
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();

		LongEnumeration enm = mgr.getBindingFactories().keys();

		long next;
		int cat, addnode, evt;
		while (enm.hasMoreElements()) {
			next = enm.nextElement();
			evt = (int) (next >> 32);
			// Some default bindings will be for hardware that this device does not have
			// Ignore these.
			if (!psp.isEventValidForDevice(evt)) {
				continue;
			}
			// We may have some default bindings not available on this platform - their
			// category won't be found. We'll ignore them.
			cat = rootNodes.get(psp.getEventCategory(evt));
			if (cat == -1)
				continue;

			addnode = eventTree.getLastNode(cat, true);
			nodeid = (addnode == cat ? eventTree.addChildNode(cat, null) : eventTree.addSiblingNode(addnode, null));
			treeNodeMapping.put(nodeid, next);
		}

		// Finally, clean up the nodes we created that don't have children.
		IntEnumeration en = rootNodes.elements();
		while (en.hasMoreElements()) {
			int id = en.nextElement();
			if (eventTree.getFirstChild(id) == -1) {
				eventTree.deleteSubtree(id);
			}
		}
	}

	private void populateEvents(int catId, IntVector events) {
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();

		boolean hasLeftShift = psp.hasLeftShift();
		boolean hasRightShift = psp.hasRightShift();
		boolean hasShiftX = psp.hasShiftX();
		int nextid = -1;
		int lshift = KeypadListener.STATUS_SHIFT | KeypadListener.STATUS_SHIFT_LEFT;
		int rshift = KeypadListener.STATUS_SHIFT | KeypadListener.STATUS_SHIFT_RIGHT;
		int eventId;
		int size = events.size();
		for (int x = 0; x < size; x++) {
			eventId = events.elementAt(x);
			lastRootNodeId = (lastRootNodeId == -1 ? eventTree.addChildNode(0, null) : eventTree.addSiblingNode(
					lastRootNodeId, null));
			treeNodeMapping.put(lastRootNodeId, Tools.packToLong(eventId, 0));

			// One ugly exception: ALT Escape can't be bound - though shift can.
			if (eventId != Keypad.KEY_ESCAPE) {
				nextid = eventTree.addChildNode(lastRootNodeId, null); // ALT
				treeNodeMapping.put(nextid, Tools.packToLong(eventId, KeypadListener.STATUS_ALT));
			}

			// ShiftX or LShift depending on keyboard.
			if (hasLeftShift || hasShiftX) {
				// can happen if keypad.escape ...
				if (nextid == -1) {
					nextid = eventTree.addChildNode(lastRootNodeId, null); // ALT
				} else {
					nextid = eventTree.addSiblingNode(nextid, null);
				}
				treeNodeMapping.put(nextid, Tools.packToLong(eventId, lshift));
			}

			if (hasRightShift) {
				nextid = eventTree.addSiblingNode(nextid, null); // RShift
				treeNodeMapping.put(nextid, Tools.packToLong(eventId, rshift));
			}
		}
	}

	public void save() throws IOException {
		KeyBindingManager manager = KeyBindingManager.getInstance();
		LongEnumeration en = saveList.keys();
		while (en.hasMoreElements()) {
			KeybindState state = (KeybindState) saveList.get(en.nextElement());
			if (state.changed) {
				if (state.command == null) {
					manager.unbindKey(state.combinedKey);
				} else {
					// Some cleanup - don't store a valu eif it's not used.
					manager.bindKey(state.combinedKey, state.command, state.parameter);
					if (!state.command.isParameterRequired() && !state.command.isParameterOptional()) {
						state.parameter = null;
					}

				}

			}

		}
		Logger.info("Saving changes to key binding manager.");
		KeyBindingManager.getInstance().commitData();
		super.save();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rim.device.api.ui.container.MainScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
	 */
	protected void makeMenu(Menu menu, int instance) {
		super.makeMenu(menu, instance);

		if (getNodeMappedKey(eventTree.getCurrentNode()) != -1) {
			BoundCommand cmd = KeyBindingManager.getInstance().getKeyBinding(
					getNodeMappedKey(eventTree.getCurrentNode()));
			if (cmd != null)
				menu.add(itemClear);
			menu.add(itemEdit);
			KeybindState state = getSelectedBindingState(false);
			if (state != null && state.changed == true)
				menu.add(itemRevert);

			menu.setDefault(itemEdit);
		}
		if (instance != Menu.INSTANCE_CONTEXT) {
			menu.add(itemResetToDefault);
		}
	}

	/**
	 * Clear and repopulate the table based on current filter.
	 */
	private void refreshTable() {
		BBSSHApp.inst().suspendPainting(true);
		lastRootNodeId = -1;
		eventTree.deleteAll();
		populateKeybindTable();
		BBSSHApp.inst().suspendPainting(false);

		// delete(table);
		// table.deleteAll();
		// add(table);
	}

	/**
	 * get the binding state for the current selection. If the selection has been modified, it will return the modified
	 * keybind state. This will create the state if it does not already exist.
	 * 
	 * @param createIfNotPresent if a valid node is selected but no state object exists, create the state object.
	 * 
	 * @return the keybind state assosciated with the current selection, or null if no selection/selection is not a
	 *         keybinding
	 */
	private KeybindState getSelectedBindingState(boolean createIfNotPresent) {
		return getBindingState(eventTree.getCurrentNode(), createIfNotPresent);
	}

	private KeybindState getBindingState(int node, boolean createIfNotPresent) {
		long key = getNodeMappedKey(node);
		if (key == -1)
			return null;
		if (saveList.containsKey(key))
			return (KeybindState) saveList.get(key);
		KeyBindingManager mgr = KeyBindingManager.getInstance();
		BoundCommand cmd = mgr.getKeyBinding(key);
		if (createIfNotPresent) {
			// @todo - we can just use boundcommand for this, can't we?
			KeybindState state;
			if (cmd == null)
				state = new KeybindState(key, getNodeText(node, 0), null, null);
			else
				state = new KeybindState(key, getNodeText(node, 0), cmd.getCommand(), cmd.getParam());
			saveList.put(key, state);
			return state;
		}
		return null;

	}

	private long getNodeMappedKey(int node) {
		if (node == -1)
			return -1;
		if (eventTree.getCookie(node) != null)
			return -1;
		long key = treeNodeMapping.get(node);
		return key;

	}

	public void fieldChanged(Field field, int context) {
		refreshTable();
	}

	/**
	 * Clear any key binding associated with selection; sets flag to indicate change completed if the item wasn't
	 * originally clear.
	 */
	private MenuItem itemClear = new MenuItem(res, BBSSHResource.KEYBIND_MENU_ITEM_CLEAR, 0x00200000, 1) {
		public void run() {
			clearSelectedBinding();
		}
	};

	/**
	 * Revert any changes made to the selected key binding.
	 */
	private MenuItem itemRevert = new MenuItem(res, BBSSHResource.KEYBIND_MENU_ITEM_REVERT, 0x00200000, 1) {
		public void run() {
			int node = eventTree.getCurrentNode();
			long key = getNodeMappedKey(node);
			if (key == -1)
				return;
			KeybindState state = getBindingState(node, false);
			if (state == null)
				return; // no changes
			if (!state.changed)
				return; // no changes
			if (saveList.containsKey(key)) {
				saveList.remove(key);
			}
			eventTree.invalidateNode(node);
		}

	};

	/**
	 * Edit the selected key binding.
	 */
	private MenuItem itemEdit = new MenuItem(res, BBSSHResource.KEYBIND_MENU_ITEM_EDIT, 0x00200000, 10) {
		public void run() {
			KeybindState state = getSelectedBindingState(true);
			if (state == null)
				return;
			popup.setKeybindState(state);
			UiApplication.getUiApplication().pushModalScreen(popup);
			if (popup.isChangeSaved()) {
				if (state.command != null && state.command.getId() == CommandConstants.NONE) {
					clearSelectedBinding();
					return;
				}
				int node = eventTree.getCurrentNode();
				long key = getNodeMappedKey(node);
				BoundCommand cmd = KeyBindingManager.getInstance().getKeyBinding(key);
				if (cmd == null) {
					state.changed = true;
				} else {
					if (cmd.getCommand() != state.command || cmd.getParam() != state.parameter) {
						state.changed = true;
					} else {
						state.changed = false;
					}

				}
				eventTree.invalidateNode(node);
			}
		}
	};

	/**
	 * Reset ALL keybindings to their default values.
	 */
	private MenuItem itemResetToDefault = new MenuItem(res, BBSSHResource.KEYBIND_MENU_ITEM_RESET, 0x00300000, 10) {
		public void run() {
			if (Dialog.ask(Dialog.D_YES_NO, res.getString(BBSSHResource.MSG_KEYBIND_RESET_CONFIRM)) == Dialog.YES) {
				saveList.clear();
				KeyBindingManager.getInstance().resetDefaults();
				Status.show(res.getString(BBSSHResource.MSG_KEYBIND_RESET_COMPLETE));
				setDirty(true);
				refreshTable();
			}
		}
	};

	private void clearSelectedBinding() {
		int node = eventTree.getCurrentNode();
		BoundCommand cmd = KeyBindingManager.getInstance().getKeyBinding(getNodeMappedKey(node));
		KeybindState state = getSelectedBindingState(true);
		if (state == null)
			return;
		// Should not happene - no binding to clear.
		if (cmd == null)
			return;

		state.command = null;
		state.changed = true;
		eventTree.invalidateNode(node);

	}

	private boolean isNodeBindingCleared(int node) {
		KeybindState state = getBindingState(node, false);
		return (state != null && state.changed == true && state.command == null);

	}

	private boolean isNodeBindingChanged(int node) {
		KeybindState state = getBindingState(node, false);
		return (state != null && state.changed);

	}

	private TreeFieldCallback callback = new TreeFieldCallback() {
		// private int savedIndent;

		public void drawTreeItem(TreeField treeField, Graphics graphics, int node, int y, int width, int indent) {

			graphics.setColor(Color.BLACK);
			// If there's a cookie we don't draw a second line - bceause only "category" nodes have cookies, and
			// they
			// consist only of the category text
			if (treeField.getCookie(node) == null) {
				graphics.setFont(line1Font);
				graphics.drawText(getNodeText(node, 0), indent, y, 0, width);
				// If we're drawing a leaf node, then we want to set it to the same indent level
				// as its parent.
				// @todo - shading/box to make this group of nodes look like part of hte same block visually?
				// if (treeField.getFirstChild(node) == -1) {
				// indent = savedIndent;
				// } else {
				// savedIndent = indent;
				// }
				String text;
				if (isNodeBindingCleared(node)) {
					graphics.setColor(Color.RED);
					graphics.setFont(line2FontAlt);
					text = res.getString(BBSSHResource.KEYBIND_LBL_CLEARED);
				} else {
					text = getNodeText(node, 1);
					if (text.length() == 0) {
						graphics.setColor(Color.DARKGRAY);
						graphics.setFont(line2FontAlt);
						text = "    " + res.getString(BBSSHResource.KEYBIND_LBL_UNBOUND);
					} else {
						graphics.setColor(isNodeBindingChanged(node) ? Color.RED : Color.BLUE);
					}
				}
				graphics.drawText(text, indent, y + (treeField.getRowHeight() / 2), 0, width);
			} else {
				graphics.setFont(fontBase);
				graphics.drawText(getNodeText(node, 0), indent, y + (treeField.getRowHeight() / 2)
							- (fontBase.getHeight() / 2), 0, width);
			}
		}
	};

	public boolean isDirty() {
		LongEnumeration en = saveList.keys();
		while (en.hasMoreElements()) {
			if (((KeybindState) saveList.get(en.nextElement())).changed)
				return true;
		}
		return false;

	}
}
