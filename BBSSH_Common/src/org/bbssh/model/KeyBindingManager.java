/*
 *  Copyright (C) 2010 Marc A. Paradise
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.bbssh.model;

import java.io.EOFException;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.synchronization.SyncObject;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.IntIntHashtable;
import net.rim.device.api.util.LongHashtable;

import org.bbssh.command.ChangeInputMode;
import org.bbssh.command.CommandConstants;
import org.bbssh.command.CopyText;
import org.bbssh.command.DisconnectSession;
import org.bbssh.command.IncrDecrFontSize;
import org.bbssh.command.ListSessions;
import org.bbssh.command.NullCommand;
import org.bbssh.command.PasteText;
import org.bbssh.command.PopActiveScreen;
import org.bbssh.command.PromptForAndSendSymbol;
import org.bbssh.command.ReconnectSession;
import org.bbssh.command.RefreshScreen;
import org.bbssh.command.RunMacro;
import org.bbssh.command.ScrollDown;
import org.bbssh.command.ScrollUp;
import org.bbssh.command.SendMovementKey;
import org.bbssh.command.SendTerminalKey;
import org.bbssh.command.SendText;
import org.bbssh.command.ShowDebugMessage;
import org.bbssh.command.ShowFontPopup;
import org.bbssh.command.ShowKeybindingScreen;
import org.bbssh.command.ShowMacroScreen;
import org.bbssh.command.ShowOverlayCommands;
import org.bbssh.command.ShowOverlayInput;
import org.bbssh.command.ShowSpecialKeysScreen;
import org.bbssh.command.ShowURLScraperScreen;
import org.bbssh.command.TakeScreenShot;
import org.bbssh.command.ToggleAltKey;
import org.bbssh.command.ToggleControlKey;
import org.bbssh.command.ToggleKeyboardState;
import org.bbssh.command.ToggleLocalAlt;
import org.bbssh.command.ToggleLocalLeftShift;
import org.bbssh.command.ToggleLocalRightShift;
import org.bbssh.command.ToggleOrientationLock;
import org.bbssh.command.Wait;
import org.bbssh.command.WaitForActivity;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.io.SyncBuffer;
import org.bbssh.keybinding.BoundCommand;
import org.bbssh.keybinding.ExecutableCommand;
import org.bbssh.keybinding.KeyBindingHelper;
import org.bbssh.keybinding.PersistableCommandFactory;
import org.bbssh.keybinding.defaults.DefaultKeybindingSet;
import org.bbssh.keybinding.defaults.FullKeyboard32;
import org.bbssh.keybinding.defaults.FullKeyboard39;
import org.bbssh.keybinding.defaults.FullKeyboardBase;
import org.bbssh.keybinding.defaults.PhoneBase;
import org.bbssh.keybinding.defaults.PhysicalKeyboardBase;
import org.bbssh.keybinding.defaults.Reduced24;
import org.bbssh.keybinding.defaults.ReducedITUT;
import org.bbssh.keybinding.defaults.Touchscreen;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.ui.screens.ShowSessionDetailScreen;
import org.bbssh.util.Tools;

/**
 * This class manages customized key bindings.
 */
public class KeyBindingManager extends DefaultSyncCollection {
	public static final long KEYBIND_GUID = 0x567ec2fa86fbe498L;// org.bbssh.model.KeyBindingManager
	private LongHashtable execCommands = new LongHashtable();
	private static KeyBindingManager me;
	/** full list of supported commands */
	ExecutableCommand[] commandArray;
	LongHashtable boundCommandMap = new LongHashtable(128);
	LongHashtable bindingFactories = new LongHashtable(128);
	/** Alpha-sorted array of commands which can be bound to a terminal */
	ExecutableCommand[] bindableCommands;

	/** Alpha-sorted array of commands which can be used in macros. */
	ExecutableCommand[] macroBindableCommands;
	private int bindableCount = 0;
	private int macroCount = 0;
	/**
	 * If debug bindings are enabled, this flag will be set to prevent saving the values.
	 */
	private boolean debugMode = false;

	private void addCommandToMap(ExecutableCommand cmd) {
		if (cmd.isAvailableOnCurrentPlatform()) {
			execCommands.put(cmd.getId(), cmd);
			if (cmd.isMacroAction()) {
				macroCount++;
			}
			if (cmd.isKeyBindable()) {
				bindableCount++;
			}
		}
	}

