package org.bbssh.ui.components;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;

/**
 * Shamlesslessy borrowed from Jonathan Fisher's site. The original is offline, but here's the source:
 * 
 * http://web.archive.org/web/20071013170747/jonathanhfisher.co.uk/b2/?p=19
 * 
 * @author marc
 * 
 */
public class HeaderBar extends Field implements DrawStyle {
	private Font headerFont = SmallFont();
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	// private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private String timeString;
	private String dateString;
	private String title;
	private boolean showSignal;
	private boolean showBattery;
	// private boolean showDate;
	private boolean showTime;
	private boolean showTitle;
	private int fieldHeight;
	private int fontColor;
	private int backgroundColor;
	private int batteryBackground;
	private Timer headerTimer = new Timer();
	private TimerTask headerTask;
	private boolean drawSeparator = true;

	public HeaderBar(String _title) {
		super(Field.NON_FOCUSABLE);
		title = _title;
		showSignal = true;
		showBattery = true;
		// showDate = true;
		showTime = true;
		showTitle = true;
		fieldHeight = 14;
		fontColor = 0x888888;
		backgroundColor = 0;
		batteryBackground = 0x999999;

		headerTask = new TimerTask() {
			public void run() {
				invalidate();
			}
		};

		headerTimer.scheduleAtFixedRate(headerTask, 0, 5000);
	}

	public void setTitle(String _title) {
		title = _title;
		invalidate();
	}

	public void setFontColor(int _fontColor) {
		fontColor = _fontColor;
		invalidate();
	}

	public void setBatteryBackground(int _batteryBackground) {
		batteryBackground = _batteryBackground;
		invalidate();
	}

	public void setBackgroundColor(int _backgroundColor) {
		backgroundColor = _backgroundColor;
		invalidate();
	}

	public void showSignal(boolean bool) {
		showSignal = bool;
		invalidate();
	}

	public void showBattery(boolean bool) {
		showBattery = bool;
		invalidate();
	}

	// public void showDate(boolean bool) {
	// showDate = bool;
	// invalidate();
	// }

	public void showTime(boolean bool) {
		showTime = bool;
		invalidate();
	}

	public void showTitle(boolean bool) {
		showTitle = bool;
		invalidate();
	}

	protected void layout(int width, int height) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}

	public int getPreferredWidth() {
		return Display.getWidth();
	}

	public int getPreferredHeight() {
		return fieldHeight;
	}

	protected void paint(Graphics graphics) {
		graphics.setFont(headerFont);
		int graphicsDiff = 0;

		// if (backgroundColor != 0) {
		graphics.setColor(backgroundColor);
		graphics.fillRect(0, 0, this.getPreferredWidth(), this.getPreferredHeight());
		// }

		if (showSignal) {
			graphicsDiff = graphicsDiff + 28;
			graphics.setColor(0x999999);
			// draw blank background
			graphics.fillRect(this.getPreferredWidth() - 26, 8, 6, 4);
			graphics.fillRect(this.getPreferredWidth() - 21, 6, 6, 6);
			graphics.fillRect(this.getPreferredWidth() - 16, 4, 6, 8);
			graphics.fillRect(this.getPreferredWidth() - 11, 2, 6, 10);
			graphics.fillRect(this.getPreferredWidth() - 6, 0, 6, 12);
			int signalLevel = RadioInfo.getSignalLevel();
			if (signalLevel > -77) {
				// 5 bands
				graphics.setColor(0xEFEFEF);
				graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);
				graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);
				graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);
				graphics.fillRect(this.getPreferredWidth() - 10, 3, 4, 8);
				graphics.fillRect(this.getPreferredWidth() - 5, 1, 4, 10);
			} else if (signalLevel > -86) {
				// 4 bands
				graphics.setColor(0xEFEFEF);
				graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);
				graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);
				graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);
				graphics.fillRect(this.getPreferredWidth() - 10, 3, 4, 8);
			} else if (signalLevel > -92) {
				// 3 bands
				graphics.setColor(0xEFEFEF);
				graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);
				graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);
				graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);
			} else if (signalLevel > -101) {
				// 2 bands
				graphics.setColor(0xefec00);
				graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);
				graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);
			} else if (signalLevel > -120) {
				// 1 band
				graphics.setColor(0xff2200);
				graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);
			}
		}
		if (showBattery) {
			int batteryLevel = DeviceInfo.getBatteryLevel();
			graphics.setColor(batteryBackground);
			graphics.fillRect(this.getPreferredWidth() - 23 - graphicsDiff, 3, 20, 8);
			graphics.fillRect(this.getPreferredWidth() - 3 - graphicsDiff, 5, 1, 4);
			if (batteryLevel > 75) {
				graphics.setColor(0x18e300);
			} else if (batteryLevel > 50) {
				graphics.setColor(0x91dc00);
			} else if (batteryLevel > 25) {
				graphics.setColor(0xefec00);
			} else {
				graphics.setColor(0xff2200);
			}
			double powerLong = ((18.00 / 100) * batteryLevel);
			int power = (int) powerLong;
			graphics.fillRect(this.getPreferredWidth() - 22 - graphicsDiff, 4, power, 6);
			graphicsDiff = graphicsDiff + 24;
		}

		graphics.setColor(fontColor);

		if (showTime) {
			timeString = " " + timeFormat.formatLocal(System.currentTimeMillis()) + " ";
		} else {
			timeString = "";
		}

		// if (showDate) {
		// dateString = " " + dateFormat.formatLocal(System.currentTimeMillis()) + " ";
		// } else {
		dateString = "";
		// }

		graphics.drawText(dateString + timeString, this.getPreferredWidth()
				- headerFont.getAdvance(dateString + timeString) - graphicsDiff, 1);

		if (showTitle) {
			int limit = 0;
			if (showSignal)
				limit = limit + 28;
			if (showBattery)
				limit = limit + 25;
			if (showTime)
				limit = limit + headerFont.getAdvance(timeString);
			// if (showDate)
			// limit = limit + headerFont.getAdvance(dateString);
			if (headerFont.getAdvance(title) > this.getPreferredWidth() - limit) {
				int elippsy = headerFont.getAdvance("...");
				int availableWidth = this.getPreferredWidth() - limit - elippsy;
				String _txt = "";
				String shorterTitle = "";
				for (int i = 1; i < title.length(); i++) {
					_txt = title.substring(0, i);
					if (headerFont.getAdvance(_txt) < availableWidth) {
						shorterTitle = _txt;
					}
				}
				title = shorterTitle + "...";
			}
			graphics.drawText(title, 1, 0);
		}

		graphics.setColor(0x888888);

		int h = getHeight() - 1;
		if (drawSeparator)
			graphics.drawLine(0, h, getWidth(), h);
	}

	public static Font SmallFont() {
		try {
			FontFamily theFam = FontFamily.forName("SYSTEM");
			return theFam.getFont(net.rim.device.api.ui.Font.PLAIN, 11);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void setDrawSeparator(boolean drawSeparator) {
		this.drawSeparator = drawSeparator;
	}
}