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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "help_topic_detail")
@NamedQueries({@NamedQuery(name = "HelpTopicDetail.findAll", query =
	"SELECT h FROM HelpTopicDetail h"),
	@NamedQuery(name = "HelpTopicDetail.findByTopicId", query =
	"SELECT h FROM HelpTopicDetail h WHERE h.helpTopicDetailPK.topicId = :topicId"),
	@NamedQuery(name = "HelpTopicDetail.findBySeq", query =
	"SELECT h FROM HelpTopicDetail h WHERE h.helpTopicDetailPK.seq = :seq")})
public class HelpTopicDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	@EmbeddedId
	protected HelpTopicDetailPK helpTopicDetailPK;
	@Lob
    @Column(name = "text")
	private String text;
	@JoinColumn(name = "topic_id", referencedColumnName = "id", insertable = false, updatable =
    false)
    @ManyToOne(optional = false)
	private HelpTopic helpTopic;

	public HelpTopicDetail() {
	}

	public HelpTopicDetail(HelpTopicDetailPK helpTopicDetailPK) {
		this.helpTopicDetailPK = helpTopicDetailPK;
	}

	public HelpTopicDetail(String topicId, short seq) {
		this.helpTopicDetailPK = new HelpTopicDetailPK(topicId, seq);
	}

	public HelpTopicDetailPK getHelpTopicDetailPK() {
		return helpTopicDetailPK;
	}

	public void setHelpTopicDetailPK(HelpTopicDetailPK helpTopicDetailPK) {
		this.helpTopicDetailPK = helpTopicDetailPK;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public HelpTopic getHelpTopic() {
		return helpTopic;
	}

	public void setHelpTopic(HelpTopic helpTopic) {
		this.helpTopic = helpTopic;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (helpTopicDetailPK != null ? helpTopicDetailPK.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof HelpTopicDetail)) {
			return false;
		}
		HelpTopicDetail other = (HelpTopicDetail)object;
		if ((this.helpTopicDetailPK == null && other.helpTopicDetailPK != null) ||
				(this.helpTopicDetailPK != null &&
				!this.helpTopicDetailPK.equals(other.helpTopicDetailPK))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.bbssh.onlinehelp.model.HelpTopicDetail[helpTopicDetailPK=" + helpTopicDetailPK +
				"]";
	}

}
