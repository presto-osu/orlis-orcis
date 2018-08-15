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
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class ChargeControllers {

    final transient Object lock = new Object();
    private static Context context;
    private String APIKey = "";
    private List<ChargeController> devices = new ArrayList<>();
    private boolean useFahrenheit = false;
    private boolean autoDetectClassic = true;
    private boolean showPopupMessages = true;
    private boolean uploadToPVOutput = false;
    private boolean bidirectionalUnitsInWatts;
    private boolean systemViewEnabled = false;
    private PVOutputSetting pVOutputSetting = new PVOutputSetting();

    // default ctor for de-serialization
    public ChargeControllers() {
    }

    public ChargeControllers(Context context) {
        this.context = context;
    }

    public ChargeController get(int position) {
        synchronized (devices) {
            return devices.get(position);
        }
    }

    public ChargeController getCurrentChargeController() {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isCurrent()) {
                    return cc;
                }
            }
        }
        return null; // none selected
    }

    public int getCurrentControllerIndex() {
        synchronized (devices) {
            for (int index = 0; index < devices.size(); index++) {
                if (devices.get(index).isCurrent()) {
                    return index;
                }
            }
        }
        return -1; // none selected
    }

    public boolean setCurrent(int position) {
        if (position >= devices.size()) {
            throw new IndexOutOfBoundsException();
        }
        synchronized (devices) {
            for (int index = 0; index < devices.size(); index++) {
                ChargeController cc = devices.get(index);
                if (cc.isCurrent() && index == position) {
                    return false; // already current
                } else {
                    cc.setIsCurrent(false);
                }
            }
            ChargeControllerInfo cc = devices.get(position);
            devices.get(position).setIsCurrent(true);
        }
        return true;
    }

    public void add(ChargeControllerInfo ccInfo) {
        ChargeController newCC = new ChargeController(ccInfo);
        synchronized (devices) {
            devices.add(newCC);
        }
        BroadcastUpdateNotification();
    }

    public void remove(ChargeControllerInfo cc) {
        synchronized (devices) {
            cc.clearLogCache();
            devices.remove(cc);
        }
        BroadcastUpdateNotification();
        BroadcastRemoveNotification(cc.uniqueId());
    }

    public int count() {
        synchronized (devices) {
            return devices.size();
        }
    }

    // number of classics configured or the number of devices that provide day log data
    public int classicCount() {
        synchronized (devices) {
            int count = 0;
            for (ChargeController cc : devices) {
                if (cc.deviceType() == DeviceType.Classic) {
                    count++;
                }
            }
            return count;
        }
    }

    public void clear() {
        synchronized (devices) {
            devices.clear();
        }
        BroadcastUpdateNotification();
    }

    public void load(ArrayAdapter adapter) {
        synchronized (devices) {
            adapter.addAll(devices);
        }
    }

    public void load(ArrayList<InetSocketAddress> arr, boolean staticOnly) throws UnknownHostException {
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (cc.isCurrent()) {
                    arr.add(cc.getInetSocketAddress());
                } else if (!staticOnly || cc.isStaticIP()) { // all non current or all static non current
                    arr.add(cc.getInetSocketAddress());
                }
            }
        }
    }

    public void setReachable(String deviceIpAddress, int port, boolean state) {
        boolean updated = false;
        synchronized (devices) {
            for (ChargeController cc : devices) {
                if (deviceIpAddress.compareTo(cc.deviceIpAddress()) == 0 && port == cc.port()) {
                    updated = cc.setIsReachable(state);
                    break;
                }
            }
        }
        if (updated) {
            BroadcastUpdateNotification();
        }
    }

    private void BroadcastUpdateNotification() {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_UPDATE_CHARGE_CONTROLLERS);
        broadcaster.sendBroadcast(pkg);
    }

    private void BroadcastRemoveNotification(String removedCC) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_REMOVE_CHARGE_CONTROLLER);
        pkg.putExtra("uniqueId", removedCC);
        broadcaster.sendBroadcast(pkg);
    }


    public synchronized boolean isBidirectionalUnitsInWatts() {
        return bidirectionalUnitsInWatts;
    }

    public synchronized void setBidirectionalUnitsInWatts(boolean bidirectionalUnitsInWatts) {
        this.bidirectionalUnitsInWatts = bidirectionalUnitsInWatts;
    }

    public synchronized boolean useFahrenheit() {
        return useFahrenheit;
    }

    public synchronized void setFahrenheit(boolean useFahrenheit) {
        this.useFahrenheit = useFahrenheit;
    }

    public synchronized boolean autoDetectClassic() {
        return autoDetectClassic;
    }

    public synchronized void setAutoDetectClassic(boolean autoDetectClassic) {
        this.autoDetectClassic = autoDetectClassic;
        MonitorApplication.ConfigurationChanged();
    }

    public synchronized boolean showPopupMessages() {
        return showPopupMessages;
    }

    public synchronized void setShowPopupMessages(boolean showPopupMessages) {
        this.showPopupMessages = showPopupMessages;
    }

    public synchronized boolean showSystemView() {

        return systemViewEnabled && count() > 1;
    }

    public synchronized boolean systemViewEnabled() {
        return systemViewEnabled;
    }

    public synchronized void setSystemViewEnabled(boolean systemViewEnabled) {
        this.systemViewEnabled = systemViewEnabled;
    }

    public synchronized String aPIKey() {
        return APIKey;
    }

    public synchronized void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }



    public synchronized Boolean uploadToPVOutput() {
        return uploadToPVOutput;
    }

    public synchronized void setUploadToPVOutput(Boolean uploadToPVOutput) {
        this.uploadToPVOutput = uploadToPVOutput;
    }

    public synchronized PVOutputSetting getPVOutputSetting() {
        if (systemViewEnabled) {
            return pVOutputSetting;
        }
        else {
            ChargeController controller = getCurrentChargeController();
            if (controller != null) {
                PVOutputSetting rSetting = controller.getPVOutputSetting();
                // Sid could have been set when in systemView
                if (rSetting.getSID() == null || rSetting.getSID().length() == 0) {
                    rSetting.setSID(pVOutputSetting.getSID());
                }
                return rSetting;
            }
            else {
                return null;
            }
        }
    }

    public void resetPVOutputLogs() {
        pVOutputSetting.resetPVOutputEntry();
        synchronized (devices) {
            for (ChargeController cc : devices) {
                cc.getPVOutputSetting().resetPVOutputEntry();
            }
        }
    }

    public void resetCurrentPVOutputLogs() {
        if (systemViewEnabled) {
            pVOutputSetting.resetPVOutputEntry();
        }
        else {
            ChargeController controller = getCurrentChargeController();
            if (controller != null) {
                controller.getPVOutputSetting().resetPVOutputEntry();
            }
        }
    }
}
