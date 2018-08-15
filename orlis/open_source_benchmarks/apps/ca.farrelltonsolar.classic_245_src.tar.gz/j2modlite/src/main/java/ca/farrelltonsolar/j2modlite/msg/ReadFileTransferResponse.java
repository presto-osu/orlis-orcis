package ca.farrelltonsolar.j2modlite.msg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ca.farrelltonsolar.j2modlite.Modbus;
import ca.farrelltonsolar.j2modlite.procimg.Register;
import ca.farrelltonsolar.j2modlite.procimg.SimpleRegister;

/**
 * Created by Graham on 15/02/14.
 */
public class ReadFileTransferResponse extends ModbusResponse {

    // instance attributes
    private int m_Category;
    private int m_DayIndex;
    private int m_Device;

    private int m_ByteCount;
    private Register[] m_Registers;

    /**
     * Constructs a new <tt>ReadMultipleRegistersResponse</tt> instance.
     */
    public ReadFileTransferResponse() {
        super();
        setFunctionCode(Modbus.READ_LOG_FILE);
    }// constructor


    /**
     * Returns the number of bytes that have been read.
     *
     * @return the number of bytes that have been read as <tt>int</tt>.
     */
    public int getByteCount() {
        return m_ByteCount;
    }
    public int getCategory() {
        return m_Category;
    }
    public int getDevice() {
        return m_Device;
    }
    public int getDayIndex() {
        return m_DayIndex;
    }


    /**
     * Returns the number of words that have been read. The returned value
     * should be half of the the byte count of this
     * <tt>ReadMultipleRegistersResponse</tt>.
     *
     * @return the number of words that have been read as <tt>int</tt>.
     */
    public int getWordCount() {
        return m_ByteCount / 2;
    }// getWordCount

    public Register getRegister(int index) {
        if (m_Registers == null)
            throw new IndexOutOfBoundsException("No registers defined!");

        if (index < 0)
            throw new IndexOutOfBoundsException("Negative index: " + index);

        if (index >= getWordCount())
            throw new IndexOutOfBoundsException(index + " > " + getWordCount());

        return m_Registers[index];
    }

    public int getRegisterValue(int index) throws IndexOutOfBoundsException {
        return getRegister(index).toUnsignedShort();
    }

    /**
     * Returns the reference to the array of registers read.
     *
     * @return a <tt>Register[]</tt> instance.
     */
    public Register[] getRegisters() {
        return m_Registers;
    }

    /**
     * Sets the entire block of registers for this response
     */
    public void setRegisters(Register[] registers) {
        m_ByteCount = registers.length * 2;
        setDataLength(m_ByteCount + 1);

        m_Registers = registers;
    }

    public void writeData(DataOutput dout) throws IOException {
       // dout.writeByte(m_ByteCount);


    }

    public void readData(DataInput din) throws IOException {
        m_Device = din.readUnsignedByte();
        m_ByteCount = din.readUnsignedByte();
        din.skipBytes(4);
        long address = din.readUnsignedShort();
        m_DayIndex = (int)(address & 0x03FF);
        m_Category = (int)((address >> 10) & 0x003F);
        int count = getWordCount();
        m_Registers = new Register[count];
        for (int k = 0; k < count; k++)
            m_Registers[k] = new SimpleRegister(din.readByte(), din.readByte());
        setDataLength(m_ByteCount + 8);
    }

    public byte[] getMessage() {
        byte[] result = new byte[1];

        return result;
    }

}