	/**
	 * Singleton constructor which loads cached command instance data.
	 * 
	 */
	private KeyBindingManager() {

	}

	/**
	 * Initializer loads persistent mappings from storage, or creates new mappings if old ones don't exist. This will
	 */
	public synchronized void initialize() {
		initializeStaticData();
		loadMap();
		loadData();
		Vector vdata = getDataVector();
		if (vdata == null || vdata.size() == 0) {
			resetDefaults();
		}

	}

	private void initializeStaticData() {
		// Unfortunately J2ME does not give us any options for self-dsicovery
		// of these components -- so we have to hard-code
		// creation of each instance.
		commandArray = new ExecutableCommand[] {
				new NullCommand(),
				new CopyText(),
				new ChangeInputMode(),
				new SendMovementKey(),
				new PasteText(),
				new PopActiveScreen(),
				new RunMacro(),
				new SendText(),
				new SendTerminalKey(),
				new ToggleAltKey(),
				new ToggleControlKey(),
				new ListSessions(),
				new PromptForAndSendSymbol(),
				new TakeScreenShot(),
				new IncrDecrFontSize(),
				new WaitForActivity(),
				new ShowOverlayCommands(),
				new ShowOverlayInput(),
				new DisconnectSession(),
				new ReconnectSession(),
				new ToggleKeyboardState(),
				new Wait(),
				new ShowFontPopup(),
				new ShowKeybindingScreen(),
				new ShowMacroScreen(),
				new ShowSessionDetailScreen(),
				new ShowSpecialKeysScreen(),
				new ShowURLScraperScreen(),
				new ToggleOrientationLock(),
				new ShowDebugMessage(),
				new ScrollDown(),
				new ScrollUp(),
				new RefreshScreen(),
				new ToggleLocalAlt(),
				new ToggleLocalLeftShift(),
				new ToggleLocalRightShift()
		};

	}

	private void loadMap() {
		// Load our caches and our map
		for (int x = 0; x < commandArray.length; x++) {
			addCommandToMap(commandArray[x]);
		}
		bindableCommands = new ExecutableCommand[bindableCount];
		macroBindableCommands = new ExecutableCommand[macroCount];
		int len = commandArray.length - 1;
		for (int x = len; x > -1; x--) {
			addCommandToMap(commandArray[x]);
		}
		int bindable = 0, macro = 0;
		for (int x = len; x > -1; x--) {
			ExecutableCommand cmd = commandArray[x];
			if (cmd.isAvailableOnCurrentPlatform()) {
				if (cmd.isKeyBindable()) {
					bindableCommands[bindable++] = cmd;
				}
				if (cmd.isMacroAction()) {
					macroBindableCommands[macro++] = cmd;
				}
			}
		}

		final ResourceBundle res = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
		// @todo - move this commandNameComparator to Tools?
		Comparator commandNameComparator = new Comparator() {

			public int compare(Object o1, Object o2) {
				// This will do a comparison based on display name
				ExecutableCommand c1 = (ExecutableCommand) o1;
				ExecutableCommand c2 = (ExecutableCommand) o2;
				return res.getString(c1.getNameResId()).compareTo(res.getString(c2.getNameResId()));
			}
		};
		Arrays.sort(bindableCommands, commandNameComparator);
		Arrays.sort(macroBindableCommands, commandNameComparator);

		buildSharedKeyboardMap();
		int layout = PlatformServicesProvider.getInstance().getHardwareLayout();
		if (layout == Keypad.HW_LAYOUT_REDUCED_24) {
			buildHW24KeyboardMap();
		} else if (layout == KeyBindingHelper.HW_LAYOUT_ITUT) {
			buildITUTKeyboardMap();
		}
	}

