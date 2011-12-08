package org.bbssh.ui.components;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;

import org.bbssh.util.Tools;

public class BitmapButtonField extends ClickableButtonField {
	private Bitmap normal;
	private Bitmap focused;
	private Bitmap disabled;
	private int width;
	private int height;

	public BitmapButtonField(String normal) { 
		this(Tools.loadBitmap(normal + ".png"), Tools.loadBitmap(normal + "_f.png"), Tools.loadBitmap(normal + "_d.png"));
	}
	public BitmapButtonField(String normal, String focused, String disabled) {
		this(Tools.loadBitmap(normal), Tools.loadBitmap(focused), Tools.loadBitmap(disabled));

	}

	public BitmapButtonField(Bitmap normal, Bitmap focused, Bitmap disabled) {
		super();
		this.normal = normal;
		this.focused = focused;
		this.disabled = disabled;
		width = normal.getWidth();
		height = normal.getHeight();
//		setMargin(0, 0, 0, 0);
//		setPadding(0, 0, 0, 0);
		// setBorder(BorderFactory.createSimpleBorder(new XYEdges(0, 0, 0, 0)));
		// setBorder(VISUAL_STATE_ACTIVE, BorderFactory.createSimpleBorder(new XYEdges(0, 0, 0, 0)));
	}

	protected void paint(Graphics graphics) {
		Bitmap bitmap = null;
		if (isEditable()) {
			if (isFocus()) {
				bitmap = focused;

			} else {
				bitmap = normal;

			}
		} else {
			bitmap = disabled;
		}

		graphics.drawBitmap(0, 0, bitmap.getWidth(), bitmap.getHeight(), bitmap, 0, 0);
	}

	public int getPreferredWidth() {
		return width;
	}

	public int getPreferredHeight() {
		return height;
	}

	protected void layout(int width, int height) {
		setExtent(this.width, this.height);
	}
	
}
