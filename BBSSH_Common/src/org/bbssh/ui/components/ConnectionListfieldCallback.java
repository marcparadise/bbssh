package org.bbssh.ui.components;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import org.bbssh.model.ConnectionManager;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.net.session.Session;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.util.Tools;

public class ConnectionListfieldCallback implements ListFieldCallback {
	private final int ROW_HEIGHT;
	private final int ICON_SIZE;
	private final int RESERVE_SIZE;
	private static Bitmap iconNotConnected;
	private static Bitmap iconConnected;
	private static Bitmap iconConnecting;
	private static Bitmap iconDisconnected;
	private static Bitmap iconError;
	private static Bitmap iconNotify;
	private Vector activeConnList;

	private Vector getList() {
		if (activeConnList != null) {
			return activeConnList;
		}
		return ConnectionManager.getInstance().getConnections();

	}

	public ConnectionListfieldCallback(Vector list, boolean forceSmall) {
		this(forceSmall);
		activeConnList = list;
	}

	public ConnectionListfieldCallback(boolean forceSmall) {
		if (forceSmall || Display.getWidth() < 300) {
			ROW_HEIGHT = 18;
			ICON_SIZE = 12;

		} else if (PlatformServicesProvider.getInstance().hasTouchscreen()) {
			ROW_HEIGHT = 30;
			ICON_SIZE = 24;
		} else {
			ROW_HEIGHT = 24;
			ICON_SIZE = 18;
		}
		RESERVE_SIZE = ICON_SIZE + 6;
		if (iconNotConnected == null) {
			iconNotConnected = Tools.scaleImage("notconnected-icon.png",
					ICON_SIZE, ICON_SIZE).getBitmap();
			iconConnected = Tools.scaleImage("connected-icon.png", ICON_SIZE,
					ICON_SIZE).getBitmap();
			iconConnecting = Tools.scaleImage("connecting-icon.png", ICON_SIZE,
					ICON_SIZE).getBitmap();
			iconDisconnected = Tools.scaleImage("disconnected-icon.png",
					ICON_SIZE, ICON_SIZE).getBitmap();
			iconError = Tools
					.scaleImage("error-icon.png", ICON_SIZE, ICON_SIZE)
					.getBitmap();
			iconNotify = Tools.scaleImage("notification-icon.png", ICON_SIZE,
					ICON_SIZE).getBitmap();
		}
	}

	public void drawListRow(ListField listField, Graphics graphics, int index,
			int y, int width) {
		Vector v = getList();
		if (index < 0 || index >= v.size())
			return;
		Object o = v.elementAt(index);
		ConnectionProperties prop = null; 
		RemoteSessionInstance inst = null; 
		if (o instanceof ConnectionProperties) { 
			prop = (ConnectionProperties)o; 
			inst = SessionManager.getInstance().getFirstSession(prop);
		} else if (o instanceof RemoteSessionInstance) { 
			inst = (RemoteSessionInstance)o;
			prop = ((RemoteSessionInstance) o).session.getProperties();
			
		}
		drawConnectionDetail(listField, prop, inst, graphics, y, width, index);
	}

	public Object get(ListField listField, int index) {
		Vector v = getList();
		if (index < 0 || index >= v.size())
			return null;

		return v.elementAt(index);
	}

	public int getPreferredWidth(ListField listField) {
		Vector v = getList();
		Font f = listField.getFont();
		int max = listField.getWidth();
		// not determined yet.
		if (max == 0) {
			max = Display.getWidth();
		}
		int advance = 0;
		for (int x = v.size(); x > -1; x--) {
			Math.max(advance, f.getAdvance(v.elementAt(x).toString()));
			if (advance + RESERVE_SIZE > max)
				return max;
		}
		return advance;

	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return 0;
	}

	private void drawConnectionDetail(ListField listField,
			ConnectionProperties p, RemoteSessionInstance i, Graphics g, int y,
			int width, int index) {
		// not connected means that there simply is no connection present for
		// this session.
		Bitmap stateIcon = iconNotConnected;
		int iconY = y + (ROW_HEIGHT / 2) - (ICON_SIZE / 2);
		int reservedSize = ICON_SIZE + 6;
		// Determine if we need notification icons, and how much space to save
		// for them.
		int reserved = reservedSize;
		Bitmap bmp1 = null;
		Bitmap bmp2 = null;
		if (i != null && i.state != null) {
			if (i.state.notified) {
				bmp1 = iconNotify;
				reserved += RESERVE_SIZE;
			}
			if (i.state.error) {
				bmp2 = iconError;
				reserved += RESERVE_SIZE;
			}
		}
		if (i != null) {
			switch (i.session.getConnectionState()) {
			case Session.CONNSTATE_CONNECTED:
				stateIcon = iconConnected;
				break;
			case Session.CONNSTATE_CONNECTING:
			case Session.CONNSTATE_DISCONNECTING:
				stateIcon = iconConnecting; // not a typo
				break;
			case Session.CONNSTATE_DISCONNECTED:
			default:
				stateIcon = iconDisconnected;
				break;

			}
		}

		// Draw text, to a max width of what is provided to us less what we need
		// to save for our notification icons
		// Use the full field widht, and not the row width - as it seems
		// sometimes the row with can be offset.
		g.drawText(p.getName(), 0, y + (ROW_HEIGHT / 2)
				- (listField.getFont().getHeight() / 2), DrawStyle.ELLIPSIS,
				width - reserved);

		// Finally draw our notification icons, if any.
		if (bmp1 != null) {
			g.drawBitmap(width - reserved, iconY, ICON_SIZE, ICON_SIZE, bmp1,
					0, 0);
			reserved -= RESERVE_SIZE;

		}
		if (bmp2 != null) {
			g.drawBitmap(width - reserved, iconY, ICON_SIZE, ICON_SIZE, bmp2,
					0, 0);
			reserved -= RESERVE_SIZE;

		}
		// Always draw this last - it will align to the right edge in all cases.
		g.drawBitmap(width - reserved, iconY, ICON_SIZE, ICON_SIZE, stateIcon,
				0, 0);
	}

	public int getRowHeight() {
		return ROW_HEIGHT;
	}
}
