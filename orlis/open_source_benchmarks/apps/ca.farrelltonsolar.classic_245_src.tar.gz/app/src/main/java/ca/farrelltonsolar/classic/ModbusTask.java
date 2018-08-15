/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.net.UnknownHostException;
import java.util.TimerTask;

import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.ModbusIOException;
import ca.farrelltonsolar.j2modlite.facade.ModbusTCPMaster;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferResponse;
import ca.farrelltonsolar.j2modlite.procimg.Register;

// Classic modbus table
//            new Register { Address = 4115, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4116, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4117, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4118, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4119, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = socketAddress => U16(socketAddress)},
//            new Register { Address = 4120, Label = "Battery charge state", UnitOfMeasure = "", Conversion = socketAddress => ChargeState(socketAddress)},
//            new Register { Address = 4121, Label = "Average PV inout current", UnitOfMeasure = "Amps", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4122, Label = "PV VOC", UnitOfMeasure = "Volts", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4125, Label = "Daily amp hours", UnitOfMeasure = "Amp hours", Conversion = socketAddress => U16_OneDec(socketAddress)},
//            new Register { Address = 4126, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = socketAddress => U32_OneDec(socketAddress)},
//            new Register { Address = 4128, Label = "Total Amp hours", UnitOfMeasure = "Amp hours", Conversion = socketAddress => U32_OneDec(socketAddress)},
//            new Register { Address = 4130, Label = "Info flag", UnitOfMeasure = "", Conversion = socketAddress => Info(socketAddress)}
//            new Register { Address = 4132, Label = "BATTemperature", UnitOfMeasure = "", Conversion = socketAddress => U16_OneDec(socketAddress)}
//            new Register { Address = 4133, Label = "FETTemperature", UnitOfMeasure = "", Conversion = socketAddress => U16_OneDec(socketAddress)}
//            new Register { Address = 4134, Label = "PCBTemperature", UnitOfMeasure = "", Conversion = socketAddress => U16_OneDec(socketAddress)}

//            new Tupple { Description = "(Off) No power, waiting for power source, battery voltage over set point.", Value = 0 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 3 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 4 },
//            new Tupple { Description = "(Float) Battery is full and regulating battery voltage at float set point.", Value = 5 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 6 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 7 },
//            new Tupple { Description = "(Error) Input voltage is above maximum classic operating voltage.", Value = 10 },
//            new Tupple { Description = "(Equalizing) Max power point tracking. Seeking equalize set point voltage.", Value = 18 }


// TriStar modbus table
//            new Register { Address = 1, Label = "V Scale", UnitOfMeasure = "", Conversion = socketAddress => U32(socketAddress)},
//            new Register { Address = 3, Label = "A Scale", UnitOfMeasure = "", Conversion = socketAddress => U32(socketAddress)},
//            new Register { Address = 25, Label = "Average battery voltage", UnitOfMeasure = "Volts", Conversion = socketAddress => VScale(socketAddress)},
//            new Register { Address = 28, Label = "PV input voltage", UnitOfMeasure = "Volts", Conversion = socketAddress => VScale(socketAddress)},
//            new Register { Address = 29, Label = "Average battery current", UnitOfMeasure = "Amps", Conversion = socketAddress => IScale(socketAddress)},
//            new Register { Address = 30, Label = "Average PV current", UnitOfMeasure = "Amps", Conversion = socketAddress => IScale(socketAddress)},
//            new Register { Address = 45, Label = "Info flag", UnitOfMeasure = "", Conversion = socketAddress => Info(socketAddress)},
//            new Register { Address = 51, Label = "Battery charge state", UnitOfMeasure = "", Conversion = socketAddress => ChargeState(socketAddress)},
//            new Register { Address = 58, Label = "Total kWhours", UnitOfMeasure = "kWh", Conversion = socketAddress => U16(socketAddress)},
//            new Register { Address = 59, Label = "Average power to the battery", UnitOfMeasure = "Watts", Conversion = socketAddress => PScale(socketAddress)},
//            new Register { Address = 69, Label = "Average energy to the battery", UnitOfMeasure = "kWh", Conversion = socketAddress => WHr(socketAddress)}

