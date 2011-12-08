/**
 * Copyright (c) 2010 Marc A. Paradise This file is part of "BBSSH" BBSSH is based upon MidpSSH by Karl von Randow.
 * MidpSSH was based upon Telnet Floyd and FloydSSH by Radek Polak. This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
 * Cambridge, MA 02139, USA.
 */
package org.bbssh.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.Message.RecipientType;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.HMAC;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.Arrays;

import org.bbssh.BBSSHApp;
import org.bbssh.i18n.BBSSHResource;
import org.bbssh.io.LineInputStream;
import org.bbssh.net.ConnectionHelper;
import org.bbssh.net.session.SshSession;
import org.bbssh.session.RemoteSessionInstance;
import org.bbssh.session.SessionManager;
import org.bbssh.ssh.kex.KexAgreement;

/**
 * Utility class containing various helper/tool functions.
 */
public class Tools {
	public static final long NOTIFICATION_GUID = 0x148f158c8bac0113L; // org.bbssh.BBSSH Notifications

	public static Object[] vectorToArray(Vector v, int frontPadding) {
		int limit = (v == null) ? 0 : v.size();
		int size = limit + frontPadding;
		Object ret[] = new Object[size];
		for (int x = 0; x < limit; x++) {
			ret[x + frontPadding] = v.elementAt(x);
		}
		return ret;
	}

	public static Object[] vectorToArray(Vector v) {
		return vectorToArray(v, 0);

	}

	public static String byteCountToHumanReadableString(int bytes) {
		if (bytes < 1024) {
			return bytes + " bytes";
		} else if (bytes < 1024 * 1024) {
			return getIntAsStringWithTwoDecimals(bytes * 100 / 1024) + " KB";
		} else {
			return getIntAsStringWithTwoDecimals(bytes * 100 / (1024 * 1024)) + " MB";
		}
	}

	private static String getIntAsStringWithTwoDecimals(int i) {
		String str = "" + i;
		return str.substring(0, str.length() - 2) + "." + str.substring(str.length() - 2);
	}

	/**
	 * Splits a string using the provided delimeter.
	 * 
	 * @param data
	 * @param splitChar
	 * @return array of strings
	 */
	public static final String[] splitString(final String data, final char splitChar) {
		if (data == null || data.length() == 0)
			return new String[] {};

		Vector v = new Vector();
		// @todo - more efficient to count iterations first?
		// @todo - implement "max" number of elements to return.
		int indexStart = 0;
		int indexEnd = data.indexOf(splitChar);
		while (indexEnd != -1) {
			String s = data.substring(indexStart, indexEnd);
			if (s.length() > 0) {
				v.addElement(s);
			}
			indexStart = indexEnd + 1;
			indexEnd = data.indexOf(splitChar, indexStart);
		}

		if (indexStart != data.length()) {
			String s = data.substring(indexStart);
			if (s.length() > 0) {
				v.addElement(s);
			}
		}

		String[] result = new String[v.size()];
		v.copyInto(result);
		return result;
	}

	static final String HEXES = "0123456789ABCDEF";