	private void buildHW24KeyboardMap() {
		keymap.put('Q', KeyBindingHelper.KEY_R24_QW);
		keymap.put('E', KeyBindingHelper.KEY_R24_ER);
		keymap.put('T', KeyBindingHelper.KEY_R24_TY);
		keymap.put('U', KeyBindingHelper.KEY_R24_UI);
		keymap.put('O', KeyBindingHelper.KEY_R24_OP);
		keymap.put('A', KeyBindingHelper.KEY_R24_AS);
		keymap.put('D', KeyBindingHelper.KEY_R24_DF);
		keymap.put('G', KeyBindingHelper.KEY_R24_GH);
		keymap.put('J', KeyBindingHelper.KEY_R24_JK);
		keymap.put('L', KeyBindingHelper.KEY_R24_L);
		keymap.put('Z', KeyBindingHelper.KEY_R24_ZX);
		keymap.put('C', KeyBindingHelper.KEY_R24_CV);
		keymap.put('B', KeyBindingHelper.KEY_R24_BN);
		keymap.put(' ', KeyBindingHelper.KEY_R24_0);

	}

	private void buildITUTKeyboardMap() {
		keymap.put('Q', KeyBindingHelper.KEY_ITUT_1);
		keymap.put('T', KeyBindingHelper.KEY_ITUT_2);
		keymap.put('O', KeyBindingHelper.KEY_ITUT_3);
		keymap.put('A', KeyBindingHelper.KEY_ITUT_4);
		keymap.put('G', KeyBindingHelper.KEY_ITUT_5);
		keymap.put('L', KeyBindingHelper.KEY_ITUT_6);
		keymap.put('Z', KeyBindingHelper.KEY_ITUT_7);
		keymap.put('B', KeyBindingHelper.KEY_ITUT_8);
		keymap.put('W', KeyBindingHelper.KEY_ITUT_9);
		keymap.put(' ', KeyBindingHelper.KEY_ITUT_0);

	}

	private void buildSharedKeyboardMap() {
		// These mappings are those that are not direct/one-to-oine mapping with the
		// actual values we key off of..
		keymap.put(Characters.CONTROL_VOLUME_DOWN, Keypad.KEY_VOLUME_DOWN);
		keymap.put(Characters.CONTROL_VOLUME_UP, Keypad.KEY_VOLUME_UP);
		keymap.put(Characters.EURO_SIGN, KeyBindingHelper.KEY_CURRENCY);
		keymap.put(Characters.DOLLAR_SIGN, KeyBindingHelper.KEY_CURRENCY);
		keymap.put(Characters.YEN_SIGN, KeyBindingHelper.KEY_CURRENCY);
		keymap.put(Characters.POUND_SIGN, KeyBindingHelper.KEY_CURRENCY);
		keymap.put('0', KeyBindingHelper.KEY_ZERO);
		keymap.put(KeyBindingHelper.KEY_CAMERA_FOCUS_SDK, KeyBindingHelper.KEY_CAMERA_FOCUS);

		// KEY_DELETE and KEY_NEXT are actually SYM in all supported models thus far. Only one of these
		// two will actually be present on any given keyboard model.
		keymap.put(Keypad.KEY_DELETE, KeyBindingHelper.KEY_SYM);
		keymap.put(Keypad.KEY_NEXT, KeyBindingHelper.KEY_SYM);
		keymap.put(KeyBindingHelper.KEY_FORWARD_SDK, KeyBindingHelper.KEY_FORWARD_SDK);
		keymap.put(KeyBindingHelper.KEY_BACKWARD_SDK, KeyBindingHelper.KEY_FORWARD_SDK);

	}

	private IntIntHashtable keymap = new IntIntHashtable(64);

	/**
	 * return the one and online KeyBindingManager instance.
	 * 
	 * @return object instance
	 */
	public static synchronized KeyBindingManager getInstance() {
		if (me == null) {
			me = new KeyBindingManager();
		}
		return me;
	}

	/**
	 * Bind a keycode & status combination to a specific command
	 * 
	 * @param keyCode key code to bind.
	 * @param statusFlags status flags to mask with key code.
	 * @param cmdId command constant that indicates which command to map this to.
	 * @return the newly bound command instance, or null if cmdId is invalid.
	 */
	public BoundCommand bindKey(int keyCode, int statusFlags, int cmdId) {
		return bindKey(keyCode, statusFlags, cmdId, null);
	}