//            new Tupple { Description = "(Start) System startup.", Value = 0 },
//            new Tupple { Description = "(Night check) No power, detecting nightfall.", Value = 1 },
//            new Tupple { Description = "(Disconnected) No power.", Value = 2 },
//            new Tupple { Description = "(Night) No power, waiting for power source.", Value = 3 },
//            new Tupple { Description = "(Fault) Detected fault.", Value = 4 },
//            new Tupple { Description = "(Bulk) Max power point tracking until absorb voltage reached.", Value = 5 },
//            new Tupple { Description = "(Absorb) Regulating battery voltage at absorb set point until the batteries are charged.", Value = 6 },
//            new Tupple { Description = "(Float) Max power point tracking. Seeking float set point voltage.", Value = 7 },
//            new Tupple { Description = "(Equalize) Regulating battery voltage at equalize set point.", Value = 8 },
//            new Tupple { Description = "(Slave) State set by master charge controller.", Value = 9 }

/**
 * Created by Graham on 12/12/2014.
 */
public class ModbusTask extends TimerTask {

    final Object lock = new Object();
    private Context context;
    private ModbusTCPMaster modbusMaster;
    private ChargeControllerInfo chargeControllerInfo;
    private Readings readings;
    private LogEntry dayLogEntry;
    private LogEntry minuteLogEntry;
    private float v_pu;
    private float i_pu;
    private boolean foundWhizBangJr = false;
    private boolean foundTriStar = false;
    private boolean initialReadingLoaded = false;

    public ModbusTask(ChargeControllerInfo cc, Context ctx) {
        chargeControllerInfo = cc;
        init(ctx);
    }

    private void init(Context ctx) {
        context = ctx;
        readings = new Readings();
        dayLogEntry = new LogEntry();
        minuteLogEntry = new LogEntry();
        Log.d(getClass().getName(), String.format("ModbusTask created thread is %s", Thread.currentThread().getName()));
    }

    public ChargeControllerInfo chargeController() {
        return chargeControllerInfo;
    }

    public boolean connect() throws UnknownHostException {
        boolean rVal = false;
        Log.d(getClass().getName(), String.format("Connecting to %s", chargeControllerInfo.toString()));
        try {
            disconnect();
            modbusMaster = new ModbusTCPMaster(chargeControllerInfo.getDeviceIp(), chargeControllerInfo.port());

            modbusMaster.connect();
            if (modbusMaster.isConnected()) {
                rVal = true;
            }
        } catch (Exception e1) {
            Log.w(getClass().getName(), String.format("Could not connect to %s, ex: %s", chargeControllerInfo.toString(), e1));
            e1.printStackTrace();
            modbusMaster = null;
            MonitorApplication.chargeControllers().setReachable(chargeControllerInfo.getDeviceIp(), chargeControllerInfo.port(), false);
        }
        return rVal;
    }

    public void disconnect() {
        boolean didDisconnect = false;
        if (isConnected()) {
            synchronized (lock) {
                if (modbusMaster != null) {
                    modbusMaster.disconnect();
                    didDisconnect = true;
                }
                modbusMaster = null;
            }
            Log.d(getClass().getName(), didDisconnect ? String.format("Disconnected from %s", chargeControllerInfo.toString()) : String.format("Tried to Disconnect from %s but did not complete", chargeControllerInfo.toString()));
        }
        clearReadings();
    }

    public boolean isConnected() {
        boolean rVal = false;
        if (modbusMaster != null) {
            rVal = modbusMaster.isConnected();
        }
        return rVal;
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(getClass().getName(), "ModbusTask finalized");
        super.finalize();
    }

    @Override
    public boolean cancel() {
        disconnect();
        Log.d(getClass().getName(), String.format("ModbusTask cancel thread is %s", Thread.currentThread().getName()));
        return super.cancel();
    }

