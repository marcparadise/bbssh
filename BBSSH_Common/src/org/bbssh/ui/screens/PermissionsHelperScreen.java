package org.bbssh.ui.screens;

import java.io.IOException;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.model.Settings;
import org.bbssh.model.SettingsManager;
import org.bbssh.util.Tools;

/**
 * Examines permissions and reports on required and optional perms that are not possible. Allows user to change perms
 * via prompting.
 * 
 * @author marc
 * 
 */
public class PermissionsHelperScreen extends MainScreen implements BBSSHResource {
	ResourceBundleFamily res = ResourceBundleFamily.getBundle(BUNDLE_ID, BUNDLE_NAME);
	CheckboxField bes = new CheckboxField(res.getString(PERM_HELPER_Q_BES_CONNECTION), false);
	CheckboxField tcp = new CheckboxField(res.getString(PERM_HELPER_Q_INTERNET_CONNECTION), false);
	CheckboxField wifi = new CheckboxField(res.getString(PERM_HELPER_Q_WIFI_CONNECTION), false);
	CheckboxField log = new CheckboxField(res.getString(PERM_HELPER_Q_DEBUG_LOG_FILE), false);
	CheckboxField screen = new CheckboxField(res.getString(PERM_HELPER_Q_SCREEN_CAPTURE), false);
	CheckboxField readkey = new CheckboxField(res.getString(PERM_HELPER_Q_IMPORT_KEY), false);
	CheckboxField savekey = new CheckboxField(res.getString(PERM_HELPER_Q_GENERATE_KEY), false);
	CheckboxField disableBindings = new CheckboxField(res.getString(PERM_HELPER_Q_DISABLE_BINDINGS), false);
	CheckboxField urlGrabber = new CheckboxField(res.getString(PERM_HELPER_Q_URL_GRABBER), false);
	CheckboxField sendEmail = new CheckboxField(res.getString(PERM_HELPER_Q_SEND_SUPPORT_EMAIL), false);

	public PermissionsHelperScreen(boolean launchFailed) {
		setTitle(res, PERM_HELPER_TITLE);

		add(new LabelField(res, PERM_HELPER_INSTRUCTION));
		add(new SeparatorField());
		setFont(Tools.deriveBBSSHDialogFont(getFont()));
		Font bold = getFont().derive(Font.BOLD);

		LabelField networking = new LabelField(res, SESSION_DTL_LBL_NETWORKING);
		LabelField security = new LabelField(res, SETTINGS_LBL_SECURITY);
		LabelField troubleshooting = new LabelField(res, PERM_HELPER_LBL_TROUBLESHOOTING);
		LabelField integration = new LabelField(res, PERM_HELPER_LBL_INTEGRATION);

		networking.setFont(bold);
		security.setFont(bold);
		troubleshooting.setFont(bold);
		integration.setFont(bold);

		setupFieldDefaults();

		add(integration);
		add(new SeparatorField());
		add(disableBindings);
		add(urlGrabber);

		add(networking);
		add(new SeparatorField());
		add(tcp);
		add(wifi);
		add(bes);

		add(security);
		add(new SeparatorField());
		add(readkey);
		add(savekey);

		add(troubleshooting);
		add(new SeparatorField());
		add(screen);
		add(log);
		add(sendEmail);

	}

	private void setupFieldDefaults() {
		ApplicationPermissionsManager mgr = ApplicationPermissionsManager.getInstance();
		boolean phoneOK = false;
		boolean emailOK = false;

		if (mgr.getPermission(ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS) == ApplicationPermissions.VALUE_ALLOW) {
			bes.setChecked(true);
		}
		if (mgr.getPermission(ApplicationPermissions.PERMISSION_WIFI) == ApplicationPermissions.VALUE_ALLOW) {
			wifi.setChecked(true);
		}
		if (mgr.getPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS) == ApplicationPermissions.VALUE_ALLOW) {
			tcp.setChecked(true);
		}
		if (mgr.getPermission(ApplicationPermissions.PERMISSION_FILE_API) == ApplicationPermissions.VALUE_ALLOW) {
			readkey.setChecked(true);
			savekey.setChecked(true);
			log.setChecked(true);
			screen.setChecked(true);
		}

		if (mgr.getPermission(ApplicationPermissions.PERMISSION_PHONE) == ApplicationPermissions.VALUE_ALLOW) {
			disableBindings.setChecked(true);
			phoneOK = true;
		}

		if (mgr.getPermission(ApplicationPermissions.PERMISSION_EMAIL) == ApplicationPermissions.VALUE_ALLOW &&
				mgr.getPermission(ApplicationPermissions.PERMISSION_MEDIA) == ApplicationPermissions.VALUE_ALLOW) {
			emailOK = true;
			sendEmail.setChecked(true);
		}
		if (phoneOK && emailOK
				&& mgr.getPermission(ApplicationPermissions.PERMISSION_PIM) == ApplicationPermissions.VALUE_ALLOW) {
			urlGrabber.setChecked(true);
		}

	}

	public void save() throws IOException {
		ApplicationPermissions perm = new ApplicationPermissions();
		perm.addPermission(ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION);
		if (bes.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_INTERNAL_CONNECTIONS);
		if (wifi.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_WIFI);
		if (tcp.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS);
		if (readkey.getChecked() || savekey.getChecked() || log.getChecked() || screen.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
		if (sendEmail.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_MEDIA);
		if (urlGrabber.getChecked() || disableBindings.getChecked())
			perm.addPermission(ApplicationPermissions.PERMISSION_PHONE);
		if (urlGrabber.getChecked()) {
			perm.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
			perm.addPermission(ApplicationPermissions.PERMISSION_PIM);
		}
		if (perm.getPermissionKeys() != null && perm.getPermissionKeys().length > 0) {
			Dialog.alert(res.getString(PERM_HELPER_MSG_COMING_NEXT));
			if (BBSSHApp.inst().requestPermissions(perm)) {
				Dialog.ask(Dialog.D_OK, res.getString(PERM_HELPER_MSG_PERM_SUCCESS));
			} else {
				Dialog.ask(Dialog.D_OK, res.getString(PERM_HELPER_PERM_FAILED));
			}
		}

		setDirty(false);
	}

	public boolean onClose() {
		SettingsManager.getSettings().setRememberOption(Settings.REMEMBER_PERM_SHOWN, true);
		return super.onClose();
	}
}
