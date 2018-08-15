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
package ca.farrelltonsolar.j2modlite.facade;


import java.net.InetAddress;
import java.net.UnknownHostException;

import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.io.ModbusTCPTransaction;
import ca.farrelltonsolar.j2modlite.msg.ModbusResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferRequest;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersRequest;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersResponse;
import ca.farrelltonsolar.j2modlite.net.TCPMasterConnection;
import ca.farrelltonsolar.j2modlite.procimg.Register;

/**
 * Modbus/TCP Master facade.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ModbusTCPMaster {

    private TCPMasterConnection m_Connection;
    private InetAddress m_SlaveAddress;
    private ModbusTCPTransaction m_Transaction;
    private ReadMultipleRegistersRequest m_ReadMultipleRegistersRequest;
    private ReadFileTransferRequest m_FileTransferRequest;
    private boolean m_Reconnecting = false;

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr an internet address as resolvable IP name or IP number,
     *             specifying the slave to communicate with.
     */
    public ModbusTCPMaster(String addr) {
        try {
            m_SlaveAddress = InetAddress.getByName(addr);
            m_Connection = new TCPMasterConnection(m_SlaveAddress);
            m_FileTransferRequest = new ReadFileTransferRequest();
            m_ReadMultipleRegistersRequest = new ReadMultipleRegistersRequest();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage());
        }
    }//constructor

    /**
     * Constructs a new master facade instance for communication
     * with a given slave.
     *
     * @param addr an internet address as resolvable IP name or IP number,
     *             specifying the slave to communicate with.
     * @param port the port the slave is listening to.
     */
    public ModbusTCPMaster(String addr, int port) {
        this(addr);
        m_Connection.setPort(port);
    }//constructor

    public boolean isConnected() {
        return m_Connection.isConnected();
    }

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public void connect()
            throws Exception {
        if (m_Connection != null && !m_Connection.isConnected()) {
            m_Connection.connect();
            m_Transaction = new ModbusTCPTransaction(m_Connection);
            m_Transaction.setReconnecting(m_Reconnecting);
        }
    }//connect

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    public void disconnect() {
        if (m_Connection != null && m_Connection.isConnected()) {
            m_Connection.close();
            m_Transaction = null;
        }
    }//disconnect

    /**
     * Sets the flag that specifies whether to maintain a
     * constant connection or reconnect for every transaction.
     *
     * @param b true if a new connection should be established for each
     *          transaction, false otherwise.
     */
    public void setReconnecting(boolean b) {
        m_Reconnecting = b;
        if (m_Transaction != null) {
            m_Transaction.setReconnecting(b);
        }
    }//setReconnecting

    /**
     * Tests if a constant connection is maintained or if a new
     * connection is established for every transaction.
     *
     * @return true if a new connection should be established for each
     * transaction, false otherwise.
     */
    public boolean isReconnecting() {
        return m_Reconnecting;
    }//isReconnecting

    /**
     * Reads a given number of registers from the slave.
     * <p/>
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the register to start reading from.
     * @param count the number of registers to be read.
     * @return a <tt>Register[]</tt> holding the received registers.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized Register[] readMultipleRegisters(int ref, int count)
            throws ModbusException {
        m_ReadMultipleRegistersRequest.setReference(ref);
        m_ReadMultipleRegistersRequest.setWordCount(count);
        m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
        m_Transaction.execute();
        ModbusResponse response = m_Transaction.getResponse();
        if (response != null) {
            return ((ReadMultipleRegistersResponse) response).getRegisters();
        }
        return null;
    }//readMultipleRegisters

    public synchronized ReadFileTransferResponse readFileTransfer(int day, int category, int device)
            throws ModbusException {
        m_FileTransferRequest.setCategory(category);
        m_FileTransferRequest.setDayIndex(day);
        m_FileTransferRequest.setDevice(device);
        m_Transaction.setRequest(m_FileTransferRequest);
        m_Transaction.execute();
        return ((ReadFileTransferResponse) m_Transaction.getResponse());
    }

}//class ModbusTCPMaster
