package org.bbssh.model.macros;

import java.util.Vector;

import org.bbssh.keybinding.PersistableCommandFactory;
import org.bbssh.model.Macro;
import org.bbssh.session.RemoteSessionInstance;

public class MacroExecutor {
	private static MacroExecutor me;

	private MacroExecutor() {

	}

	public synchronized static MacroExecutor getInstance() {
		if (me == null) {
			me = new MacroExecutor();
		}
		return me;

	}

	/**
	 * Executes the macro using the specified session as the target.
	 * 
	 * @param m
	 * @param rsi
	 */
	public void executeMacro(final Macro m, final RemoteSessionInstance rsi) {
		if (rsi == null)
			return;

		if (m.isExecutionDelayed()) {
			new Thread("Macros") {
				public void run() {
					runMacro(m.getCommandVector(), rsi);
				}
			}.start();
		} else {
			runMacro(m.getCommandVector(), rsi);
		}
	}

	private void runMacro(Vector commands, RemoteSessionInstance rsi) {
		int count = commands.size();
		for (int x = 0; x < count; x++) {
			((PersistableCommandFactory) commands.elementAt(x))
					.getBoundCommandInstance().execute(rsi, false);

		}
	}

}