    @Override
    public void run() {
        Log.d(getClass().getName(), String.format("ModbusTask begin run for %s on thread is %s", chargeControllerInfo.toString(), Thread.currentThread().getName()));
        try {
            synchronized (lock) {
                boolean connected = isConnected();
                if (connected == false) {
                    connected = connect();
                }
                if (connected) {
                    if (initialReadingLoaded == false) {
                        initialReadingLoaded = true;
                        if (lookForTriStar() == false) {
                            lookForWhizBangJr();
                            loadBoilerPlateInfo();
                        }
                        MonitorApplication.chargeControllers().setReachable(chargeControllerInfo.getDeviceIp(), chargeControllerInfo.port(), true);
                    }
                    GetModbusReadings();
                    if (chargeControllerInfo.deviceType() == DeviceType.Classic) { // no tristar or kid log support
                        if (getDayLogReacings()) {
                            if (chargeControllerInfo.isCurrent()) { // don't need minute logs for summaries
                                getHourLogReacings();
                            }
                        }
                    }
                }
            }

        } catch (Exception e1) {
            Log.w(getClass().getName(), String.format("Failed to run due to exception ex: %s", e1));
            e1.printStackTrace();
            disconnect();
        }
//        Log.d(getClass().getName(), "end run");

    }

    public boolean getDayLogReacings () throws ModbusException {
        boolean usingCache = false;
        if (dayLogEntry.isEmpty()) {
            loadDayLogs();
        } else {
            DateTime logDate = dayLogEntry.getLogDate();
            if (logDate.isBefore(DateTime.now().withTimeAtStartOfDay())) { // still good?
                loadDayLogs();
            }
            else {
                usingCache = true;
            }
        }
        if (!dayLogEntry.isEmpty()) {
            dayLogEntry.broadcastLogs(context, chargeControllerInfo.uniqueId(), chargeControllerInfo.isCurrent() ? Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS : Constants.CA_FARRELLTONSOLAR_CLASSIC_DAY_LOGS_SLAVE);
        }
        return usingCache;
    }

    public boolean getHourLogReacings () throws ModbusException {
        boolean usingCache = false;
        if (minuteLogEntry.isEmpty()) {
            loadMinuteLogs();
        } else {
            DateTime logDate = minuteLogEntry.getLogDate();
            if (logDate.isBefore(DateTime.now().minusHours(1))) { // still good?
                loadMinuteLogs();
            }
            else {
                usingCache = true;
            }
        }
        if (!minuteLogEntry.isEmpty()) {
            minuteLogEntry.broadcastLogs(context, chargeControllerInfo.uniqueId(), Constants.CA_FARRELLTONSOLAR_CLASSIC_MINUTE_LOGS);
        }
        return usingCache;
    }

    public void clearReadings() {
        readings.set(RegisterName.Power, 0.0f);
        readings.set(RegisterName.BatVoltage, 0.0f);
        readings.set(RegisterName.BatCurrent, 0.0f);
        readings.set(RegisterName.PVVoltage, 0.0f);
        readings.set(RegisterName.PVCurrent, 0.0f);
        readings.set(RegisterName.EnergyToday, 0.0f);
        readings.set(RegisterName.TotalEnergy, 0.0f);
        readings.set(RegisterName.ChargeState, -1);
        readings.set(RegisterName.ConnectionState, 0);
        readings.set(RegisterName.SOC, 0);
        readings.set(RegisterName.Aux1, false);
        readings.set(RegisterName.Aux2, false);
        readings.broadcastReadings(context, chargeControllerInfo.uniqueId(), getAction());
    }

