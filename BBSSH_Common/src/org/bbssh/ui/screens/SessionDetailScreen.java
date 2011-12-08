package org.bbssh.ui.screens;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.net.session.Session;
import org.bbssh.net.session.SshSession;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ssh.kex.KexAgreement;
import org.bbssh.util.Logger;
import org.bbssh.util.Tools;

public class SessionDetailScreen extends MainScreen implements BBSSHResource {
	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	Session session;

	public SessionDetailScreen() {
		super(DEFAULT_CLOSE | DEFAULT_MENU | VERTICAL_SCROLL);
		setTitle(res.getString(SESSION_DETAIL_TITLE));
		RemoteSessionInstance inst = SessionManager.getInstance().activeSession;
		if (inst == null) 
			return; 
		
		session = inst.session;

		KexAgreement a = session.getAgreement();
		if (a == null) {
			add(new LabelField("Connection Security: None"));
		} else {

			ObjectListField lf = new ObjectListField();
			String[] data = Tools.buildConnectionDataString(a);
			lf.set(data);
			add(lf);
			for (int x = 0; x < data.length; x++) {
				Logger.debug(" " + data[x]);
			}
		}
		add(new SeparatorField());
		StringBuffer bf = new StringBuffer(128);

		bf.append("Paint Stats: (FR PR LE LP PBS PC) ")
				.append(inst.state.debugFullRefreshCount).append(' ')
				.append(inst.state.debugPartialRefreshCount).append(' ')
				.append(inst.state.debugLineEvalCount).append(' ')
				.append(inst.state.debugLinePaintCount).append(' ')
				.append(inst.state.debugPaintBackStoreCount).append(' ')
				.append(inst.state.debugPaintCount);

		if (session instanceof SshSession) {
			SshSession s = (SshSession) session;
			add(new LabelField("Packet In/Out: " + s.inputPacketCount+ " / " + s.outputPacketCount));
		}
		add(new LabelField("Redraw Request Delay Total: " + inst.state.debugRedrawRequestWaitTime));
				add(new LabelField("Redraw Exec Delay Total: " + inst.state.debugRedrawStartWaitTime));
		add(new LabelField(bf.toString()));

	}
}
