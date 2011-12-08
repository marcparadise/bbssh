package org.bbssh.ui.components;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.util.Tools;

/**
 * Simple class for displaying hyperlink fields. 
 * @author marc
 *
 */
public class HyperlinkField extends LabelField {

	private String url;
	private MenuItem itemLink;

	public HyperlinkField(int labelId, long style, int urlId) {
		super(Tools.getStringResource(labelId), style | FOCUSABLE);

		// setFont(Font.getDefault().getFontFamily().getFont(FontFamily.SCALABLE_FONT, 10));

		url = Tools.getStringResource(urlId);

		itemLink = new MenuItem(Tools.getStringResource(BBSSHResource.URLLIST_MENU_OPEN_BROWSER), 0x100000, 0) {
			public void run() {
				// invoke browser with URL
				Browser.getDefaultSession().displayPage(url);
			}
			
		};
	}

	protected void paint(Graphics graphics) {
		graphics.setColor(Color.BLUE);
		super.paint(graphics);
	}
	
	protected void makeContextMenu(ContextMenu menu) {
		super.makeContextMenu(menu);
		if (menu != null) {
			menu.addItem(itemLink);
			menu.setDefaultItem(itemLink);
		}
	}


}
