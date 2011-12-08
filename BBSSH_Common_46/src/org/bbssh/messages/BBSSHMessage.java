package org.bbssh.messages;

import java.util.Date;

import net.rim.blackberry.api.messagelist.ApplicationMessage;

public class BBSSHMessage implements ApplicationMessage {

	private String _sender;
	private String _subject;
	private String _message;
	private long _receivedTime;
	private boolean _deleted;

	BBSSHMessage(String sender, String subject, String message) {
		_sender = sender;
		_subject = subject;
		_message = message;
		_receivedTime = new Date().getTime();
	}

	void reply(String message) {
		return;
	}

	void messageDeleted() {
		_deleted = true;
	}

	boolean isDeleted() {
		return _deleted;
	}

	void setSender(String sender) {
		_sender = sender;
	}

	void setSubject(String subject) {
		_subject = subject;
	}

	void setReceivedTime(long receivedTime) {
		_receivedTime = receivedTime;
	}

	void setMessage(String message) {
		_message = message;
	}

	String getMessage() {
		return _message;
	}

	// Implementation of ApplicationMessage ------------------------------------
	/**
	 * @return Contact
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getContact()
	 */
	public String getContact() {
		return _sender;
	}

	/**
	 * @return Message status
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getStatus()
	 */
	public int getStatus() {
		return 0;
		// // Form message list status based on current message state.
		// if (_isNew) {
		// return MessageListDemo.STATUS_NEW;
		// }
		// if (_deleted) {
		// return MessageListDemo.STATUS_DELETED;
		// }
		// if (_replyMessage != null) {
		// return MessageListDemo.STATUS_REPLIED;
		// }
		// return MessageListDemo.STATUS_OPENED;
	}

	/**
	 * @return Subject
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getSubject()
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * @return Non-zero timestamp
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getTimestamp()
	 */
	public long getTimestamp() {
		return _receivedTime;
	}

	/**
	 * @return Message type
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getType()
	 */
	public int getType() {
		// All messages have the same type.
		return 0;
	}

	/**
	 * @return Preview text if defined, null otherwise.
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getPreviewText()
	 */
	public String getPreviewText() {
		if (_message == null) {
			return null;
		}
		if (_message.length() > 100)
			return _message.substring(0, 100) + "...";
		return _message;
	}

	/**
	 * @return Cookie value if provided by the message, null otherwise.
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getCookie(int cookieId)
	 */
	public Object getCookie(int cookieId) {
		return null;
	}

	/**
	 * @return Preview picture if provided by the message, null otherwise.
	 * 
	 * @see net.rim.blackberry.api.messagelist.ApplicationMessage#getPreviewPicture()
	 */
	public Object getPreviewPicture() {
		return null;
	}
}