package org.bbssh.ui.screens;

import java.io.IOException;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.ConnectionProperties;
import org.bbssh.model.FontSettings;
import org.bbssh.net.ConnectionHelper;
import org.bbssh.platform.PlatformServicesProvider;
import org.bbssh.terminal.TerminalStateData;
import org.bbssh.ui.components.ClickableButtonField;
import org.bbssh.ui.components.FontPicker;

public class ConnectionPropertiesScreen extends MainScreen implements FieldChangeListener, BBSSHResource {
	// @todo ultimately make a collapsibleFieldManager to contain thesse --
	// allowing us to add field groups and collapse them.
	protected VerticalFieldManager baseFields = new VerticalFieldManager();
	protected VerticalFieldManager appearanceFields = new VerticalFieldManager();
	protected VerticalFieldManager networkingFields = new VerticalFieldManager();

	protected ConnectionProperties prop;
	protected ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	protected String[] COLOR_TABLE = res.getStringArray(BBSSHResource.SESSION_DTL_LIST_COLORS);
	protected boolean saved;
	protected LabelField basicHeader;
	protected LabelField networkingHeader;
	protected BasicEditField keepAliveDurationField;
	protected LabelField appearanceHeader;
	protected ClickableButtonField chooseFont;
	protected BasicEditField termTypeField;
	protected ObjectChoiceField fnkeyMode;
	protected ObjectChoiceField bgColorField;
	protected ObjectChoiceField fgColorField;
	protected BasicEditField termColField;
	protected BasicEditField termRowField;
	protected BasicEditField scrollbackLines;
	protected ObjectChoiceField connectionTypeField;
	protected HorizontalFieldManager fontHFM;
	protected CheckboxField useWifiIfAvailable;
	protected CheckboxField altPrefixMeta;
	protected FontSettings fs;

	/** Connection type */
	/** Delimiter between host and port */
	protected CheckboxField useHybridInputMode;
	protected BasicEditField besTimeout;

	public ConnectionPropertiesScreen(ConnectionProperties prop) {
		super(Screen.DEFAULT_CLOSE);
		this.prop = prop;
		fs = prop.getFontSettings();
		createFields();
		addFields();
		setTitle(res.getString(SESSION_DTL_TITLE_0));

		add(baseFields);
		add(new SeparatorField());
		add(appearanceFields);
		add(new SeparatorField());
		add(networkingFields);

	}

