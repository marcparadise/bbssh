package org.bbssh.ui.components;

import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.i18n.BBSSHResource;
import org.bbssh.util.Tools;

public class MemoryDialog {
	private static boolean remember;
	private static int selection;

	public static synchronized int ask(int questionId, int[] answers, int defaultAnswer) {
		remember = false;
		ResourceBundleFamily res = ResourceBundleFamily.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
		LabelField question = new LabelField(res.getString(questionId));
		CheckboxField check = new CheckboxField(res.getString(BBSSHResource.MSG_CONFIRM_ANS_REMEMBER), false);
		FlowFieldManager fm = new FlowFieldManager(FlowFieldManager.FIELD_HCENTER
				| FlowFieldManager.NO_HORIZONTAL_SCROLL);
		FieldChangeListener lst = new FieldChangeListener() {

			public void fieldChanged(Field field, int context) {
				selection = field.getIndex();
				field.getScreen().close();
			}
		};
		int max = answers.length;
		for (int x = 0; x < max; x++) {
			ClickableButtonField bf = new ClickableButtonField(res.getString(answers[x]));
			bf.setChangeListener(lst);
			fm.add(bf);
		}
		final PopupScreen s = new PopupScreen(new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
				| VerticalFieldManager.VERTICAL_SCROLLBAR));
		s.setFont(Tools.deriveBBSSHDialogFont(s.getFont()));
		s.add(question);
		s.add(check);
		s.add(fm);
		Field f = fm.getField(defaultAnswer);
		if (f != null) {
			f.setFocus();
		}
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				UiApplication.getUiApplication().pushModalScreen(s);
			}
		});
		remember = check.getChecked();
		return selection;

	}

	/**
	 * @return true if the answer to this prompt should be saved.
	 */
	public static boolean getRememberSelection() {
		return remember;
	}

}