	/**
	 * Bind a keycode & status combination to a specific command.
	 * 
	 * @param keyCode key code to bind.
	 * @param statusFlags status flags to mask with key code.
	 * @param cmdId command constant that indicates which command to map this to.
	 * @param param parameter to associate with the command. Can be null.
	 * @return the newly bound command instance, or null if cmdId is invalid.
	 */
	public BoundCommand bindKey(int keyCode, int statusFlags, int cmdId, Object param) {
		return bindKey(Tools.packToLong(keyCode, statusFlags), cmdId, param);
	}
	public BoundCommand bindKey(int keyCode, int statusFlags, int cmdId, int param) {
		return bindKey(keyCode, statusFlags, cmdId, new Integer(param));
	}

	/**
	 * Bind masked value to a specific command by instance.
	 * 
	 * @param maskedKey mask consisting of key code and status flags.
	 * @param cmd command instance to bind to this masked value
	 * @param param parameter to associate with the command. Can be null.
	 * @return the newly bound command instance, or null if cmdId is invalid.
	 */
	public synchronized BoundCommand bindKey(long maskedKey, ExecutableCommand cmd, Object param) {
		if (cmd == null) {
			return null;
		}
		BoundCommand command = getKeyBinding(maskedKey);
		if (command == null) {
			command = new BoundCommand(cmd, param);
			boundCommandMap.put(maskedKey, command);
		} else {
			command.setCommand(cmd);
			command.setParam(param);
		}
		PersistableCommandFactory f = new PersistableCommandFactory(maskedKey, cmd.getId(), param);
		bindingFactories.put(maskedKey, f);
		getDataVector().addElement(f);
		return command;

	}

	/**
	 * Bind masked value to a specific command by id.
	 * 
	 * @param maskedKey mask consisting of key code and status flags.
	 * @param cmdId command constant that indicates which command to map this to.
	 * @param param parameter to associate with the command. Can be null.
	 * @return the newly bound command instance, or null if cmdId is invalid.
	 */
	public synchronized BoundCommand bindKey(long maskedKey, int cmdId, Object param) {
		ExecutableCommand cmd = getExecutableCommandById(cmdId);
		return bindKey(maskedKey, cmd, param);
	}

	/**
	 * Clear key binding for specified key combination, if any exists. If the key combination is not mapped, no action
	 * is taken.
	 * 
	 * @param keyCode key code that is bound to a command
	 * @param statusFlags status mask for key code.
	 */
	public synchronized void unbindKey(int keyCode, int statusFlags) {
		unbindKey(Tools.packToLong(keyCode, statusFlags));

	}

	public BoundCommand getKeyBinding(long packedKey) {
		BoundCommand command = (BoundCommand) boundCommandMap.get(packedKey);
		if (command != null) {
			return command;
		}
		PersistableCommandFactory factory = (PersistableCommandFactory) bindingFactories.get(packedKey);
		if (factory == null) {
			return null;
		}

		// Instead of taking the time to load our mapping to actual command instances
		// up front, we'll "lazy load" them as needed.
		command = factory.getBoundCommandInstance();
		boundCommandMap.put(packedKey, command);
		return command;
	}

	/**
	 * Retrieve a key bound to a specific status&keycode combination.
	 * 
	 * If necessary, this will recreate the spceific keybinding from the underlying PersistableCommandFactory data. All
	 * such bindings are cached across sessions for the lifetime of the application.
	 * 
	 * @param keyCode keycode to check
	 * @param statusFlags status flag to check.
	 * @return bound command, or null if it does not exist.
	 */
	public BoundCommand getKeyBinding(int keyCode, int statusFlags) {
		// Check to see if any override for this code should be used.
		int realCode = keymap.get(keyCode);
		if (realCode > -1) {
			keyCode = realCode;
		}
		return getKeyBinding(Tools.packToLong(keyCode, statusFlags));
	}

	/**
	 * @return map of bound commands
	 */
	public LongHashtable getBindingFactories() {
		return this.bindingFactories;
	}

	/**
	 * Initializes default keybindings, overwriting anything already present.
	 */
	public void resetDefaults() {
		getDataVector().removeAllElements();
		boundCommandMap = new LongHashtable(128);
		bindingFactories = new LongHashtable(128);
		loadDefaultKeybindings();

	}

