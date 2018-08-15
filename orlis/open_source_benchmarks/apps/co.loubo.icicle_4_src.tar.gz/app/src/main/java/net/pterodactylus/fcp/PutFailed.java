/*
 * jFCPlib - GetFailed.java - Copyright © 2008 David Roden
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The “PutFailed” message signals the client that a {@link ClientPut} request
 * has failed. This also means that no further progress messages for that
 * request will be sent.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PutFailed extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PutFailed” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	PutFailed(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the code of the error.
	 *
	 * @return The code of the error, or <code>-1</code> if the error code
	 *         could not be parsed
	 */
	public int getCode() {
		return FcpUtils.safeParseInt(getField("Code"));
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The identifier of the request
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns whether the request is on the global queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> if it is on the client-local queue
	 */
	public boolean isGlobal() {
		return Boolean.valueOf(getField("Global"));
	}

	/**
	 * Returns the description of the error code.
	 *
	 * @return The description of the error code
	 */
	public String getCodeDescription() {
		return getField("CodeDescription");
	}

	/**
	 * Returns the extra description of the error.
	 *
	 * @return The extra description of the error
	 */
	public String getExtraDescription() {
		return getField("ExtraDescription");
	}

	/**
	 * Returns the short description of the error.
	 *
	 * @return The short description of the error
	 */
	public String getShortCodeDescription() {
		return getField("ShortCodeDescription");
	}

	/**
	 * Returns the expected URI of the request.
	 *
	 * @return The expected URI
	 */
	public String getExpectedURI() {
		return getField("ExpectedURI");
	}

	/**
	 * Returns whether the request failed fatally. If a request fails fatally
	 * it can never complete, even with inifinite retries.
	 *
	 * @return <code>true</code> if the request failed fatally,
	 *         <code>false</code> otherwise
	 */
	public boolean isFatal() {
		return Boolean.valueOf(getField("Fatal"));
	}

	/**
	 * Returns a list of complex error codes with the message. Use
	 * {@link #getComplexErrorDescription(int)} and
	 * {@link #getComplexErrorCount(int)} to get details.
	 *
	 * @return A list of complex error codes
	 */
	public int[] getComplexErrorCodes() {
		Map<String, String> allFields = getFields();
		List<Integer> errorCodeList = new ArrayList<Integer>();
		for (Entry<String, String> field : allFields.entrySet()) {
			String fieldKey = field.getKey();
			if (fieldKey.startsWith("Errors.")) {
				int nextDot = fieldKey.indexOf('.', 7);
				if (nextDot > -1) {
					int errorCode = FcpUtils.safeParseInt(fieldKey.substring(7, nextDot));
					if (errorCode != -1) {
						errorCodeList.add(errorCode);
					}
				}
			}
		}
		int[] errorCodes = new int[errorCodeList.size()];
		int errorIndex = 0;
		for (int errorCode : errorCodeList) {
			errorCodes[errorIndex++] = errorCode;
		}
		return errorCodes;
	}

	/**
	 * Returns the description of the complex error. You should only hand it
	 * error codes you got from {@link #getComplexErrorCodes()}!
	 *
	 * @param errorCode
	 *            The error code
	 * @return The description of the complex error
	 */
	public String getComplexErrorDescription(int errorCode) {
		return getField("Errors." + errorCode + ".Description");
	}

	/**
	 * Returns the count of the complex error. You should only hand it error
	 * codes you got from {@link #getComplexErrorCodes()}!
	 *
	 * @param errorCode
	 *            The error code
	 * @return The count of the complex error, or <code>-1</code> if the count
	 *         could not be parsed
	 */
	public int getComplexErrorCount(int errorCode) {
		return FcpUtils.safeParseInt(getField("Errors." + errorCode + ".Count"));
	}

}
