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
package org.bbssh.ssh.kex;

/**
 * Represents a negotiated agreement between host and client. 
 */
public final class KexAgreement {
	public String kexAlgorithm;
	public String serverHostKeyAlgorithm;
	public String clientToServerCryptoAlgorithm;
	public String serverToClientCryptoAlgorithm;
	public String MACClientToServer;
	public String MACServerToClient;
	public String compressionClientToServer;
	public String compressionServerToClient;
	public String languageClientToServer;
	public String languageServerToClient;
}
