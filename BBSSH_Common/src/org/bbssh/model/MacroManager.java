package org.bbssh.model;

import java.io.EOFException;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.synchronization.SyncObject;

import org.bbssh.command.CommandConstants;
import org.bbssh.io.SyncBuffer;
import org.bbssh.keybinding.PersistableCommandFactory;
import org.bbssh.util.Logger;

public class MacroManager extends DefaultSyncCollection {
	public static final long MACRO_GUID = 0xea72d52bcfa4f1fL; // org.bbssh.model.MacroManager
	private static MacroManager me;
	private Hashtable macroMap = new Hashtable();

	private MacroManager() {

	}

	public void initialize() {
		// @todo is loadData even needed? That will get called automatically on first use.
		loadData();
	}

	public static synchronized MacroManager getInstance() {
		if (me == null) {
			me = new MacroManager();
		}
		return me;
	}

	public Macro getMacro(String name) {
		return (Macro) macroMap.get(name);
	}

	public void addMacro(Macro macro) {

		if (macroMap.containsKey(macro.getName())) {
			return;
		}
		getDataVector().addElement(macro);
		macroMap.put(macro.getName(), macro);
	}

	public void delMacro(String name) {
		if (macroMap.containsKey(name)) {
			Object m = macroMap.get(name);
			macroMap.remove(name);
			getDataVector().removeElement(m);
		}
	}

	public boolean isExistingMacro(String name) {
		return macroMap.containsKey(name);
	}

	public String getSyncName() {
		return "BBSSH Macros";	
	}

	public int getSyncVersion() {
		return 1;
	}

	protected SyncObject convertImpl(SyncBuffer buffer, int version, int uID, boolean syncDirty) {
		Macro m = new Macro(uID);
		// @todo we can also make sync state dirty an interface member, and haev the caller
		// do this.
		m.setSyncStateDirty(syncDirty);
		try {
			m.setName(buffer.readNextStringField());
			Vector v = m.getCommandVector();
			if (version == 0) {
				// version 0 is a simple thig - has name and text. Convert it to our new version
				// which will just be a SendKeys command.
				v.addElement(new PersistableCommandFactory(0, CommandConstants.SEND_TEXT, buffer
								.readNextStringField()));
			} else {
				int count = buffer.readNextIntField();
				while (count-- > 0) {
					v.addElement(KeyBindingManager.readPersistableCommandFactory(buffer, uID, 0));
				}
			}
			
			macroMap.put(m.getName(), m);
		} catch (EOFException e) {
			Logger.error("MacroManager.convertImpl received unexpected EOF while deserializing macros.");
		}
		return m;
	}

	protected boolean convertImpl(SyncObject object, SyncBuffer buffer, int version) {
		// @todo - this *will* screw up any count that preceded thsi. We may need to have a verifyCount function:
		// for each vector element check non-null and data type?
		if (!(object instanceof Macro))
			return false;

		Macro m = (Macro) object;
		// BEGIN VERSION 0 FIELDS
		buffer.writeField(m.getName());
		// buffer.writeField(m.getText());

		// END VERSION 0 FIELDS
		Vector v = m.getCommandVector();
		int count = v.size();
		buffer.writeField(count);
		for (int x = 0; x < count; x++) {
			KeyBindingManager.writePersistableCommandFactory((PersistableCommandFactory) v.elementAt(x), buffer);
		}

		return true;
	}

	public Vector getMacros() {
		return getDataVector();
	}

	public Vector getTemporaryMacroNamesList() {
		Vector v = getDataVector();
		Vector out = new Vector(v.size());
		// @todo - sorted?
		int max = v.size();
		for (int x = 0; x < max; x++) {
			Object t = v.elementAt(x);
			if (t == null) { // bad data - shouldn't happen, butlet's be safe.
				v.removeElementAt(x);
				x--; // hit this same index again.
				continue;
			}
			out.addElement(((Macro) v.elementAt(x)).getName());
		}
		return out;
	}

	public long getPersistentStoreId() {
		return MACRO_GUID;
	}

	public void resetState() {
		macroMap.clear();
	}

	public Macro duplicateMacro(Macro m) {
		Macro dup = new Macro(m);
		int idx = 1;
		String name = m.getName();
		while (getMacro(name) != null) {
			name = name + idx++;
			// Make a reasonable attempt at a unique name before we give up
			// and make a ridiculous attempt at a unique name.
			if (idx > 10) {
				// through somefluke, we COULD get stuck in a loophere if getInt kept returning us a value
				// of 10 or less... but that's fairly unlikely.
				name = name + dup.getUID();
			}
		}
		dup.setName(name);
		addMacro(dup);
		return dup;

	}
	public boolean isSecureStoreRequired() {
		return false;
	}
}