    private void GetModbusReadings() throws ModbusException {
        try {
            if (foundTriStar) {
                Register[] registers = modbusMaster.readMultipleRegisters(0, 80);
                if (registers != null && registers.length == 80) {
                    readings.set(RegisterName.BatVoltage, VScale(registers[24].getValue()));
                    readings.set(RegisterName.PVVoltage, VScale(registers[27].getValue()));
                    readings.set(RegisterName.BatCurrent, IScale(registers[28].getValue()));
                    readings.set(RegisterName.PVCurrent, IScale(registers[29].getValue()));
                    readings.set(RegisterName.ChargeState, StateScale(registers[50].getValue()));
                    readings.set(RegisterName.Power, PScale(registers[58].getValue()));
                    readings.set(RegisterName.EnergyToday, WHr(registers[68].getValue()));
                    readings.set(RegisterName.TotalEnergy, (float) registers[57].getValue());
                    readings.set(RegisterName.BatTemperature, (short)registers[37].getValue() / 1.0f);
                    readings.set(RegisterName.FETTemperature, (short)registers[35].getValue() / 1.0f);
                    readings.set(RegisterName.PCBTemperature, (short)registers[35].getValue() / 1.0f);
                } else {
                    Log.w(getClass().getName(), String.format("Modbus failed to read 0000, readMultipleRegisters returned null"));
                    throw new ModbusException("Failed to read data from modbus 0000");
                }
            } else {
                if (foundWhizBangJr) {
                    Register[] registers2 = modbusMaster.readMultipleRegisters(4360, 22);
                    if (registers2 != null && registers2.length == 22) {
                        Integer val = ((registers2[5].getValue() << 16) + registers2[4].getValue());
                        readings.set(RegisterName.PositiveAmpHours, val);
                        val = (registers2[7].getValue() << 16) + registers2[6].getValue();
                        readings.set(RegisterName.NegativeAmpHours, Math.abs(val));
                        val = (registers2[9].getValue() << 16) + registers2[8].getValue();
                        readings.set(RegisterName.NetAmpHours, val);
                        readings.set(RegisterName.ShuntTemperature, ((short)registers2[11].getValue() & 0x00ff) -50.0f);
                        Register a = registers2[10];
                        readings.set(RegisterName.WhizbangBatCurrent, a.toShort() / 10.0f);
                        Register soc = registers2[12];
                        short socVal = soc.toShort();
                        readings.set(RegisterName.SOC, socVal);
                        readings.set(RegisterName.RemainingAmpHours, registers2[16].toShort());
                        readings.set(RegisterName.TotalAmpHours, registers2[20].toShort());
                    } else {
                        Log.w(getClass().getName(), String.format("Modbus failed to read 4360 readMultipleRegisters returned null"));
                        throw new ModbusException("Failed to read data from modbus 4360");
                    }
                }
                Register[] registers = modbusMaster.readMultipleRegisters(4100, 36);
                if (registers != null && registers.length == 36) {
                    readings.set(RegisterName.BatCurrent, registers[16].getValue() / 10.0f);
                    readings.set(RegisterName.Power, (float) registers[18].getValue());
                    readings.set(RegisterName.BatVoltage, registers[14].getValue() / 10.0f);
                    readings.set(RegisterName.PVVoltage, registers[15].getValue() / 10.0f);
                    readings.set(RegisterName.PVCurrent, registers[20].getValue() / 10.0f);
                    readings.set(RegisterName.EnergyToday, registers[17].getValue() / 10.0f);
                    readings.set(RegisterName.TotalEnergy, ((registers[26].getValue() << 16) + registers[25].getValue()) / 10.0f);
                    readings.set(RegisterName.ChargeState, MSBFor(registers[19].getValue()));
                    readings.set(RegisterName.InfoFlagsBits, ((registers[30].getValue() << 16) + registers[29].getValue()));

                    readings.set(RegisterName.BatTemperature, (short)registers[31].getValue() / 10.0f);
                    readings.set(RegisterName.FETTemperature, (short)registers[32].getValue() / 10.0f);
                    readings.set(RegisterName.PCBTemperature, (short)registers[33].getValue() / 10.0f);
                    int infoFlag = registers[29].getValue();
                    readings.set(RegisterName.Aux1, (infoFlag & 0x4000) != 0);
                    readings.set(RegisterName.Aux2, (infoFlag & 0x8000) != 0);
                } else {
                    Log.w(getClass().getName(), String.format("Modbus failed to read 4100, readMultipleRegisters returned null"));
                    throw new ModbusException("Failed to read data from modbus 4100");
                }

            }
            readings.broadcastReadings(context, chargeControllerInfo.uniqueId(), getAction());
        } catch (Exception all) {
            Log.w(getClass().getName(), String.format("GetModbusReadings Exception ex: %s", all));
            all.printStackTrace();
            throw new ModbusException(all.getMessage());
        }
    }

