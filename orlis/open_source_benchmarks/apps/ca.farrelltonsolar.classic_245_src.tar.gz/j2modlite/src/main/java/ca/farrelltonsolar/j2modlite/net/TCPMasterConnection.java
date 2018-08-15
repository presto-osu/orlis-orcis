//License
/***
 * Java Modbus Library (jamod)
 * Copyright (c) 2002-2004, jamod development team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package ca.farrelltonsolar.j2modlite.net;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import ca.farrelltonsolar.j2modlite.Modbus;
import ca.farrelltonsolar.j2modlite.io.ModbusTCPTransport;
import ca.farrelltonsolar.j2modlite.io.ModbusTransport;

/**
 * Class that implements a TCPMasterConnection.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class TCPMasterConnection {

	// instance attributes
	private Socket m_Socket;
	private int m_Timeout = Modbus.DEFAULT_TIMEOUT;
	private boolean m_Connected;

	private InetAddress m_Address;
	private int m_Port = Modbus.DEFAULT_PORT;

	// private int m_Retries = Modbus.DEFAULT_RETRIES;
	private ModbusTCPTransport m_ModbusTransport;
	
	/**
	 * m_useUrgentData - sent a byte of urgent data when testing the TCP
	 * connection.
	 */
	private boolean m_useUrgentData = false;

	/**
	 * Prepares the associated <tt>ModbusTransport</tt> of this
	 * <tt>TCPMasterConnection</tt> for use.
	 * 
	 * @throws IOException
	 *             if an I/O related error occurs.
	 */
	private void prepareTransport() throws IOException {
		if (m_ModbusTransport == null) {
			m_ModbusTransport = new ModbusTCPTransport(m_Socket);
		} else {
			m_ModbusTransport.setSocket(m_Socket);
		}
	}// prepareIO

	/**
	 * Opens this <tt>TCPMasterConnection</tt>.
	 * 
	 * @throws Exception
	 *             if there is a network failure.
	 */
	public synchronized void connect() throws Exception {
		if (! isConnected()) {
			if (Modbus.debug)
                Log.d(getClass().getName(), "connect()");
			
			m_Socket = new Socket(m_Address, m_Port);
			m_Socket.setReuseAddress(true);
			m_Socket.setSoLinger(true, 1);
			m_Socket.setKeepAlive(true);
			
			setTimeout(m_Timeout);
			prepareTransport();
			
			m_Connected = true;
		}
	}// connect

	/**
	 * Tests if this <tt>TCPMasterConnection</tt> is connected.
	 * 
	 * @return <tt>true</tt> if connected, <tt>false</tt> otherwise.
	 */
	public synchronized boolean isConnected() {
		if (m_Connected && m_Socket != null) {
			if (!m_Socket.isConnected() || m_Socket.isClosed()
					|| m_Socket.isInputShutdown()
					|| m_Socket.isOutputShutdown()) {
				try {
					m_Socket.close();
				} catch (IOException e) {
					// Blah.
				}
				m_Connected = false;
			}
		}
		return m_Connected;
	}// isConnected

	/**
	 * Closes this <tt>TCPMasterConnection</tt>.
	 */
	public void close() {
		if (m_Connected) {
			try {
				m_ModbusTransport.close();
			} catch (IOException ex) {
				if (Modbus.debug)
                    Log.d(getClass().getName(), "close()");
			}
			m_Connected = false;
		}
	}// close

	/**
	 * Returns the <tt>ModbusTransport</tt> associated with this
	 * <tt>TCPMasterConnection</tt>.
	 * 
	 * @return the connection's <tt>ModbusTransport</tt>.
	 */
	public ModbusTransport getModbusTransport() {
		return m_ModbusTransport;
	}// getModbusTransport

	/**
	 * Set the <tt>ModbusTransport</tt> associated with this
	 * <tt>TCPMasterConnection</tt>
	 */
	public void setModbusTransport(ModbusTCPTransport trans) {
		m_ModbusTransport = trans;
	}

	/**
	 * Returns the timeout for this <tt>TCPMasterConnection</tt>.
	 * 
	 * @return the timeout as <tt>int</tt>.
	 */
	public int getTimeout() {
		return m_Timeout;
	}// getTimeout

	/**
	 * Sets the timeout for this <tt>TCPMasterConnection</tt>.
	 * 
	 * @param timeout
	 *            the timeout as <tt>int</tt>.
	 */
	public void setTimeout(int timeout) {
		m_Timeout = timeout;
		try {
			m_Socket.setSoTimeout(m_Timeout);
		} catch (IOException ex) {
			// handle?
		}
	}// setTimeout

	/**
	 * Returns the destination port of this <tt>TCPMasterConnection</tt>.
	 * 
	 * @return the port number as <tt>int</tt>.
	 */
	public int getPort() {
		return m_Port;
	}// getPort

	/**
	 * Sets the destination port of this <tt>TCPMasterConnection</tt>. The
	 * default is defined as <tt>Modbus.DEFAULT_PORT</tt>.
	 * 
	 * @param port
	 *            the port number as <tt>int</tt>.
	 */
	public void setPort(int port) {
		m_Port = port;
	}// setPort

	/**
	 * Returns the destination <tt>InetAddress</tt> of this
	 * <tt>TCPMasterConnection</tt>.
	 * 
	 * @return the destination address as <tt>InetAddress</tt>.
	 */
	public InetAddress getAddress() {
		return m_Address;
	}// getAddress

	/**
	 * Sets the destination <tt>InetAddress</tt> of this
	 * <tt>TCPMasterConnection</tt>.
	 * 
	 * @param adr
	 *            the destination address as <tt>InetAddress</tt>.
	 */
	public void setAddress(InetAddress adr) {
		m_Address = adr;
	}// setAddress
	
	public boolean getUseUrgentData() {
		return m_useUrgentData;
	}
	
	public void setUseUrgentData(boolean b) {
		m_useUrgentData = b;
	}

	/**
	 * Constructs a <tt>TCPMasterConnection</tt> instance with a given
	 * destination address.
	 * 
	 * @param adr
	 *            the destination <tt>InetAddress</tt>.
	 */
	public TCPMasterConnection(InetAddress adr) {
		m_Address = adr;
	}// constructor

}// class TCPMasterConnection
