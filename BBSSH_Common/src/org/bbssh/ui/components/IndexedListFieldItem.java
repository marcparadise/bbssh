package org.bbssh.ui.components;

/**
 * A generic class that can be used for populating ListField 
 * controls with data elements that have a numeric id or index 
 * associated with them; and when those items can't be inserted in 
 * an order that guarantees that their list index is the desired value. 
 * @author marc
 *
 */
public class IndexedListFieldItem {
	private String name;
	private int index;

	public IndexedListFieldItem(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public String getName() {
		return this.name;
	}

	public int getIndex() {
		return this.index;
	}

	public String toString() {
		return name;
	}
}