    private String getAction() {
        return chargeControllerInfo.isCurrent() ? Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS : Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS_SLAVE;
    }

    private void BroadcastToast(String message) {
        Intent intent2 = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_TOAST);
        intent2.putExtra("message", message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
    }

    private void loadBoilerPlateInfo() {
        try {
            Register[] registers = modbusMaster.readMultipleRegisters(4100, 32);
            boolean isClassic = false;
            if (registers != null && registers.length == 32) {
                int unitId = -1;
                short reg1 = (short) registers[0].getValue();
                if (reg1 == 0) { // the kid?
                    chargeControllerInfo.setModel("The Kid");
                    chargeControllerInfo.setDeviceType(DeviceType.Kid);
                }
                else {
                    String model = String.format("Classic %d (rev %d)", reg1 & 0x00ff, reg1 >> 8);
                    chargeControllerInfo.setModel(model);
                    chargeControllerInfo.setDeviceType(DeviceType.Classic);
                    isClassic = true;
                }
                int buildYear = registers[1].getValue();
                int buildMonthDay = registers[2].getValue();
                DateTime buildDate = new DateTime(buildYear, (buildMonthDay >> 8), (buildMonthDay & 0x00ff), 0, 0);
                chargeControllerInfo.setBuildDate(DateTimeFormat.fullDate().print(buildDate));
                short reg6 = registers[5].toShort();
                short reg7 = registers[6].toShort();
                short reg8 =  registers[7].toShort();
                String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", reg8 >> 8, reg8 & 0x00ff, reg7 >> 8, reg7 & 0x00ff, reg6 >> 8, reg6 & 0x00ff);
                chargeControllerInfo.setMacAddress(macAddress);
                float reg22 = (float) registers[21].getValue();
                chargeControllerInfo.setLastVOC(reg22 / 10);
                unitId = (registers[11].getValue() << 16) + registers[10].getValue();
                chargeControllerInfo.setUnitID(unitId);
            }
            registers = modbusMaster.readMultipleRegisters(4209, 4);
            if (registers != null && registers.length == 4) {
                byte[] v0 = registers[0].toBytes();
                byte[] v1 = registers[1].toBytes();
                byte[] v2 = registers[2].toBytes();
                byte[] v3 = registers[3].toBytes();

                byte[] temp = new byte[8];
                temp[0] = v0[1];
                temp[1] = v0[0];
                temp[2] = v1[1];
                temp[3] = v1[0];
                temp[4] = v2[1];
                temp[5] = v2[0];
                temp[6] = v3[1];
                temp[7] = v3[0];
                String unitName = new String(temp);
                chargeControllerInfo.setDeviceName(unitName);
                Log.i(getClass().getName(), String.format("Modbus device unitName is %s ", unitName));
            }
            registers = modbusMaster.readMultipleRegisters(4244, 2);
            if (registers != null && registers.length == 2) {
                short reg4245 = (short) registers[0].getValue();
                chargeControllerInfo.setNominalBatteryVoltage(reg4245);
            }
            if (isClassic){
                registers = modbusMaster.readMultipleRegisters(16386, 4);
                if (registers != null && registers.length == 4) {
                    short reg16387 = registers[0].toShort();
                    short reg16388 = registers[1].toShort();
                    short reg16389 = registers[2].toShort();
                    short reg16390 = registers[3].toShort();
                    chargeControllerInfo.setAppVersion(String.format("%d", (reg16388 << 16) + reg16387));
                    chargeControllerInfo.setNetVersion(String.format("%d", (reg16390 << 16) + reg16389));
                }
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "loadBoilerPlateInfo failed ex: %s", e);
        }
    }