	/**
	 * Converts a string representation of a hex value into byte array.
	 * 
	 * @param data
	 * @return hex string as bytes
	 * @throws NumberFormatException
	 *             if string is not valid hex format or is does not contain an even number of digits.
	 */
	public static byte[] getHexStringAsBytes(String data) throws NumberFormatException {

		int len = data.length();
		byte[] output = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			output[i / 2] =
					(byte) ((Character.digit(data.charAt(i), 16) << 4) + Character.digit(data.charAt(i + 1), 16));
		}
		return output;

	}

	public static String getBytesAsUnpaddedHexString(byte[] raw, int count) {
		if (raw == null) {
			return null;
		}
		final StringBuffer hex = new StringBuffer(3 * raw.length);
		for (int x = 0; x < count; x++) {
			byte b = raw[x];
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	public static String getBytesAsHexString(byte[] raw, int count) {
		if (raw == null) {
			return null;
		}
		final StringBuffer hex = new StringBuffer(3 * raw.length);
		for (int x = 0; x < count; x++) {
			byte b = raw[x];
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");
		}
		return hex.toString();
	}

	public static byte[] fillPattern(byte pattern, int length) {
		byte[] val = new byte[length];
		for (int x = 0; x < length; x++) {
			val[x] = (byte) pattern;
		}
		return val;
	}

	/**
	 * A simple helper function primarily used during key negotiations - in many cases the server and client exchange a
	 * list of supported protocols, and the protocl to be used will be the first common protocol listed from clietn and
	 * server. This function simply identifies the first common elements in two string arrays by performing a string
	 * comparison, with first match from "a" found in "b" being the accepted value.
	 * 
	 * @param a
	 *            first non-null string array to compare
	 * @param b
	 *            second non-null string array to compare.
	 * @return the first matching element from "a" that is in "b", or null if none.
	 */
	public static String findFirstMatchingElement(String[] a, String[] b) {
		for (int x = 0; x < a.length; x++) {
			for (int y = 0; y < b.length; y++) {
				if (a[x].equals(b[y])) {
					return a[x];

				}

			}
		}
		return null;

	}

	/**
	 * Compares the first elements of two string arrays and returns true if and only if they match exactly
	 * 
	 * @param a
	 * @param b
	 * @return true if a[0] and b[0] are valid and match.
	 */
	public static boolean doFirstElementsMatch(String[] a, String[] b) {
		if (a == null || b == null) {
			return false;
		}
		if (a.length == 0 && b.length == 0) {
			return true;
		}
		if (a.length == 0 || b.length == 0) {
			return false;
		}
		return a[0].equals(b[0]);

	}

	public static StringBuffer getLocalFileContents(String url) throws IOException {
		FileConnection fc = (FileConnection) Connector.open(url);
		LineInputStream stream = new LineInputStream(fc.openInputStream());
		StringBuffer data = readContentIntoFile(stream);
		fc.close();
		return data;

	}

	/**
	 * This is something of a kludge - attempts to load a file via the first valid connection type.
	 * 
	 * @param url
	 * @return a valid HttpConnection
	 * @throws IOException
	 *             if a connection can't be established.
	 */
	public static HttpConnection getHTTPConnectionForURL(String url) throws IOException {
		int accessCount = 0;
		for (int x = 0; x < ConnectionHelper.CONNECTION_TYPE_COUNT; x++) {
			try {
				return ConnectionHelper.getHttpConnection((byte) x, url);
			} catch (IOException e) {
				Logger.error("Failed to load URL via type " + x + ": " + url + " : " + e.getMessage());
			} catch (ControlledAccessException ex2) {
				Logger.error("Access denied for connection type " + x);
				accessCount++;
			}
		}
		if (accessCount == ConnectionHelper.CONNECTION_TYPE_COUNT) {
			throw new IOException("No permissions to connect via any available networking mode.");
		}

		throw new IOException("Unable to load file via any available connection method.");
	}

	/**
	 * Retrieve the file specified by URL and return its contents in a stringbuffer.
	 * 
	 * @param url
	 *            file to retrieve
	 * @return file contents in stringbuffer.
	 * @throws IOException
	 */
	public static StringBuffer getHTTPFileContents(String url) throws IOException {
		HttpConnection conn = getHTTPConnectionForURL(url);
		if (conn == null) {
			Logger.error("Could not obtain valid connection for URL");
			return new StringBuffer();
		}
		LineInputStream stream = new LineInputStream(conn.openInputStream());
		StringBuffer data = readContentIntoFile(stream);
		conn.close();
		return data;
	}

	/**
	 * Reads the full contents of a LineInputStream into a StringBuffer
	 * 
	 * @param stream
	 * @return contents of stream.
	 * @throws IOException
	 */
	public static StringBuffer readContentIntoFile(LineInputStream stream) throws IOException {

		String line;
		StringBuffer data = new StringBuffer();
		while ((line = stream.readLine(true)) != null) {
			data.append(line);
		}
		return data;

	}

	/**
	 * Sends feedback, optionally including the provided bitmap as an attachement. it is the caller's responsibility to
	 * ensure that this is invoked in a properly synchronized manner.
	 * 
	 * @param screenshot
	 *            - if not null, this function prompts the user to include the screenshot as an attachment.
	 */
	public static void sendFeedback(Bitmap screenshot) {
		BBSSHApp.inst().requestPermission(ApplicationPermissions.PERMISSION_EMAIL,
				BBSSHResource.MSG_PERMISSIONS_MISSING_EMAIL_FEEDBACK);
		ResourceBundle b = ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
		try {
			Multipart mp = new Multipart();
			Message msg = new Message();
			// @todo - prompt whether they want to send bug or feature request request or feedback/comments.
			// if bug/feature, use SUPPORT_EMAIL_ADDRESS and SUPPORT_EMAIL_NAME
			// otherwise use SUPPORT_EMAIL_FEEDBACK_ADDRESS and SUPPORT_EMAIL_FEEDBACK_NAME

			Address a =
					new Address(b.getString(BBSSHResource.SUPPORT_EMAIL_FEEDBACK_ADDRESS),
							b.getString(BBSSHResource.SUPPORT_EMAIL_FEEDBACK_NAME));
			Address[] addresses = {
				a
			};
			if (screenshot == null
					|| Dialog.ask(Dialog.D_YES_NO, b.getString(BBSSHResource.MSG_FEEDBACK_INCLUDE_SCREENSHOT),
							Dialog.YES) == Dialog.NO) {
			} else {
				PNGEncodedImage img = PNGEncodedImage.encode(screenshot);
				SupportedAttachmentPart pt =
						new SupportedAttachmentPart(mp, img.getMIMEType(), "bbssh-screen.png", img.getData());
				mp.addBodyPart(pt);
			}

			if (Logger.isFileLoggingEnabled()) {
				if (Dialog.ask(Dialog.D_YES_NO, b.getString(BBSSHResource.MSG_FEEDBACK_INCLUDE_LOGS), Dialog.YES) == Dialog.YES) {
					byte[] data = Logger.getFileContent();
					if (data == null || data.length == 0) {
						Dialog.inform(b.getString(BBSSHResource.MSG_FEEDBACK_COULD_NOT_READ_LOG));
					} else {
						mp.addBodyPart(new SupportedAttachmentPart(mp, "text/plain", "BBSSH-LOG.TXT", data));
					}
				}
			}
			StringBuffer data = new StringBuffer(2048);
			data.append("\r\n\r\n---------------------\r\n")
					.append("Please allow us to include the following information in order"
							+ " to help troubleshoot any problems you may be having.")
					.append("\r\nNone of this information is personally identifiable.\r\n");

			buildDiagnosticString(data);
			TextBodyPart tb = new TextBodyPart(mp, data.toString());
			mp.addBodyPart(tb);
			msg.setContent(mp);
			msg.addRecipients(RecipientType.TO, addresses);
			Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(msg));
		} catch (Throwable ex) {
			Logger.error("Unable to send feedback: " + ex.getMessage());
		}

	}

	public static void buildDiagnosticString(StringBuffer data) {
		data.append("BBSSH version: ").append(Version.getAppVersion()).append("\r\n").append("Hardware: ")
				.append(DeviceInfo.getDeviceName()).append("\r\n").append("Plat version: ")
				.append(DeviceInfo.getPlatformVersion()).append("\r\n").append("SW version: ")
				.append(DeviceInfo.getSoftwareVersion()).append("\r\n").append("Mfg: ")
				.append(DeviceInfo.getManufacturerName()).append("\r\n").append("Release mode: ")
				.append(Version.isReleaseMode()).append("\r\n").append("Direct: ")
				.append(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT)).append("\r\n")
				.append("WiFi Connected: " + ConnectionHelper.isWifiAvailable()).append("\r\n").append("BIS-B: ")
				.append(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B)).append("\r\n").append("BES: ")
				.append(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS)).append("\r\n")
				.append("Sig Strength: ").append(RadioInfo.getSignalLevel()).append("dB\r\n");
		RemoteSessionInstance i = SessionManager.getInstance().activeSession;
		if (i != null) {
			if (i.state != null) {
				data.append("Cols Visible: ").append(i.state.numColsVisible).append("\r\n").append("Rows: ")
						.append(i.state.numRows).append("\r\n").append("Top Offset: ").append(i.state.topTermRow)
						.append("\r\n").append("Term Left Offset: ").append(i.state.left).append("\r\n")
						.append("Paint Stats: (FR PR LE LP PBS PC) ").append(i.state.debugFullRefreshCount).append(' ')
						.append(i.state.debugPartialRefreshCount).append(' ').append(i.state.debugLineEvalCount)
						.append(' ').append(i.state.debugLinePaintCount).append(' ')
						.append(i.state.debugPaintBackStoreCount).append(' ').append(i.state.debugPaintCount)
						.append(' ').append("Redraw Request Wait Time: ").append(i.state.debugRedrawRequestWaitTime)
						.append(' ').append("Redraw Paint Wait Time: ").append(i.state.debugRedrawStartWaitTime)
						.append(' ');
				if (i.session != null) {
					data.append("WiFi Override: ").append(i.session.isWifiOverrideConnection()).append("\r\n");
				}

				if (i.session instanceof SshSession) {
					SshSession s = (SshSession) i.session;
					data.append("Packet In/Out: ").append(s.inputPacketCount).append('/').append(s.outputPacketCount)
							.append("\r\n").append("Data Received/Accepted bytes: ").append(s.getReceivedDataBytes())
							.append('/').append(s.getAcceptedDataBytes()).append("\r\n");

				}
				if (i.emulator != null) {
					data.append("Max Rows: ").append(i.emulator.getMaxBufferSize()).append("\r\n")
							.append("Curr Rows: ").append(i.emulator.getBufferSize()).append("\r\n")
							.append("Processed data bytes: ").append(i.emulator.handledCharCount).append("\r\n");
				}
			}
		}

	}

	/**
	 * Packs the keystroke
	 * 
	 * @param keyCode
	 * @param status
	 * @return packed value of the keycode and status.
	 */
	public static long packToLong(int keyCode, int status) {
		return ((long) keyCode << 32) | status;
	}

	private static Hashtable bitmaps = new Hashtable();
	private static Hashtable images = new Hashtable();

	public static Bitmap loadBitmap(String fileName) {
		Bitmap image = (Bitmap) bitmaps.get(fileName);
		Logger.debug("Loading resource: " + fileName);
		if (image == null) {
			image = Bitmap.getBitmapResource(fileName);
			if (image == null) {
				image = Bitmap.getBitmapResource("cod://" + fileName);
			}
			if (image == null) {
				image = Bitmap.getBitmapResource("/" + fileName);

			}
			if (image == null) {
				Logger.error("Failed to load image file: " + fileName);
			} else {
				bitmaps.put(fileName, image);
			}
		}
		return image;

	}

	// @todo can we replace Bitmap with this too?
	public static EncodedImage loadEncodedImage(String fileName) {
		EncodedImage image = (EncodedImage) images.get(fileName);
		Logger.debug("Loading resource: " + fileName);
		if (image == null) {
			image = EncodedImage.getEncodedImageResource(fileName);
			if (image == null) {
				image = EncodedImage.getEncodedImageResource("cod://" + fileName);
			}
			if (image == null) {
				image = EncodedImage.getEncodedImageResource("/" + fileName);
			}
			if (image == null) {
				Logger.error("Failed to load encoded image file: " + fileName);
			} else {
				images.put(fileName, image);
			}
		}
		return image;
	}

	public static EncodedImage scaleImage(String name, int width, int height) {
		EncodedImage image = loadEncodedImage(name);
		if (image == null)
			return null;

		int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
		int currentHeightFixed32 = Fixed32.toFP(image.getHeight());

		int requiredWidthFixed32 = Fixed32.toFP(width);
		int requiredHeightFixed32 = Fixed32.toFP(height);

		int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
		int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
		return image.scaleImage32(scaleXFixed32, scaleYFixed32);
	}

	/**
	 * Makes every possible attempt to find a file resource by name and returns it as an InputStream if found. If not fo
	 * und, an exception is thrown. It is the caller's responsibility to close the input stream if it opens the stream.
	 * 
	 * @param fileName
	 *            file name of the resource.
	 * @return InputStream of the requestedresource.
	 * @throws FileNotFoundException
	 *             when resource/file can't be found.
	 */
	public static InputStream getResourceInputStream(String fileName) throws FileNotFoundException {
		Object o = new Object();
		InputStream stream = o.getClass().getResourceAsStream("cod://" + fileName);
		if (stream == null) {
			stream = o.getClass().getResourceAsStream("/" + fileName);
		}
		if (stream == null) {
			stream = o.getClass().getResourceAsStream(fileName);
		}
		if (stream == null)
			throw new FileNotFoundException();
		return stream;
	}

	public static void copyIntToByteArray(int value, byte[] target, int offset) {
		target[offset + 3] = (byte) (value & 0xff);
		target[offset + 2] = (byte) ((value >> 8) & 0xff);
		target[offset + 1] = (byte) ((value >> 16) & 0xff);
		target[offset + 0] = (byte) ((value >> 24) & 0xff);
	}

	public static int byteArrayToInt(byte[] source, int offset) {
		return (source[offset + 3] | (source[offset + 2] << 8) | (source[offset + 1] << 16) | (source[offset + 0] << 24));

	}

	private static final byte[] converter = new byte[4];

	/**
	 * This works around an apparent flaw in HMAC.updateInt wherein it's not correctly calculating the hash when an int
	 * is used.
	 * 
	 * @param val
	 * @param mac
	 */
	public static void updateMACForInt(int val, HMAC mac) throws CryptoTokenException {
		converter[0] = (byte) (val >>> 24);
		converter[1] = (byte) (val >>> 16);
		converter[2] = (byte) (val >>> 8);
		converter[3] = (byte) val;
		mac.update(converter);
	}

	public static byte[] insertByteValue(byte value, byte[] in) {
		byte[] out = new byte[in.length + 1];
		out[0] = value;
		System.arraycopy(in, 0, out, 1, in.length);
		return out;
	}

	/**
	 * @param input
	 * @param length
	 * @return byte array no longer than the specified length, truncating as required. .
	 */
	public static byte[] trimBytesToLength(byte[] input, int length) {
		byte[] out = input;
		if (input.length > length) {
			out = new byte[length];
			System.arraycopy(input, 0, out, 0, length);
		}
		return out;
	}

	/**
	 * Safely swaps the two provided indices. If either index is invalid for any reason no action is taken.
	 * 
	 * @param v
	 * @param idx1
	 * @param idx2
	 */
	public static void swapVectorElements(Vector v, int idx1, int idx2) {
		if (idx1 == idx2 || idx1 < 0 || idx2 < 0 || idx1 >= v.size() || idx2 >= v.size()) {
			return;
		}
		Object o = v.elementAt(idx1);
		v.setElementAt(v.elementAt(idx2), idx1);
		v.setElementAt(o, idx2);

	}

	public static String[] buildConnectionDataString(KexAgreement a) {
		if (a == null)
			return null;

		return new String[] {
				"Crypto S -> C: " + a.serverToClientCryptoAlgorithm,
				"Crypto C -> S: " + a.clientToServerCryptoAlgorithm,
				"Compression S -> C: " + a.compressionClientToServer,
				"Compression C -> S: " + a.compressionServerToClient, "KEX Algorithm: " + a.kexAlgorithm,
				"Language S -> C: " + a.languageClientToServer, "Language C -> S: " + a.languageServerToClient,
				"MAC S -> C: " + a.MACClientToServer, "MAC C -> S: " + a.MACServerToClient,
				"Server Key Algorithm: " + a.serverHostKeyAlgorithm
		};
	}

	public static ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(BBSSHResource.BUNDLE_ID, BBSSHResource.BUNDLE_NAME);
	}

	public static String getStringResource(int id) {
		return getResourceBundle().getString(id);
	}

	public static void sortVector(Vector v) {
		Object[] o = new Object[v.size()];
		v.copyInto(o);
		// ..getInstance(false)
		Arrays.sort(o, ObjectStringComparator.CASE_SENSITIVE_COMPARATOR);
		for (int x = v.size() - 1; x >= 0; x--) {
			v.setElementAt(o[x], x);
		}
	}

	public static Font deriveBBSSHDialogFont(Font f) {
		return f.derive(Font.PLAIN, (f.getHeight() / 4) * 3);

	}

	public static byte[] removeBytePadding(byte[] b) {
		// Remove padding
		// @todo ... shouldn't this remove ALL leading?
		if (b[0] == 0) {
			// trim leading zero.
			byte[] temp = new byte[b.length - 1];
			System.arraycopy(b, 1, temp, 0, temp.length);
			return temp;
		}
		return b;

	}

	private static DataOutputStream openNewOutputImpl(String name) {
		try {
			FileConnection fconn = (FileConnection) Connector.open(name);
			if (fconn.exists()) {
				fconn.delete();
			}
			fconn.create();
			DataOutputStream s = fconn.openDataOutputStream();
			fconn.close();

			return s;
		} catch (Exception e) {
			Logger.error("Failed openNewOutput " + name + " " + e.getMessage());
		}
		return null;
	}

	/**
	 * destructively opens the requested file if possible. creates it if it doesn't exist; otherwise deletes it and
	 * ercreates it. If the file can't be created, this returns null. File will be attempted first on the user's SDcard;
	 * if that's not available it will be created on device storage.
	 * 
	 * @param name
	 *            unqualified name of the file.
	 * @return
	 */
	public static DataOutputStream openNewOutput(String name) {
		DataOutputStream r = openNewOutputImpl("file:///SDCard/" + name);
		if (r == null)
			r = openNewOutputImpl("file:///store/home/user/" + name);
		return r;
	}

	/**
	 * returns the standard color index for the specified color. reference:
	 * http://en.wikipedia.org/wiki/ANSI_escape_code#Colors - color table
	 * 
	 * @param color
	 * @param default
	 * @return color index 0-7; if no match, returns defValue.
	 */
	public static int convertColorToANSITable(int color, int defValue) {
		//
		// (color names are standard, appearance is impl specific.
		switch (color) {
			case 0: // black, dim black
			case 0x333333: // bold black
				return 0;
			case 0xcc0000: // red
			case 0x990000: // dim
			case 0xff0000: // bold red
				return 1;
			case 0x00cc00: // green
			case 0x009900: // dim green
			case 0x00FF00: // bold green
				return 2;
			case 0xcccc00: // yellow
			case 0x999900: // dim
			case 0xFFFF00: // bold
				return 3;
			case 0x0000cc: // blue
			case 0x000099: // dim
			case 0x0000FF: // bold
				return 4;
			case 0xcc00cc: // magenta
			case 0x990099: // dim
			case 0xFF00FF: // bold
				return 5;
			case 0x00cccc: // cyan
			case 0x009999: // dim
			case 0x00FFFF: // bold
				return 6;
			case 0xcccccc: // white
			case 0x999999: // dim
			case 0xFFFFFF: // bold
				return 7;
			default:
				return defValue;
		}

	}

	// A list of colors used for representation of the display */
	public static final int color[] = {
			0x000000,// black
			0xcc0000, // red
			0x00cc00, // green
			0xcccc00, // yellow
			0x0000cc, // blue
			0xcc00cc, // magenta
			0x00cccc, // cyan
			0xcccccc
	// white
			};
	public static final int boldcolor[] = {
			0x333333, // black
			0xff0000, // red
			0x00ff00, // green
			0xffff00, // yellow
			0x0000ff, // blue
			0xff00ff, // magenta
			0x00ffff, // cyan
			0xffffff
	// white
			};
	public static final int lowcolor[] = {
			0x000000,// black
			0x990000, // red
			0x009900, // green
			0x999900, // yellow
			0x000099, // blue
			0x990099, // magenta
			0x009999, // cyan
			0x999999
	// white
			};
	public static final String CRLF = "\r\n";
}
