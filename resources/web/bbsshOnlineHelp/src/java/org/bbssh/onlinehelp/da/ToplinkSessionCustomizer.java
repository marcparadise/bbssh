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
// @todo we need to be inmportant java persistance standard so this can be used across providers. 

import oracle.toplink.essentials.jndi.JNDIConnector;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.tools.sessionconfiguration.SessionCustomizer;

/**
 * 
 */
public class ToplinkSessionCustomizer implements SessionCustomizer {
    public void customize(Session session) throws Exception {

        JNDIConnector jc = (JNDIConnector) session.getLogin().getConnector();
        jc.setLookupType(JNDIConnector.STRING_LOOKUP);
        /*Connector connector =  session.getLogin().getConnector();
        if (connector instanceof JNDIConnector) {
            jc = (JNDIConnector)connector;
        } else {
            jc = new JNDIConnector("java:comp/env/jdbc/khalidine");


        }
        session.getLogin().setConnector(jc);
        session.getLogin().setUsesExternalConnectionPooling(true);
         */
    }
}