    private boolean lookForWhizBangJr() throws ModbusException {
        foundWhizBangJr = false;
        Register[] registers = modbusMaster.readMultipleRegisters(4360, 12);
        if (registers != null && registers.length == 12) {
            Register a = registers[10];
            foundWhizBangJr = a.toShort() != 0;
        }
        chargeControllerInfo.setHasWhizbang(foundWhizBangJr);
        return foundWhizBangJr;
    }

    private boolean lookForTriStar() {
        foundTriStar = false;
        try {
            Register[]registers = modbusMaster.readMultipleRegisters(0, 4); // see if its a tristar
            if (registers != null && registers.length == 4) {
                foundTriStar = registers[0].toShort() != 0;
                if (foundTriStar) {
                    chargeControllerInfo.setDeviceName("TriStar");
                    chargeControllerInfo.setDeviceType(DeviceType.TriStar);
                    float hi = registers[0].toShort();
                    float lo = registers[1].toShort();
                    lo = lo / 65536;
                    v_pu = hi + lo;

                    hi = (float) registers[2].toShort();
                    lo = (float) registers[3].toShort();
                    lo = lo / 65536;
                    i_pu = hi + lo;
                }
            }
        } catch (ModbusException e) {
            Log.d(getClass().getName(), "This is probably not a Tristar!");
        }
        return foundTriStar;
    }

