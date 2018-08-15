/*
 * jFCPlib - FcpMessage.java - Copyright © 2008 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package net.pterodactylus.fcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An FCP message. FCP messages consist of a name, an arbitrary amount of
 * “fields” (i.e. key-value pairs), a message end marker, and optional payload
 * data that follows the marker.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpMessage implements Iterable<String> {

	/** Constant for the linefeed. */
	private static final String LINEFEED = "\r\n";

	/** The name of the message. */
	private final String name;

	/** The fields of the message. */
	private final Map<String, String> fields = new HashMap<String, String>();

	/** The optional payload input stream. */
	private InputStream payloadInputStream;

	/**
	 * Creates a new FCP message with the given name.
	 *
	 * @param name
	 *            The name of the FCP message
	 */
	public FcpMessage(String name) {
		this(name, null);
	}

	/**
	 * Creates a new FCP message with the given name and the given payload
	 * input stream. The payload input stream is not read until the message is
	 * sent to the node using {@link FcpConnection#sendMessage(FcpMessage)}.
	 *
	 * @param name
	 *            The name of the message
	 * @param payloadInputStream
	 *            The payload of the message
	 */
	public FcpMessage(String name, InputStream payloadInputStream) {
		this.name = name;
		this.payloadInputStream = payloadInputStream;
	}

	/**
	 * Returns the name of the message.
	 *
	 * @return The name of the message
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks whether this message has a field with the given name.
	 *
	 * @param field
	 *            The name of the field to check for
	 * @return <code>true</code> if the message has a field with the given
	 *         name, <code>false</code> otherwise
	 */
	public boolean hasField(String field) {
		return fields.containsKey(field);
	}

	/**
	 * Sets the field with the given name to the given value. If the field
	 * already exists in this message it is overwritten.
	 *
	 * @param field
	 *            The name of the field
	 * @param value
	 *            The value of the field
	 */
	public void setField(String field, String value) {
		if ((field == null) || (value == null)) {
			throw new NullPointerException(((field == null) ? "field " : "value ") + "must not be null");
		}
		fields.put(field, value);
	}

	/**
	 * Returns the value of the given field.
	 *
	 * @param field
	 *            The name of the field
	 * @return The value of the field, or <code>null</code> if there is no such
	 *         field
	 */
	public String getField(String field) {
		return fields.get(field);
	}

	/**
	 * Returns all fields of this message.
	 *
	 * @return All fields of this message
	 */
	public Map<String, String> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<String> iterator() {
		return fields.keySet().iterator();
	}

	/**
	 * Sets the payload input stream of the message.
	 *
	 * @param payloadInputStream
	 *            The payload input stream
	 */
	public void setPayloadInputStream(InputStream payloadInputStream) {
		this.payloadInputStream = payloadInputStream;
	}

	/**
	 * Writes this message to the given output stream. If the message has a
	 * payload (i.e. {@link #payloadInputStream} is not <code>null</code>) the
	 * payload is written to the given output stream after the message as well.
	 * That means that this method can only be called once because on the
	 * second invocation the payload input stream could not be read (again).
	 *
	 * @param outputStream
	 *            The output stream to write the message to
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public void write(OutputStream outputStream) throws IOException {
		writeLine(outputStream, name);
		for (Entry<String, String> fieldEntry : fields.entrySet()) {
			writeLine(outputStream, fieldEntry.getKey() + "=" + fieldEntry.getValue());
		}
		writeLine(outputStream, "EndMessage");
		outputStream.flush();
		if (payloadInputStream != null) {
			FcpUtils.copy(payloadInputStream, outputStream);
			outputStream.flush();
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Writes the given line (followed by {@link #LINEFEED} to the given output
	 * stream, using UTF-8 as encoding.
	 *
	 * @param outputStream
	 *            The output stream to write to
	 * @param line
	 *            The line to write
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	private void writeLine(OutputStream outputStream, String line) throws IOException {
		outputStream.write((line + LINEFEED).getBytes("UTF-8"));
	}

}
