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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 
 */
@Embeddable
public class HelpTopicDetailPK implements Serializable {
	@Basic(optional = false)
    @Column(name = "topic_id")
	private String topicId;
	@Basic(optional = false)
    @Column(name = "seq")
	private short seq;

	public HelpTopicDetailPK() {
	}

	public HelpTopicDetailPK(String topicId, short seq) {
		this.topicId = topicId;
		this.seq = seq;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (topicId != null ? topicId.hashCode() : 0);
		hash += (int)seq;
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof HelpTopicDetailPK)) {
			return false;
		}
		HelpTopicDetailPK other = (HelpTopicDetailPK)object;
		if ((this.topicId == null && other.topicId != null) ||
				(this.topicId != null && !this.topicId.equals(other.topicId))) {
			return false;
		}
		if (this.seq != other.seq) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "org.bbssh.onlinehelp.model.HelpTopicDetailPK[topicId=" + topicId + ", seq=" + seq +
				"]";
	}

}