    private void loadDayLogs() throws ModbusException {
        String dayLogCacheName = chargeControllerInfo.dayLogCacheName();
        try {
            Bundle dayLogs = BundleCache.getInstance(context).getBundle(dayLogCacheName);
            if (dayLogs != null && dayLogs.size() > 0) {
                dayLogEntry = new LogEntry(dayLogs);
                DateTime logDate = dayLogEntry.getLogDate();
                if (logDate.isAfter(DateTime.now().withTimeAtStartOfDay())) { // still good?
                    Log.d(getClass().getName(), "DayLog cache still good to go");
                    return;
                }
                Log.d(getClass().getName(), "DayLog cache stale, reload data from modbus");
            }
            dayLogEntry.set(Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_KWHOUR_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_FLOAT_TIME_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_HIGH_POWER_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_HIGH_TEMP_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_HIGH_PV_VOLT_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            dayLogEntry.set(Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY, ReadLogs(100, Constants.CLASSIC_HIGH_BATTERY_VOLT_DAILY_CATEGORY, Constants.MODBUS_FILE_DAILIES_LOG, 1));
            Log.d(getClass().getName(), "Completed reading Day logs");
            dayLogEntry.setLogDate(DateTime.now());
            BundleCache.getInstance(context).putBundle(dayLogCacheName, dayLogEntry.getLogs());
            BroadcastToast(context.getString(R.string.toast_day_logs));
        } catch (ModbusIOException ex) {
            if (ex.isEOF()) {
                Log.w(getClass().getName(), String.format("loadDayLogs reached EOF ex: %s", ex));
                dayLogEntry.setLogDate(DateTime.now());
                BundleCache.getInstance(context).putBundle(dayLogCacheName, dayLogEntry.getLogs());
                BroadcastToast(context.getString(R.string.toast_day_logs));
                return;
            }
            BundleCache.getInstance(context).clearCache(dayLogCacheName);
            dayLogEntry = new LogEntry();
            dayLogEntry.setLogDate(DateTime.now().plusMinutes(5)); // try it again later
            Log.w(getClass().getName(), String.format("loadDayLogs failed ex: %s", ex));
        } catch (Exception ex) {
            BundleCache.getInstance(context).clearCache(dayLogCacheName);
            dayLogEntry = new LogEntry();
            dayLogEntry.setLogDate(DateTime.now().plusMinutes(5)); // try it again later
            Log.w(getClass().getName(), String.format("loadDayLogs failed ex: %s", ex));
        }
    }

    private void loadMinuteLogs() throws ModbusException {
        String minuteLogCacheName = chargeControllerInfo.minuteLogCacheName();
        try {
            Bundle minuteLog = BundleCache.getInstance(context).getBundle(minuteLogCacheName);
            if (minuteLog != null && minuteLog.size() > 0) {
                minuteLogEntry = new LogEntry(minuteLog);
                DateTime logDate = minuteLogEntry.getLogDate();
                if (logDate.isAfter(DateTime.now().minusHours(1))) { // still good?
                    Log.d(getClass().getName(), "MinuteLog cache still good to go");
                    return;
                }
                Log.d(getClass().getName(), "MinuteLog cache stale, reload data from modbus");
            }
            int requiredEntries = ReadMinuteLogTimestamps(); // sum of minutes log up to 24 hours
            minuteLogEntry.set(Constants.CLASSIC_POWER_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_POWER_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 1));
            minuteLogEntry.set(Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_INPUT_VOLTAGE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_BATTERY_VOLTAGE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_OUTPUT_CURRENT_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_ENERGY_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_ENERGY_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 10));
            minuteLogEntry.set(Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY, ReadLogs(requiredEntries, Constants.CLASSIC_CHARGE_STATE_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG, 256));
            Log.d(getClass().getName(), "Completed reading minute logs");
            minuteLogEntry.setLogDate(DateTime.now());
            BundleCache.getInstance(context).putBundle(minuteLogCacheName, minuteLogEntry.getLogs());
            BroadcastToast(context.getString(R.string.toast_minute_logs));
        } catch (ModbusIOException ex) {
            if (ex.isEOF()) {
                Log.w(getClass().getName(), String.format("loadMinuteLogs reached EOF ex: %s", ex));
                minuteLogEntry.setLogDate(DateTime.now());
                BundleCache.getInstance(context).putBundle(minuteLogCacheName, minuteLogEntry.getLogs());
                BroadcastToast(context.getString(R.string.toast_minute_logs));
                return;
            }
            BundleCache.getInstance(context).clearCache(minuteLogCacheName);
            minuteLogEntry = new LogEntry();
            minuteLogEntry.setLogDate(DateTime.now().plusMinutes(5)); // try it again later
            Log.w(getClass().getName(), String.format("loadDayLogs failed ex: %s", ex));

        } catch (Exception ex) {
            BundleCache.getInstance(context).clearCache(minuteLogCacheName);
            minuteLogEntry = new LogEntry();
            minuteLogEntry.setLogDate(DateTime.now().plusMinutes(5)); // try it again later
            Log.w(getClass().getName(), String.format("LoadMinuteLogs failed ex: %s", ex));
        }
    }

    private float[] ReadLogs(int requiredEntries, int category, int device, int factor) throws ModbusException {
        int index = 0;
        float[] buffer = new float[requiredEntries];
        while (index < requiredEntries) {
            ReadFileTransferResponse regRes = modbusMaster.readFileTransfer(index, category, device);
            if (regRes != null) {
                int count = regRes.getWordCount();
                if (count > 0) {
                    int j = count - 1;
                    for (int i = 0; i < count; i++, j--) {
                        if (i + index > requiredEntries - 1) {
                            break;
                        }
                        float value = (float)registerToShort(regRes.getRegister(j).toBytes());
                        buffer[i + index] = value / factor;
                    }
                    index += count;
                }
            } else {
                Log.w(getClass().getName(), String.format("Modbus ReadLogs failed to get category: %d", category));
                throw new ModbusException("Failed to read File Transfer data from modbus");
            }
        }

        return buffer;
    }

    private int ReadMinuteLogTimestamps() throws ModbusException {
        final int bufferSize = 1440; // assume max of one entry per minute for 20 hrs
        int requiredEntries = 0;
        int index = 0;
        short lasMinute;
        short currentMinute = -1;
        short minuteSum = 0;
        short[] buffer = new short[bufferSize];
        try {

            while (index < bufferSize) {
                ReadFileTransferResponse regRes = modbusMaster.readFileTransfer(index, Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, Constants.MODBUS_FILE_MINUTES_LOG);
                if (regRes != null) {
                    int count = regRes.getWordCount();
                    if (count > 0) {
                        int j = count - 1;
                        for (int i = 0; i < count; i++, j--) {
                            lasMinute = currentMinute;
                            if (i + index > bufferSize - 1) {
                                break;
                            }
                            short val = registerToShort(regRes.getRegister(j).toBytes());
                            short min = (short) (val & 0x003f);
                            short hour = (short) ((val >> 6) & 0x001f);
                            currentMinute = (short) (min + hour * 60);

                            if (lasMinute != -1) {
                                if (currentMinute > lasMinute) {
                                    lasMinute += 1440; // roll over midnight
                                }
                                minuteSum += lasMinute - currentMinute;
                                buffer[i + index] = minuteSum;
                                if (minuteSum > 1440) { //minutes in 24 hours
                                    requiredEntries = i + index; // output buffer size required
                                    index = bufferSize; //exit while
                                    break; //exit for
                                }
                            }
                        }
                        index += count;
                    }
                } else {
                    Log.w(getClass().getName(), String.format("Modbus ReadLogs failed to get timestamps"));
                    throw new ModbusException("Failed to read File Transfer data from modbus");
                }
            }
        }catch (ModbusIOException ex) {
            if (ex.isEOF()) {
                if (requiredEntries == 0) {
                    throw new ModbusException("Could not load Minute Log Timestamps");
                }
            }
        }
        short[] output = new short[requiredEntries];
        System.arraycopy(buffer, 0, output, 0, requiredEntries);
        minuteLogEntry.set(Constants.CLASSIC_TIMESTAMP_HIGH_HOURLY_CATEGORY, output);
        return requiredEntries;
    }

    private static short registerToShort(byte[] bytes) {
        return (short) ((bytes[1] << 8) | (bytes[0] & 0xff));
    }

    private int MSBFor(int val) {
        return val >> 8;
    }


    private float WHr(float val) {
        val /= 1000;
        return val;
    }

    private float PScale(float val) {
        val = val * v_pu * i_pu;
        val /= 131072;
        return val;
    }

    private float VScale(float val) {
        val = val * v_pu;
        val /= 32768;
        return val;
    }

    private float IScale(float val) {
        val = val * i_pu;
        val /= 32768;
        return val;
    }

