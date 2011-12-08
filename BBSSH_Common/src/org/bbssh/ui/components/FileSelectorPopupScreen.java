package org.bbssh.ui.components;

/**
 * Modified from the BB samples
 * @author marc
 *
 */
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.util.Tools;

/**
 * A PopupScreen with a file browser allowing for file selection.
 * 
 * @todo refactor a base class which displays three rows - top, list, bottom w/ OK/Cancel buttons.
 */

public class FileSelectorPopupScreen extends PopupScreen implements BBSSHResource, FieldChangeListener {
	public static final int TYPE_OPEN = 0;
	public static final int TYPE_SAVE_AS = 1;
	public static final int TYPE_SELECT_FOLDER = 2;

	private ResourceBundle res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	private String currentPath; // The current path;
	private ObjectListField list; // Lists fields and directories.
	private BasicEditField fileNameField;
	// @todo - simple UI component, OKCancelPair - b/c this comes up a LOT...
	private ClickableButtonField okButton = new ClickableButtonField(res.getString(GENERAL_LBL_OK));
	private ClickableButtonField cancelButton = new ClickableButtonField(res.getString(GENERAL_LBL_CANCEL));
	int type = TYPE_OPEN;
	HorizontalFieldManager topRowManager;
	FlowFieldManager listFieldManager;
	VerticalFieldManager bottomRowManager;
	private String startPath;
	private String defaultSaveAsFileName;