	/**
	 * Creates the fields.
	 */
	protected void createFields() {
		Font boldFont = getFont().derive(Font.BOLD);

		besTimeout = new BasicEditField(res.getString(SESSION_DTL_LBL_BES_TIMEOUT),
				String.valueOf(prop.getBESTimeout()), 5, Field.EDITABLE | BasicEditField.NO_NEWLINE
						| BasicEditField.NON_SPELLCHECKABLE | BasicEditField.FILTER_NUMERIC);

		// Networking
		networkingHeader = new LabelField(res.getString(SESSION_DTL_LBL_NETWORKING));
		networkingHeader.setFont(boldFont);
		connectionTypeField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_SESSION_TYPE),
				res.getStringArray(SESSION_DTL_LIST_CONNECT_CHOICES), prop.getConnectionType());
		useWifiIfAvailable = new CheckboxField(res.getString(SESSION_DTL_LBL_USE_WIFI_IF_AVAILABLE),
				prop.getUseWifiIfAvailable());

		keepAliveDurationField = new BasicEditField(res.getString(SESSION_DTL_LBL_KEEPALIVE_DURATION),
				Integer.toString(prop.getKeepAliveTime()), 5, BasicEditField.FILTER_INTEGER
						| BasicEditField.NON_SPELLCHECKABLE);

		appearanceHeader = new LabelField(res.getString(SESSION_DTL_LBL_APPEARANCE));
		appearanceHeader.setFont(boldFont);

		FontSettings fs = prop.getFontSettings();
		fontHFM = new HorizontalFieldManager();
		chooseFont = new ClickableButtonField();
		chooseFont.setChangeListener(this);
		fontHFM.add(new LabelField(res.getString(SESSION_DTL_LBL_FONT)));
		fontHFM.add(chooseFont);

		// Free string, 32
		termTypeField = new BasicEditField(res.getString(SESSION_DTL_LBL_TERMINAL_TYPE), prop.getTermType(), 32,
				Field.EDITABLE | BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);

		fnkeyMode = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_FN_KEY_WORKAROUND),
				res.getStringArray(SESSION_DTL_LIST_FN_KEY_MODES), prop.getFunctionKeyMode());

		if (!PlatformServicesProvider.getInstance().isReducedLayout()) {

			useHybridInputMode = new CheckboxField(res.getString(SESSION_DTL_LBL_USE_HYBRID),
					prop.getDefaultInputMode() == TerminalStateData.TYPING_MODE_HYBRID);
		}
		
		bgColorField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_BG_COLOR), COLOR_TABLE, prop.getBackgroundColorIndex());
		fgColorField = new ObjectChoiceField(res.getString(SESSION_DTL_LBL_FG_COLOR), COLOR_TABLE, prop.getForegroundColorIndex());

		
		termColField = new BasicEditField(res.getString(SESSION_DTL_LBL_COLS),
				Integer.toString(prop.getTerminalCols()), 3, Field.EDITABLE | BasicEditField.FILTER_NUMERIC
						| BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);
		termRowField = new BasicEditField(res.getString(SESSION_DTL_LBL_ROWS),
				Integer.toString(prop.getTerminalRows()), 3, Field.EDITABLE | BasicEditField.FILTER_NUMERIC
						| BasicEditField.NO_NEWLINE | BasicEditField.NON_SPELLCHECKABLE);

		scrollbackLines = new BasicEditField(res.getString(SESSION_DTL_LBL_SCROLLBACK), Integer.toString(prop
				.getScrollbackLines()), 3, Field.EDITABLE | BasicEditField.FILTER_NUMERIC | BasicEditField.NO_NEWLINE
				| BasicEditField.NON_SPELLCHECKABLE);

		altPrefixMeta = new CheckboxField(res.getString(SESSION_DTL_ALT_PREFIX_META), prop.getAltPrefixesMeta());

		// Finally, update some labels to reflect current state.
		updateFontButtonText(fs);
		// Refresh various network releated fields based on selected connection type
		fieldChanged(connectionTypeField, 1);

		connectionTypeField.setChangeListener(this);

	}

	private void updateFontButtonText(FontSettings fs) {
		String s = fs.toString();
		if (s == null || s.length() == 0) {
			s = res.getString(GEN_LBL_CLICK_TO_CHOOSE);
		}
		chooseFont.setLabel(s);
	}

	/**
	 * Adds fields to the screen
	 * 
	 * @todo split into ssh vs telnet -for validation too. Provide a ConnectionPropertiesValidator?
	 */
	protected void addFields() {

		appearanceFields.add(appearanceHeader);
		appearanceFields.add(fontHFM);
		appearanceFields.add(termTypeField);
		appearanceFields.add(fnkeyMode);
		appearanceFields.add(altPrefixMeta);
		if (!PlatformServicesProvider.getInstance().isReducedLayout())
			appearanceFields.add(useHybridInputMode);
		appearanceFields.add(scrollbackLines);
		appearanceFields.add(bgColorField);
		appearanceFields.add(fgColorField);
		appearanceFields.add(termColField);
		appearanceFields.add(termRowField);

		networkingFields.add(networkingHeader);
		networkingFields.add(connectionTypeField);
		networkingFields.add(useWifiIfAvailable);
		networkingFields.add(keepAliveDurationField);
		networkingFields.add(besTimeout);

	}

	public void fieldChanged(Field field, int context) {
		if (field == chooseFont) {
			FontPicker p = new FontPicker(prop.getFontSettings());
			UiApplication.getUiApplication().pushModalScreen(p);
			FontSettings s = p.getUpdatedFontSettings();
			if (s != null) {
				fs = s;
				updateFontButtonText(fs);
			}

		} else if (field == connectionTypeField) {
			// If wifi conection is selected, use wifi flag is meaningless as that's the only OK connection type.
			if (connectionTypeField.getSelectedIndex() == ConnectionHelper.CONNECTION_TYPE_WIFI) {
				useWifiIfAvailable.setEditable(false);
				useWifiIfAvailable.setChecked(false);
			} else {
				useWifiIfAvailable.setEditable(true);
			}
			if (connectionTypeField.getSelectedIndex() == ConnectionHelper.CONNECTION_TYPE_BES) {
				besTimeout.setEditable(true);
			} else {
				besTimeout.setEditable(false);
			}
		}
	}

	public void save() throws IOException {
		byte connType = (byte) connectionTypeField.getSelectedIndex();

		prop.setConnectionType(connType);
		prop.setFontSettings(fs);
		prop.setTermType(termTypeField.getText());

		int sel = bgColorField.getSelectedIndex();
		prop.setBackgroundColorIndex(sel == -1 ? 0 : sel);

		sel = fgColorField.getSelectedIndex();
		prop.setForegroundColorIndex(sel == -1 ? 7 : sel);

		prop.setTerminalCols(Short.parseShort(termColField.getText()));
		prop.setTerminalRows(Short.parseShort(termRowField.getText()));
		prop.setScrollbackLines(Short.parseShort(scrollbackLines.getText()));
		prop.setKeepAliveTime(Integer.parseInt(keepAliveDurationField.getText()));
		prop.setFunctionKeyMode((byte) fnkeyMode.getSelectedIndex());
		if (!PlatformServicesProvider.getInstance().isReducedLayout()) {
			prop.setDefaultInputMode(useHybridInputMode.getChecked() ? TerminalStateData.TYPING_MODE_HYBRID
					: TerminalStateData.TYPING_MODE_DIRECT);
		}
		prop.setUseWifiIfAvailable(useWifiIfAvailable.getChecked());
		prop.setBESTimeout(Integer.parseInt(besTimeout.getText()));
		prop.setAltPrefixesMeta(altPrefixMeta.getChecked());
		super.save();
	}

	protected void promptPermissions(int connType, int msg) {
		int requiredPermType = ConnectionHelper.getPermissionForConnType(connType);
		BBSSHApp.inst().requestPermission(requiredPermType, msg);
	}

	public boolean isDataValid() {
		if (besTimeout.isEditable() && Integer.parseInt(besTimeout.getText()) < 0) {
			Status.show(res.getString(MSG_BES_TIMEOUT_INVALID));
			besTimeout.setFocus();
			return false;
		}
		if (termColField.getTextLength() == 0)
			termColField.setText("0");
		if (termRowField.getTextLength() == 0)
			termRowField.setText("0");
		if (scrollbackLines.getTextLength() == 0)
			scrollbackLines.setText("0");

		promptPermissions(connectionTypeField.getSelectedIndex(), MSG_NET_PERMISSIONS_MISSING_ADD_NOW);
		if (useWifiIfAvailable.isEditable() && useWifiIfAvailable.getChecked()) {
			promptPermissions(ConnectionHelper.CONNECTION_TYPE_WIFI, MSG_NET_PERMISSIONS_MISSING_WIFI);
		}

		return true;
	}

	protected boolean onSave() {
		saved = true;
		return super.onSave();
	}

	/**
	 * Returns true if the session details were validated and saved.
	 * 
	 * @return save staet
	 */
	public boolean isSaved() {
		return saved;
	}

	public ConnectionProperties getEditedProperties() {
		return prop;
	}

}