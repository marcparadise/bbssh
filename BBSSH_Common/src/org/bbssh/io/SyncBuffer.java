package org.bbssh.io;

import java.io.EOFException;

import org.bbssh.util.Logger;

import net.rim.device.api.util.DataBuffer;

public class SyncBuffer {
	DataBuffer data;
	byte fieldId = 0;
	int expectedCount = 0; 
	public static final byte ARR_VALUE = 0;
	public static final byte ARR_EMPTY = 1;
	public static final byte ARR_NULL = 2;

	public SyncBuffer(DataBuffer data, int expectedCount) {
		this(data, (byte) 0, expectedCount);
	}
	public SyncBuffer(DataBuffer data) {
		this(data, (byte) 0, 0);
	}

	public SyncBuffer(DataBuffer data, byte fieldId, int expectedCount) {
		this.data = data;
		this.fieldId = fieldId;
		this.expectedCount = expectedCount;

	}

	public void writeField(short value) {
		data.writeShort(2); // int length
		data.writeByte(fieldId++);
		data.writeShort(value);
	}

	public void writeField(int value) {
		data.writeShort(4); // int length
		data.writeByte(fieldId++);
		data.writeInt(value);
	}

	public void writeField(boolean value) {
		data.writeShort(1); // byte length
		data.writeByte(fieldId++);
		data.writeBoolean(value);

	}

	public void writeField(byte value) {
		data.writeShort(1); // byte length
		data.writeByte(fieldId++);
		data.writeByte(value);
	}

	public void writeField(long value) {
		data.writeShort(8);
		data.writeByte(fieldId++);
		data.writeLong(value);
	}

	public void writeField(byte[] value) {
		short len = 1;
		byte lengthInd;
		if (value == null) {
			lengthInd = ARR_NULL;
		} else if (value.length == 0) {
			lengthInd = ARR_EMPTY;
		} else {
			len += value.length;
			lengthInd = ARR_VALUE;
		}
		data.writeShort(len);
		data.writeByte(fieldId++);
		data.writeByte(lengthInd);
		if (lengthInd == ARR_VALUE) {
			data.write(value);
		}
	}

	public void writeField(String value) {
		if (value == null) {
			writeField((byte[]) null);
		} else {
			writeField(value.getBytes());
		}
	}

	/**
	 * Returns field length and positions buffer at start of field id. .
	 * 
	 * @return field length
	 * @throws EOFException
	 */
	private short getFieldLength() throws EOFException {
		return data.readShort(); // length
	}

	/**
	 * Reads and returns field ID from the stream. If field id does not match expected (based on sequential
	 * incrementing) then an error is logged.
	 * 
	 * Positions buffer at start of field data.
	 * 
	 * @return field id
	 * @throws EOFException
	 */
	private byte getFieldId() throws EOFException {
		byte id = data.readByte(); // id
		if (id != fieldId) {
			// @todo - we'll need to throw a meaingful exception here...
			Logger.info("Field id mismatch: expected ID " + fieldId + " + but received " + id);
		}
		fieldId++;
		return id;
	}

	public short readNextShortField() throws EOFException {
		getFieldLength(); // don't care
		getFieldId(); // don't care
		return data.readShort();
	}

	public int readNextIntField() throws EOFException {
		getFieldLength(); // don't care
		getFieldId(); // don't care
		return data.readInt();
	}

	public long readNextLongField() throws EOFException {
		getFieldLength(); // don't care
		getFieldId(); // don't care
		return data.readLong();
	}

	/**
	 * Reads next string field from the buffer.
	 * 
	 * @return Will return null if original string was null; otherwise a valid string of length 0 or more.
	 * @throws EOFException
	 */
	public String readNextStringField() throws EOFException {
		byte[] value = readNextByteArrayField();
		if (value == null) {
			return null;
		}
		return new String(value);
	}

	/**
	 * Reads next byte array field from the buffer.
	 * 
	 * @return Will return null if original array was null; otherwise a valid byte array containing 0 or more bytes.
	 * @throws EOFException
	 */
	public byte[] readNextByteArrayField() throws EOFException {
		short length = getFieldLength();
		getFieldId(); // don't care
		byte valueInd = data.readByte();
		byte[] value;
		switch (valueInd) {
			case ARR_EMPTY:
				value = new byte[] {};
				break;

			case ARR_VALUE:
				if (length > 0) {
					value = new byte[length - 1];
					data.readFully(value);
				} else {
					value = new byte[] {};
				}
				break;

			case ARR_NULL:
			default:
				value = null;
				break;

		}
		return value;
	}

	public boolean readNextBooleanField() throws EOFException {
		getFieldLength(); // don't care
		getFieldId(); // don't care
		return data.readBoolean();
	}

	public byte readNextByteField() throws EOFException {
		getFieldLength(); // don't care
		getFieldId(); // don't care
		return data.readByte();
	}

	public int getFieldCount() {
		
		if (fieldId > 0) 
			return fieldId; 
		return expectedCount; 
	}
}