	/**
	 * Initializes the persistent store with default keybindings only if key bindings are not alerady present.
	 * 
	 * @return true if data was loaded, false if it had been previously loaded.
	 */
	private boolean loadDefaultKeybindings() {
		
		// Directional keybindings are the same for all devices.

		// @todo this will be overridden in platform-specific KeyBindingManager instances?
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		// Apply default bindings based on phone model features. 
		Vector bindings = new Vector(10); 
		bindings.addElement(new PhoneBase()); 
		
		if (psp.hasTouchscreen()) { 
			bindings.addElement(new Touchscreen()); 
		}
		if (psp.hasHardwareKeyboard()) {
			bindings.addElement(new PhysicalKeyboardBase()); 
		}
		
		switch (psp.getHardwareLayout()) {
			case Keypad.HW_LAYOUT_32:
				bindings.addElement(new FullKeyboardBase());
				bindings.addElement(new FullKeyboard32());
				break;
			case Keypad.HW_LAYOUT_39:
				bindings.addElement(new FullKeyboardBase());
				bindings.addElement(new FullKeyboard39());
				break;
			case Keypad.HW_LAYOUT_REDUCED_24:
				bindings.addElement(new Reduced24());
				break;
			case KeyBindingHelper.HW_LAYOUT_ITUT:
				bindings.addElement(new ReducedITUT());
				break;
		
		}
		for (int x = 0; x < bindings.size(); x++) { 
			((DefaultKeybindingSet)bindings.elementAt(x)).bindKeys(this, psp);
		}

		commitData();
		return true;
	}

	/**
	 * Retrieve a specific executable command instance by ID.
	 * 
	 * @param commandId command ID
	 * @return command requested or null if invalid.
	 */
	public ExecutableCommand getExecutableCommandById(long commandId) {
		return (ExecutableCommand) execCommands.get(commandId);
	}

	/**
	 * return array of commands whic are availabel for use in macros.
	 * 
	 * @return command array
	 */
	public ExecutableCommand[] getMacroActionCommands() {
		return macroBindableCommands;

	}

	/**
	 * @return name-sorted array of all ExecutableCommands which can be bound to a key for use in a session.
	 * @todo change namet o getSesssionBindableCommands?
	 */
	public ExecutableCommand[] getBindableCommands() {
		return bindableCommands;
	}

	/**
	 * Removes key binding for the this key combination.
	 * 
	 * @param maskedKey
	 */
	public void unbindKey(long maskedKey) {
		PersistableCommandFactory f = (PersistableCommandFactory) bindingFactories.get(maskedKey);
		if (f == null)
			return;

		getDataVector().removeElement(f);
		bindingFactories.remove(maskedKey);
		boundCommandMap.remove(maskedKey);

	}

	public int getSyncVersion() {
		return 0;
	}

	public static PersistableCommandFactory readPersistableCommandFactory(SyncBuffer buffer, int UID, int version)
			throws EOFException {
		PersistableCommandFactory f = new PersistableCommandFactory(UID);
		f.setBoundTo(buffer.readNextLongField());
		f.setExecutableCommandId(buffer.readNextIntField());
		// parameter field load will vary based on the data type.
		short parmType = buffer.readNextByteField();
		if (parmType == 0) {
			f.setParam(null);
		} else if (parmType == 1) {
			f.setParam(new Integer(buffer.readNextIntField()));
		} else { // al else is stored as string
			f.setParam(buffer.readNextStringField());
		}
		return f;

	}

	public static void writePersistableCommandFactory(PersistableCommandFactory factory, SyncBuffer buffer) {
		// BEGIN VERSION 0 FIELDS
		buffer.writeField(factory.getBoundTo());
		buffer.writeField(factory.getExecutableCommandId());
		Object param = factory.getParam();

		if (param == null) {
			// write a marker so we know it's not a valid value
			buffer.writeField((byte) 0);
		} else if (param instanceof Integer) {
			buffer.writeField((byte) 1);
			buffer.writeField(((Integer) param).intValue());
		} else if (param instanceof String) {
			buffer.writeField((byte) 2);
			buffer.writeField((String) param);
		} else {
			buffer.writeField((byte) 3);
			buffer.writeField(param.toString());
			// we don't support any other types
		}
		// END VERSION 0 FIELDS

	}

	public boolean convertImpl(SyncObject object, SyncBuffer buffer, int version) {
		if (!(object instanceof PersistableCommandFactory))
			return false;
		writePersistableCommandFactory((PersistableCommandFactory) object, buffer);
		return true;
	}

