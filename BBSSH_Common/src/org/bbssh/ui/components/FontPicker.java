package org.bbssh.ui.components;

import java.io.IOException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.exceptions.FontNotFoundException;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.FontSettings;
import org.bbssh.terminal.fonts.BBSSHFontManager;
import org.bbssh.terminal.fonts.BitmapFontData;
import org.bbssh.terminal.fonts.FontRenderer;
import org.bbssh.ui.screens.TerminalScreen;
import org.bbssh.util.Logger;

public class FontPicker extends PopupScreen implements BBSSHResource, FieldChangeListener {

	ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private BitmapFontData[] bitmapFontChoices;
	private String[] truetypeFontChoices;
	private ObjectChoiceField fontFaceList;
	private BasicEditField fontSizeField;
	private ObjectChoiceField fontSizeList;
	private ObjectChoiceField fontTypeList;
	private FontDisplayField fontDisplay;
	FontSettings originalFS;

	/** Index of the font size field which we will potentially replace */
	private int fontSizeFieldPosition;
	private boolean closedThroughButtons;
	OKCancelControl okCancel = new OKCancelControl();
	FontSettings fs;
	TerminalScreen screen;
	private ButtonField updatePreview;

	public FontPicker(FontSettings fs) {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR), DEFAULT_CLOSE);
		this.fs = fs;
		bitmapFontChoices = BBSSHFontManager.getInstance().getBitmapFonts();
		okCancel.setChangeListener(this);

		updatePreview = new ClickableButtonField(res.getString(SESSION_DTL_LBL_FONT_REFRESH));
		updatePreview.setChangeListener(this);

		this.fs = new FontSettings(fs);
		this.originalFS = fs;
		setupFontFields();

		addFontFields();
		add(okCancel);
		if (fontTypeList.getSelectedIndex() == FontSettings.FONT_TT) {
			insert(updatePreview, okCancel.getIndex() - 1);
		}
		this.fs = new FontSettings(fs);
		// Now that everything is in place- set using the original values , b/c
		// the member may have been updated as a result of changelistener activity during our setup.
		setFontFaceSelection(originalFS.getFontId());
		setFontSize(originalFS.getFontSize());
		if (fontDisplay != null) {
			add(fontDisplay);
		}

	}

	private void addFontFields() {
		add(fontTypeList);
		add(fontFaceList);
		if (fs.getFontType() == FontSettings.FONT_BITMAP) {
			add(fontSizeList);
			fontSizeFieldPosition = fontSizeList.getIndex();
		} else {
			add(fontSizeField);
			fontSizeFieldPosition = fontSizeField.getIndex();
		}

		handleFontTypeChange();

	}

	/**
	 * When font type selection is changed, we need to reset font face name lists, and provide appropriate default
	 * values for font face selection and font size based on the font type.
	 * 
	 * This method will also swap in the correct font size field: pick list for bitmap, or numeric freeform text for
	 * TTF.
	 */
	private void handleFontTypeChange() {
		// If font type changes, reset values to default for the new type.
		Field insField = null;
		Field removeField = null;
		int type = fontTypeList.getSelectedIndex();
		if (type == FontSettings.FONT_BITMAP) {
			fontFaceList.setChoices(bitmapFontChoices);
			insField = fontSizeList;
			removeField = fontSizeField;
		} else {
			fontFaceList.setChoices(truetypeFontChoices);
			insField = fontSizeField;
			removeField = fontSizeList;

		}
		fontFaceList.setSelectedIndex(0);
		if (removeField.getManager() != null) {
			delete(removeField);
		}
		if (insField.getManager() == null) {
			insert(insField, fontSizeFieldPosition);
		}
		if (type == FontSettings.FONT_BITMAP) {
			if (updatePreview.getManager() != null) {
				delete(updatePreview);
			}

		} else {
			if (okCancel.getManager() != null) {
				insert(updatePreview, okCancel.getIndex() - 1);
			}

		}

	}

	private void setupFontFields() {
		String[] typeChoices;

		if (BBSSHFontManager.getInstance().areTruetypeFontsSupported()) {
			typeChoices = res.getStringArray(SESSION_DTL_LIST_FONT_TYPE_CHOICES);
		} else {
			typeChoices = new String[] { res.getString(SESSION_DTL_VALUE_FONT_BITMAP) };
			// if we ever restore settings to a platform that doesn't support TT
			// then set up some defaults to prevent errors.
			if (fs.getFontType() == FontSettings.FONT_TT) {
				fs.setFontType(FontSettings.FONT_BITMAP);
				fs.setFontId((byte) 0);
				fs.setFontSize(FontSettings.DEFAULT_BITMAP_FONT_SIZE);
			}
		}
		bitmapFontChoices = BBSSHFontManager.getInstance().getBitmapFonts();
		truetypeFontChoices = BBSSHFontManager.getInstance().getTTFontNames();

		fontTypeList = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_FONT_TYPE), typeChoices, fs
				.getFontType());
		fontFaceList = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_FONT_NAME), bitmapFontChoices, 0);

		fontSizeList = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_FONT_SIZE), null, 0);
		fontSizeField = new BasicEditField(res.getString(SESSION_DTL_LBL_FONT_SIZE), "16", 2,
				BasicEditField.FILTER_INTEGER | EditField.NO_NEWLINE);
		// Not sure how or why, but for some reason the listener is defaulting to this class... as unlikely as that
		// sounds.
		if (fs.getFontType() == FontSettings.FONT_BITMAP) {
			populateFontSizeList(fs.getFontId(), false);
			// fontSizeList.setSelectedIndex(fs.getFontSize());
		}
		try {
			fontDisplay = new FontDisplayField(BBSSHFontManager.getInstance().getRenderer(fs));
		} catch (FontNotFoundException e) {
			Logger.error("FontNotFoundException in FontPicker.setupFontFields [ " + e.getMessage() + " ] ");
		}
		fontTypeList.setChangeListener(this);
		fontFaceList.setChangeListener(this);

	}

	private void populateFontSizeList(int fontId, boolean setListener) {
		try {
			fontSizeList.setChangeListener(null);
			fontSizeList.setChoices(BBSSHFontManager.getInstance().getBitmapFontData(fontId).getFontRecords());
			if (setListener)
				fontSizeList.setChangeListener(this);
		} catch (FontNotFoundException e) {
			// This should not occur - we'll just log it for now; and if instances occur in the wild we'll look further.
			Logger.error("FontNotFoundException in FontPicker.populateFontSizeList [ " + e.getMessage() + " ] ");
		}
	}

	private void updateFontSettings() {
		fs.setFontType((byte) fontTypeList.getSelectedIndex());
		fs.setFontId((byte) fontFaceList.getSelectedIndex());
		if (fs.getFontType() == FontSettings.FONT_BITMAP) {
			fs.setFontSize((byte) fontSizeList.getSelectedIndex());
		} else {
			fs.setFontSize((byte) Integer.parseInt(fontSizeField.getText()));
		}

	}

	public void save() throws IOException {
		updateFontSettings();
		super.save();

	}

	public void fieldChanged(Field field, int context) {
		if (field == okCancel) {
			closedThroughButtons = true;
			if (context == OKCancelControl.CONTEXT_CANCEL_PRESS) {
				fs = null;

			} else {
				if (isDataValid()) {
					try {
						save();
					} catch (IOException e) {
					}

				} else {
					return;
				}
			}
			close();
		} else if (field == fontTypeList) {
			handleFontTypeChange();
			setFontFaceSelection((byte) 0);
			setFontSize(-1);
			refreshPreview();
		} else if (field == fontFaceList) {
			populateFontSizeList(fontFaceList.getSelectedIndex(), true);
			refreshPreview();
		} else if (field == fontSizeList) {
			refreshPreview();
		} else if (field == updatePreview) {
			refreshPreview();
		}
	}

	private void refreshPreview() {
		updateFontSettings();
		try {
			FontRenderer fr = BBSSHFontManager.getInstance().getRenderer(fs);
			if (fontDisplay == null) {
				fontDisplay = new FontDisplayField(fr);
				insert(fontDisplay, fontSizeFieldPosition + 1);
			} else {
				fontDisplay.setRenderer(fr);
			}

		} catch (FontNotFoundException e) {
			Logger.error("FontNotFoundException in FontPicker.refreshPreview [ " + e.getMessage() + " ] ");
			return;
		}

	}

	/**
	 * Sets currently displayed font size field to the specified value. If the value is out of range, it will substitute
	 * with an appropriate default.
	 * 
	 * @param size font size to change the field value to.
	 */
	private void setFontSize(int size) {
		if (fontSizeField.getManager() == null) {
			try {
				if (!BBSSHFontManager.getInstance().getBitmapFontData(fontFaceList.getSelectedIndex()).isFontSizeValid(
						(byte) size)) {
					size = FontSettings.DEFAULT_BITMAP_FONT_SIZE;
				}
			} catch (FontNotFoundException e) {
				// @todo Again, this shouldn't occur here. we'll need to clean this up.
				Logger.error("FontNotFoundException in FontPicker.setFontSize [ " + e.getMessage() + " ] ");
			}
			try {
				fontSizeList.setSelectedIndex(size);
			} catch (IllegalArgumentException e) {
				Logger.error("Unexpected error - font size idx " + size + " not in list!");
				fontSizeList.setSelectedIndex(FontSettings.DEFAULT_BITMAP_FONT_SIZE);
			}
		} else {
			if (size < 3 || size > 99) {
				size = FontSettings.DEFAULT_TRUETYPE_FONT_SIZE;
			}
			fontSizeField.setText(Integer.toString(size));
		}
	}

	public boolean isDataValid() {
		if (fontTypeList.getSelectedIndex() == FontSettings.FONT_TT) {
			// Should not be possible, as we're now not presenting the option;
			// still if someone preserves settings across a device change,
			// odd things can happen...
			if (!BBSSHFontManager.getInstance().areTruetypeFontsSupported()) {
				Dialog.alert(res.getString(SESSION_DTL_MSG_NO_TRUETYPE));
				fontTypeList.setFocus();
				return false;
			}
		}
		return true;
	}

	private void setFontFaceSelection(byte fontId) {
		if (fontTypeList.getSelectedIndex() == FontSettings.FONT_BITMAP) {
			if (fontId < 0 || fontId >= bitmapFontChoices.length) {
				fontId = 0;
			}
		} else {
			if (fontId < 0 || fontId >= truetypeFontChoices.length) {
				fontId = 0;
			}
		}
		fontFaceList.setSelectedIndex(fontId);
	}

	public FontSettings getUpdatedFontSettings() {
		if (originalFS.equals(fs)) {
			return null;
		}
		return fs;
	}

	public void close() {
		if (!closedThroughButtons)
			fs = null;
		super.close();
	}
}