//    0 START
//    1 NIGHT_CHECK
//    2 DISCONNECT
//    3 NIGHT
//    4 FAULT
//    5 MPPT
//    6 ABSORPTION
//    7 FLOAT
//    8 EQUALIZE
//    9 SLAVE

//    chargeStateTitles.put(-1, "");
//    chargeStateTitles.put(0, getString(R.string.RestingTitle));
//    chargeStateTitles.put(3, getString(R.string.AbsorbTitle));
//    chargeStateTitles.put(4, getString(R.string.BulkMPPTTitle));
//    chargeStateTitles.put(5, getString(R.string.FloatTitle));
//    chargeStateTitles.put(6, getString(R.string.FloatMPPTTitle));
//    chargeStateTitles.put(7, getString(R.string.EqualizeTitle));
//    chargeStateTitles.put(10, getString(R.string.HyperVocTitle));
//    chargeStateTitles.put(18, getString(R.string.EqMpptTitle));
    private int StateScale(int value) {
        int rVal = 0;
        switch (value) {
            case 0:
            case 1:
            case 3:
                rVal = 0;
                break;
            case 2:
                rVal = -1;
                break;
            case 4:
                rVal = 10;
                break;
            case 6:
                rVal = 3;
                break;
            case 5:
                rVal = 4;
                break;
            case 7:
                rVal = 5;
                break;
            case 8:
                rVal = 7;
                break;
        }
        return rVal;
    }

    // END Tristar code...
}
