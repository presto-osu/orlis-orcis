package ca.farrelltonsolar.j2modlite.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ca.farrelltonsolar.j2modlite.Modbus;

/**
 * Created by Graham on 15/02/14.
 */
public class ReadFileTransferRequest extends ModbusRequest {

    // instance attributes
    private int m_Category;
    private int m_Day;
    private int m_Device;

    /**
     * Constructs a new <tt>ReadMultipleRegistersRequest</tt> instance.
     */
    public ReadFileTransferRequest() {
        super();

        setFunctionCode(Modbus.READ_LOG_FILE);
        setDataLength(10);
    }


    public ModbusResponse getResponse() {
        ReadFileTransferResponse response = null;

        response = new ReadFileTransferResponse();

        response.setUnitID(getUnitID());
        response.setHeadless(isHeadless());
        if (!isHeadless()) {
            response.setProtocolID(getProtocolID());
            response.setTransactionID(getTransactionID());
        }
        return response;
    }

    public ModbusResponse createResponse() {
        ReadFileTransferResponse response = null;
        return response;
    }


    public void setDayIndex(int day) {
        m_Day = day;
    }

    public void setCategory(int val) {
        m_Category = val;
    }

    public void setDevice(int val) {
        m_Device = val;
    }

    /**
     * Returns the number of words to be read with this
     * <tt>ReadMultipleRegistersRequest</tt>.
     * <p/>
     *
     * @return the number of words to be read as <tt>int</tt>.
     */
    public long Address() {
        return ((m_Category & 0x003F) << 10) + (m_Day & 0x03FF);
    }

    public void writeData(DataOutput dout) throws IOException {
        dout.write(getMessage());
    }

    public void readData(DataInput din) throws IOException {
//            m_Reference = din.readUnsignedShort();
//            m_WordCount = din.readUnsignedShort();
    }

    public byte[] getMessage() {
        byte result[] = new byte[8];
        long address = Address();
        result[0] = (byte) (m_Device & 0xff);
        result[1] = (byte) 64; // data_len
        result[2] = (byte) 0xFF; // reserved
        result[3] = (byte) 0xFF; // reserved
        result[4] = (byte) ((address >> 24) & 0xff);
        result[5] = (byte) ((address >> 16) & 0xff);
        result[6] = (byte) ((address >> 8) & 0xff);
        result[7] = (byte) (address & 0xff);
        return result;
    }
}
