package org.bbssh.ui.components;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;

/**
 * Simple label field extension that draws a plain border. Unfortuantely we can't use setBorder, BorderFactory, etc
 * because those are not introduced until 4.6.
 * 
 * @author marc
 * 
 */
public class LabelFieldWithBorder extends LabelField {
	private int textColor = 0;
	private int backgroundColor = 0;
	long style;

	public LabelFieldWithBorder(String label, long style) {
		super(label, style);
		this.style = style;
		this.textColor = Color.BLACK;
		this.backgroundColor = Color.WHITE;
	}

	protected void paint(Graphics graphics) {
		graphics.setColor(textColor);
		graphics.setBackgroundColor(backgroundColor);
		super.paint(graphics);
		
		// Now that the painting is done, draw our border - top only.  
		graphics.setColor(0xEEEEEEEE); // Color.LIGHTGRAY is 5.0 only
		graphics.drawLine(0, 0, getWidth(), 0); // drawRect(0, 0, getWidth(), getHeight());

	}
}
