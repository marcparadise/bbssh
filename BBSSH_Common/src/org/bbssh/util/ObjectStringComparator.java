package org.bbssh.util;

import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.StringComparator;

/**
 * Comparitor that compares toString method of objects in reaching it's determination
 * 
 * @author marc
 * 
 */
public class ObjectStringComparator implements Comparator {
	boolean ignore = false;

	private ObjectStringComparator(boolean ignore) {
		this.ignore = ignore;
	}

	public static ObjectStringComparator CASE_SENSITIVE_COMPARATOR = new ObjectStringComparator(true);
	public static ObjectStringComparator CASE_INSENSITIVE_COMPARATOR = new ObjectStringComparator(false);

	public int compare(Object o1, Object o2) {
		if (o1 == null || o2 == null)
			return 0;
		String s1 = o1.toString();
		String s2 = o2.toString();
		return StringComparator.getInstance(ignore).compare(s1, s2);

	}

}
