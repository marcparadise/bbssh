package org.bbssh.model;

public interface DataObject {
	public boolean isSyncStateDirty();
	public void setSyncStateDirty(boolean dirty);
	
}