	public SyncObject convertImpl(SyncBuffer buffer, int version, int UID, boolean syncDirty) {
		PersistableCommandFactory f;
		try {
			f = readPersistableCommandFactory(buffer, UID, version);
			f.setSyncStateDirty(syncDirty);
			bindingFactories.put(f.getBoundTo(), f);

		} catch (EOFException e) {
			f = null;
		}

		return f;
	}

	public String getSyncName() {
		return "BBSSH Keybindings/Shortcuts";
	}

	public long getPersistentStoreId() {
		return KEYBIND_GUID;
	}

	public void resetState() {
		boundCommandMap.clear();
		bindingFactories.clear();
	}

	private void bindDebugKey(int key, String name) {

		bindKey(key, 0, CommandConstants.SHOW_DEBUG_MESSAGE, name);
		bindKey(key, KeypadListener.STATUS_ALT,
				CommandConstants.SHOW_DEBUG_MESSAGE, "Alt " + name);
		bindKey(key, KeypadListener.STATUS_SHIFT & KeypadListener.STATUS_SHIFT_LEFT,
				CommandConstants.SHOW_DEBUG_MESSAGE, "LS " + name);
		bindKey(key, KeypadListener.STATUS_SHIFT & KeypadListener.STATUS_SHIFT_RIGHT,
				CommandConstants.SHOW_DEBUG_MESSAGE, "RS " + name);
	}