	/**
	 * Create a new list field that notifies us any time the selected line changes.
	 * 
	 * @return
	 */
	private ObjectListField createListField() {
		// @todo make this into a custom component/list field type.
		return new ObjectListField() {
			// Some extra pains here - we want to make sure our listbox
			// doesn't expand to push the buttons off the bottom of the page,
			// so we need to contain it in its own field manager that is bounded
			// by our top and bottom managers.
			// @todo idea: by using gridfieldmanager in the middle, that will give us an
			// effective BorderLayout...
			// Customizing the list field so that it notifies on selection change -- and also
			// changes focus on left/right movement.
			int oldSel = -1;

			public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
				// Sloppy and not 100% accurate, but we'll be using this to indicate to the
				// selection has changed.
				int x = getSelectedIndex();
				if (x != oldSel) {
					oldSel = x;
					FieldChangeListener l = getChangeListener();
					if (l != null) {
						l.fieldChanged(this, 10);
					}

				}
				super.drawListRow(listField, graphics, index, y, width);
			}

			protected boolean navigationMovement(int dx, int dy, int status, int time) {
				// We need to perform any movement required first, so that our selection is updated appropriately.
				// @todo - do we also need to override touch event in the Storm version? That'll get messy...
				if (dx > 0) {
					FileSelectorPopupScreen.this.bottomRowManager.getField(0).setFocus();
					return true;
				}
				return super.navigationMovement(dx, dy, status, time);
			};
		};
	}

	public FileSelectorPopupScreen() {
		super(new VerticalFieldManager(VERTICAL_SCROLL | VERTICAL_SCROLLBAR));
	}

	private void initialize() {
		list = createListField();
		topRowManager = new HorizontalFieldManager();
		listFieldManager = new FlowFieldManager() {
			protected void sublayout(int w, int h) {
				super.sublayout(w, h);
				setExtent(w, (list.getRowHeight() * 6)); // 6 visible rows
			}
		};
		bottomRowManager = new VerticalFieldManager();
		listFieldManager.add(list);

		fileNameField = new BasicEditField(res.getString(FILE_SELECTION_LBL_SAVE_AS), "", 32, BasicEditField.NO_NEWLINE
				| BasicEditField.NON_SPELLCHECKABLE | BasicEditField.FILTER_FILENAME) {
			protected void onFocus(int direction) {
				// passing value < 0 will force cursor to end of line.
				super.onFocus(-1);
			}
		};
		if (defaultSaveAsFileName != null)
			fileNameField.setText(defaultSaveAsFileName);

		prepScreen(startPath);

	}

	/**
	 * Display the screen, prompting the user to pick a file.
	 * 
	 * @return current directory if the user is still browsing for a file, the selected file if the user has chosen one
	 *         or null if the user dismissed the screen.
	 */
	public String pickFile() {
		initialize();
		customizeUI();
		if (!BBSSHApp.inst().requestPermission(
				ApplicationPermissions.PERMISSION_FILE_API, BBSSHResource.MSG_PERMISSIONS_MISSING_FILE_ACCESS_USER)) {
			return "";
		}

		UiApplication.getUiApplication().pushModalScreen(this);
		if (currentPath == null) {
			return "";
		}
		if (type == TYPE_SAVE_AS) {
			return "file:///" + currentPath + fileNameField.getText();
		}
		return "file:///" + currentPath;
	}

	/**
	 * Override this method in os-specific implementations to further customize the UI.
	 */
	protected void customizeUI() {

	}

	protected ObjectListField getListField() {
		return list;
	}

	/**
	 * Set up the screens initial fields, and populate the list using the given path.
	 * 
	 * @param path initial path. If null,
	 */
	private void prepScreen(String path) {
		int title = FILE_SELECTION_TITLE_OPEN;
		if (type == TYPE_SAVE_AS) {
			title = FILE_SELECTION_TITLE_SAVE;
		} else if (type == TYPE_SELECT_FOLDER) {
			title = FILE_SELECTION_TITLE_FOLDER;
		}
		topRowManager.add(new LabelField(res.getString(title)));
		if (type == TYPE_SAVE_AS) {

			bottomRowManager.add(fileNameField);
		}
		// @todo use the new OKCancel control.
		HorizontalFieldManager hfm = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
		hfm.add(okButton);
		hfm.add(cancelButton);
		bottomRowManager.add(hfm);
		add(topRowManager);
		add(listFieldManager);
		add(bottomRowManager);
		updateList(path);

		list.setChangeListener(null);
		list.setChangeListener(this);
		okButton.setChangeListener(this);
		cancelButton.setChangeListener(this);

	}

	private Vector getFilesystemRoots() {
		Vector filesVector = new Vector();
		Enumeration fileEnum = FileSystemRegistry.listRoots();
		while (fileEnum.hasMoreElements()) {
			filesVector.addElement((Object) fileEnum.nextElement());
		}
		return filesVector;
	}

	/**
	 * Reads all of the files and directories in a given path, applying any extension or directory filter if provided
	 * and based on current mode.
	 * 
	 * @param path
	 * @return vector of all matching files
	 */
	private Vector readFiles(String path) {
		Enumeration fileEnum;
		Vector filesVector = new Vector();
		currentPath = path;
		if (path == null) {
			currentPath = null;
			return getFilesystemRoots();
		} else {
			// Read the files and directories for the current path.
			try {
				FileConnection fc = (FileConnection) Connector.open("file:///" + path);
				fileEnum = fc.list();
				String currentFile;
				while (fileEnum.hasMoreElements()) {
					currentFile = (String) fileEnum.nextElement();
					switch (type) {
						case TYPE_OPEN:
						case TYPE_SAVE_AS:
							filesVector.addElement(currentFile);
							break;
						case TYPE_SELECT_FOLDER:
							if (currentFile.endsWith("/")) {
								filesVector.addElement(currentFile);
							}
							break;
					}
				}
				fc.close();
			} catch (Throwable ex) {
				// When all else fails...
				currentPath = null;
				return getFilesystemRoots();
			}
		}
		return filesVector;
	}

	/**
	 * Invoked when the user picks an entry in the list field.
	 */
	private void doSelection() {
		Object focus = getLeafFieldWithFocus();
		if (focus == cancelButton) {
			doCancelProcessing();
			return;
		} else if (focus == okButton) {
			doOKProcessing();
			return;
		} else if (focus != list) {
			return;
		}

		// Determine the current path.
		String path = buildPath();
		if (path != null && path.equals("*?*")) {
			if (type != TYPE_SELECT_FOLDER) {
				this.close();
				return;
			}
		}
		if (path == null || path.endsWith("/")) {
			updateList(path);
		}

	}

	/**
	 * Updates the entries in the ObjectListField.
	 * 
	 * @param path
	 */
	private void updateList(String path) {
		// Read all files and directories in the path.
		Vector fileList = readFiles(path);

		// Create an array from the Vector.
		Object fileArray[] = fileVectorToArray(fileList);

		// Update the field with the new files.
		list.setChangeListener(null);
		list.set(fileArray);
		list.setChangeListener(this);
	}

	// Build a String that contains the full path of the user's selection.
	// If a file has been selected, close this screen.
	// Returns *?* if the user has selected a file.
	private String buildPath() {
		String newPath = (String) list.get(list, list.getSelectedIndex());

		if (newPath.equals("..")) {
			// Go up one level (if we can) by removing the trailing directory.
			newPath = currentPath.substring(0, currentPath.length() - 1);
			int lastSlash = newPath.lastIndexOf('/');
			if (lastSlash == -1) {
				newPath = null;
			} else {
				newPath = newPath.substring(0, lastSlash + 1);
			}
		} else if (newPath.endsWith("/")) {
			// If the path ends with /, a directory was selected.
			// Prefix the _currentPath if it is not null (not in the
			// root directory).
			if (currentPath != null) {
				newPath = currentPath + newPath;
			}
		} else {
			// A file was selected.
			currentPath += newPath;

			// Return *?* to stop the screen update process.
			newPath = "*?*";
		}

		return newPath;
	}

	/**
	 * Saves the files and directories listed in vector format into an object array.
	 * 
	 * @param filesVector vector of files.
	 * @return object arra of the files, properly padded with "parent" directory indicator if required.
	 */
	private Object[] fileVectorToArray(Vector filesVector) {
		Object[] files;
		// If not in the root, add ".." to the top of the array.
		if (currentPath == null || currentPath.length() == 0) {
			files = Tools.vectorToArray(filesVector);
		} else {
			files = Tools.vectorToArray(filesVector, 1);
			files[0] = (Object) ("..");
		}
		return files;
	}

	// Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			doCancelProcessing();
			return true;
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}

	private void doOKProcessing() {
		// @todo we also need to make sure they aren't selecting a directory that's
		// at the top level... or that they haven't write access to.
		// @todo prompt for confirmation if they select any existing file.
		if (currentPath == null || currentPath.length() == 0 || (type != TYPE_OPEN && !currentPath.endsWith("/"))) {
			Dialog.ask(Dialog.D_OK, res.getString(FILE_SELECTION_MSG_INVALID_DIR));
			return;
		}
		if (type == TYPE_SAVE_AS) {
			if (fileNameField.getTextLength() == 0) {
				Dialog.ask(Dialog.D_OK, res.getString(FILE_SELECTION_MSG_NO_FILE));
				return;
			}
			if (fileNameField.getText().indexOf('/') > -1) {
				Dialog.ask(Dialog.D_OK, res.getString(FILE_SELECTION_MSG_INVALID_FILE));
				return;
			}
		}
		this.close();

	}

	private void doSelectionChangedProcessing() {
		if (type != TYPE_SAVE_AS) {
			return;
		}
		int idx = list.getSelectedIndex();
		if (idx == -1) {
			return;
		}
		String name = (String) list.get(list, idx);
		if (name.equals("..")) {
			return;
		}
		if (name.endsWith("/")) {
			return;
		}
		fileNameField.setText(name);

	}

	private void doCancelProcessing() {
		currentPath = null;
		this.close();

	}

	public void fieldChanged(Field field, int context) {
		if (field == list && context == 10) {
			doSelectionChangedProcessing();
		} else if (field == okButton) {
			doOKProcessing();

		} else if (field == cancelButton) {
			doCancelProcessing();

		}
	}

	/**
	 * @param type dialog type, one of the TYPE_ consts.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @param startPath What initial path to use when displaying the dialog.
	 */
	public void setStartPath(String startPath) {
		this.startPath = startPath;
	}

	/**
	 * @param defaultSaveAsFileName if type is TYPE_SAVE_AS, then the default file name to use.
	 */
	public void setDefaultSaveAsFileName(String defaultSaveAsFileName) {
		this.defaultSaveAsFileName = defaultSaveAsFileName;
	}

	/**
	 * 
	 * @return the location that was navigated to (if any)
	 */
	public String getLocation() {
		if (currentPath == null)
			return null;
		int pos = currentPath.lastIndexOf('/');
		if (pos == -1)
			return null;
		if (pos == currentPath.length() - 1)
			return currentPath;
		return currentPath.substring(0, pos);

	}

}
/*
 * If in file open mode: -- simple selection. Drill down/up until you find what you want. If in save file mode: -- drill
 * down/up to find what you want -- type in file name
 * 
 * If in folder selection mode: -- see only folders -- drill down until you see what you want -- press button
 */