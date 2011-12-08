package org.bbssh.command;

import org.bbssh.session.RemoteSessionInstance;

public class ScrollDown extends ScrollCommand {

	public boolean execute(RemoteSessionInstance inst, Object parameter) {
		if (!(parameter instanceof Integer))
			return false; 
		inst.scrollViewVertical(((Integer)parameter).intValue(), true);
		return true;
	}

	public int getDescriptionResId() {
		return CMD_DESC_SCROLL_DOWN_LINES;
	}

	public int getId() {
		return CommandConstants.SCROLL_DOWN_LINES;
	}

	public int getNameResId() {
		return CMD_NAME_SCROLL_DOWN_LINES;
	}


}