	public void setDebugBindings() {

		ResourceBundleFamily r = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
		PlatformServicesProvider p = PlatformServicesProvider.getInstance();
		debugMode = true; // marks all changes as non-permanent. s
		bindDebugKey(KeyBindingHelper.KEY_NAV_UP, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_NAV_UP)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_DOWN, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_NAV_DOWN)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_LEFT, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_NAV_LEFT)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_RIGHT, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_NAV_RIGHT)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_CLICK, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_NAV_CLICK)));
		bindDebugKey(KeyBindingHelper.KEY_CAMERA_FOCUS, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_CAMERA_FOCUS)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_PINCH_IN, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_PINCH_IN)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_PINCH_OUT, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_PINCH_OUT)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_SWIPE_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_NAV_SWIPE_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_NAV_SWIPE_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_SWIPE_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_NAV_SWIPE_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_SWIPE_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_NAV_SWIPE_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_NAV_SWIPE_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_NAV_SWIPE_WEST)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_CENTER, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_CENTER)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_TAP_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_TAP_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_CENTER, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_CENTER)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBTAP_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_CENTER, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_CENTER)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_CLICK_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_CENTER, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_CENTER)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_DBCLICK_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_CENTER, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_CENTER)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_NORTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTH, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTH)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_EAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_EAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_WEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_WEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_SOUTHWEST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHEAST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHEAST)));
		bindDebugKey(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHWEST, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_TOUCH_HOVER_NORTHWEST)));

		bindDebugKey(KeyBindingHelper.KEY_CAMERA_FOCUS, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_CAMERA_FOCUS)));
		bindDebugKey(KeyBindingHelper.KEY_CURRENCY, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_CURRENCY)));
		bindDebugKey(KeyBindingHelper.KEY_LOCK, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_LOCK)));
		bindDebugKey(KeyBindingHelper.KEY_ZERO, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ZERO)));
		bindDebugKey(Keypad.KEY_BACKSPACE, r.getString(p.getEventResourceId(Keypad.KEY_BACKSPACE)));
		bindDebugKey(Keypad.KEY_CONVENIENCE_1, r.getString(p.getEventResourceId(Keypad.KEY_CONVENIENCE_1)));
		bindDebugKey(Keypad.KEY_CONVENIENCE_2, r.getString(p.getEventResourceId(Keypad.KEY_CONVENIENCE_2)));
		bindDebugKey(Keypad.KEY_END, r.getString(p.getEventResourceId(Keypad.KEY_END)));
		bindDebugKey(Keypad.KEY_ENTER, r.getString(p.getEventResourceId(Keypad.KEY_ENTER)));

		// Leave this off so we can leave the window...
		// bindDebugKey(Keypad.KEY_ESCAPE,res.getString(psp.getEventResourceId()));
		bindDebugKey(Keypad.KEY_MENU, r.getString(p.getEventResourceId(Keypad.KEY_MENU)));
		bindDebugKey(Keypad.KEY_SPEAKERPHONE, r.getString(p.getEventResourceId(Keypad.KEY_SPEAKERPHONE)));
		bindDebugKey(Keypad.KEY_END, r.getString(p.getEventResourceId(Keypad.KEY_END)));
		bindDebugKey(Keypad.KEY_ENTER, r.getString(p.getEventResourceId(Keypad.KEY_ENTER)));
		//                                                                                                                                                            
		// bindDebugKey(Keypad.KEY_MENU , res.getString(psp.getEventResourceId()));
		bindDebugKey(Keypad.KEY_SPEAKERPHONE, r.getString(p.getEventResourceId(Keypad.KEY_SPEAKERPHONE)));
		bindDebugKey(Keypad.KEY_SEND, r.getString(p.getEventResourceId(Keypad.KEY_SEND)));
		bindDebugKey(Keypad.KEY_SPACE, r.getString(p.getEventResourceId(Keypad.KEY_SPACE)));
		bindDebugKey(Keypad.KEY_DELETE, r.getString(p.getEventResourceId(Keypad.KEY_DELETE)));
		bindDebugKey(Keypad.KEY_VOLUME_DOWN, r.getString(p.getEventResourceId(Keypad.KEY_VOLUME_DOWN)));
		bindDebugKey(Keypad.KEY_VOLUME_UP, r.getString(p.getEventResourceId(Keypad.KEY_VOLUME_UP)));

		bindDebugKey(KeyBindingHelper.KEY_FORWARD_SDK, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_FORWARD_SDK)));
		bindDebugKey(KeyBindingHelper.KEY_BACKWARD_SDK, r.getString(p
				.getEventResourceId(KeyBindingHelper.KEY_BACKWARD_SDK)));
		PlatformServicesProvider psp = PlatformServicesProvider.getInstance();
		if (psp.getHardwareLayout() == Keypad.HW_LAYOUT_REDUCED) {
			bindDebugKey(KeyBindingHelper.KEY_R24_ER, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_ER)));
			bindDebugKey(KeyBindingHelper.KEY_R24_TY, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_TY)));
			bindDebugKey(KeyBindingHelper.KEY_R24_UI, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_UI)));
			bindDebugKey(KeyBindingHelper.KEY_R24_OP, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_OP)));
			bindDebugKey(KeyBindingHelper.KEY_R24_AS, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_AS)));
			bindDebugKey(KeyBindingHelper.KEY_R24_DF, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_DF)));
			bindDebugKey(KeyBindingHelper.KEY_R24_GH, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_GH)));
			bindDebugKey(KeyBindingHelper.KEY_R24_JK, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_JK)));
			bindDebugKey(KeyBindingHelper.KEY_R24_L, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_L)));
			bindDebugKey(KeyBindingHelper.KEY_R24_ZX, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_ZX)));
			bindDebugKey(KeyBindingHelper.KEY_R24_CV, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_CV)));
			bindDebugKey(KeyBindingHelper.KEY_R24_BN, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_BN)));
			bindDebugKey(KeyBindingHelper.KEY_R24_0, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_R24_0)));
		} else if (psp.getHardwareLayout() == KeyBindingHelper.HW_LAYOUT_ITUT) {
			bindDebugKey(KeyBindingHelper.KEY_ITUT_1, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_1)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_2, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_2)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_3, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_3)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_4, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_4)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_5, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_5)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_6, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_6)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_7, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_7)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_8, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_8)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_9, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_9)));
			bindDebugKey(KeyBindingHelper.KEY_ITUT_0, r.getString(p.getEventResourceId(KeyBindingHelper.KEY_ITUT_0)));

		}

		// Overwrite any existing b inding so that we make sure we have a means to exit the app no matter what.
		bindKey(Keypad.KEY_ESCAPE, 0, CommandConstants.POP_TERMINAL_SCREEN);
	}

	/**
	 * Override of @see DefaultSyncCollection.commitData which blocks save from occurring if debug mode settings are
	 * enabled.
	 */
	public void commitData() {
		if (!debugMode)
			super.commitData();

	}
	public boolean isSecureStoreRequired() {
		return false;
	}
}
