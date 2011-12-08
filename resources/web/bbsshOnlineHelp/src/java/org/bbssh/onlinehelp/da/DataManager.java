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
package org.bbssh.onlinehelp.da;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.bbssh.onlinehelp.model.HelpTopic;

/**
 * 
 */
public class DataManager {
	static DataManager me = null;
	private EntityManagerFactory emf;

	private DataManager() {
		emf = Persistence.createEntityManagerFactory("bbsshOnlineHelpPU");
	}

	/**
	 * Return data manager instance
	 * @return the one and only data manager.
	 */
	public static DataManager getInstance() {
		if (me == null) {
			me = new DataManager();
		}
		return me;
	}

	public HelpTopic getHelpTopic(String id) {
		return getHelpTopicInternal(id, null);
	}

	public HelpTopic getHelpTopic(String id, String field) {
		EntityManager em = emf.createEntityManager();
		try {
			return getHelpTopicInternal(id, field);
		} catch (NoResultException e) {
			return getHelpTopicInternal("sorry", null);
		} finally {
			em.close();
		}
	}

	private HelpTopic getHelpTopicInternal(String id, String field) {
		EntityManager em = emf.createEntityManager();
		try {
			Query q = em.createNamedQuery("HelpTopic.findById");
			//Query q = em.createQuery("SELECT h FROM HelpTopic h WHERE h.id = :id");
			String key;
			if (id == null || id.length() == 0) {
				key = "sorry";
			} else {
				if (field == null || field.length() == 0) {
					key = id;
				} else {
					key = id + "+" + field;
				}
			}
			q.setParameter("id", key);
			return (HelpTopic)q.getSingleResult();

		} finally {
			em.close();
		}

	}
}
