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

package org.bbssh.onlinehelp.model;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "help_topic")
@NamedQueries({@NamedQuery(name = "HelpTopic.findAll", query = "SELECT h FROM HelpTopic h"),
	@NamedQuery(name = "HelpTopic.findById", query = "SELECT h FROM HelpTopic h WHERE h.id = :id"),
	@NamedQuery(name = "HelpTopic.findByName", query =
	"SELECT h FROM HelpTopic h WHERE h.name = :name")})
public class HelpTopic implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @Basic(optional = false)
    @Column(name = "id")
	private String id;
	@Column(name = "name")
	private String name;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "helpTopic")
	private Collection<HelpTopicDetail> helpTopicDetailCollection;

	public HelpTopic() {
	}

	public HelpTopic(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<HelpTopicDetail> getHelpTopicDetailCollection() {
		return helpTopicDetailCollection;
	}

	public void setHelpTopicDetailCollection(Collection<HelpTopicDetail> helpTopicDetailCollection) {
		this.helpTopicDetailCollection = helpTopicDetailCollection;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof HelpTopic)) {
			return false;
		}
		HelpTopic other = (HelpTopic)object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.bbssh.onlinehelp.model.HelpTopic[id=" + id + "]";
	}

}
