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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by Graham on 08/12/2014.
 */
public class UDPListener extends Service {

    final Object lock = new Object();
    private final IBinder mBinder = new UDPListenerServiceBinder();
    private static Gson GSON = new Gson();
    private ListenerThread mListener;

    public UDPListener() {
    }

    public class UDPListenerServiceBinder extends Binder {
        UDPListener getService() {
            return UDPListener.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopListening();
        super.onDestroy();
        Log.d(getClass().getName(), "onDestroy");
    }

    public void listen(ChargeControllers currentCCs) {
        stopListening();
        mListener = new ListenerThread(currentCCs);
        mListener.setUncaughtExceptionHandler(setUncaughtExceptionHandler);
        mListener.start();
        Log.d(getClass().getName(), "UDP Listener running");
    }

    private Thread.UncaughtExceptionHandler setUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.wtf(getClass().getName(), String.format("UDPListener thread uncaughtException ex: %s on thread %s", ex, thread.getName()));
        }
    };

    public void stopListening() {
        if (mListener != null) {
            mListener.SetRunning(false);
            mListener = null;
            Log.d(getClass().getName(), "stopListening");
        }
    }


    class ListenerThread extends Thread {
        private boolean running;
        private DatagramSocket socket;
        private byte[] buffer = new byte[16];
        private DatagramPacket packet;
        private ArrayList<InetSocketAddress> alreadyFoundList = new ArrayList<>();
        private ChargeControllers currentChargeControllers;

        private void addToAlreadyFoundList(InetSocketAddress socketAddress) {
            synchronized (lock) {
                alreadyFoundList.add(socketAddress);
            }
        }

        private boolean hasAddressAlreadyBeenFound(InetSocketAddress socketAddress) {
            boolean rVal = false;
            synchronized (lock) {
                for (InetSocketAddress cc : alreadyFoundList) {
                    if (cc.equals(socketAddress)) {
                        rVal = true;
                        break;
                    }
                }
            }
            return rVal;
        }

        public ListenerThread(ChargeControllers existingControllers) {
            currentChargeControllers = existingControllers;
        }

        private boolean GetRunning() {
            synchronized (lock) {
                return running;
            }
        }

        public void SetRunning(boolean state) {
            synchronized (lock) {
                running = state;
                if (state == false && socket != null) {
                    socket.close();
                    socket.disconnect();
                    socket = null;
                }
            }
        }

        @Override
        public void run() {
            int sleepTime = Constants.UDPListener_Minimum_Sleep_Time;
            SetRunning(true);
            int retryCount = 10;

            do {
                do {
                    try {
                        socket = new DatagramSocket(Constants.CLASSIC_UDP_PORT);
                        socket.setSoTimeout(5000);
                        break;
                    } catch (IOException ex) {
                        Log.w(getClass().getName(), String.format("Creating datagram ListenerThread failed, retry count %d ex: %s", retryCount, ex));
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Log.w(getClass().getName(), String.format("Creating datagram ListenerThread sleep Interrupted ex: %s", ie));
                        return;
                    }
                } while (--retryCount > 0);
                if (retryCount > 0) break;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e2) {
                    Log.w(getClass().getName(), String.format("Creating datagram ListenerThread sleep Interrupted ex: %s", e2));
                    return;
                }
            } while (true);
            packet = new DatagramPacket(buffer, buffer.length);
            try {
                currentChargeControllers.load(alreadyFoundList, false); // all known controllers
                do {
                    try {
                        socket.receive(packet);
                        byte[] data = packet.getData();
                        byte[] addr = new byte[4];
                        if (data[0] == -87 && data[1] == -2) { // 169.254 ==> Microsoft reserved for private addressing
                            continue;
                        }
                        addr[0] = data[0];
                        addr[1] = data[1];
                        addr[2] = data[2];
                        addr[3] = data[3];
                        InetAddress address = InetAddress.getByAddress(addr);
                        int port = ((int) data[4] & 0xff);
                        port += ((long) data[5] & 0xffL) << (8);
                        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
                        if (hasAddressAlreadyBeenFound(socketAddress) == false) {
                            Log.d(getClass().getName(), "Found new classic at address: " + address + " port: " + port);
                            addToAlreadyFoundList(socketAddress);
                            ChargeControllerInfo cc = new ChargeControllerInfo(socketAddress);
                            cc.setDeviceType(DeviceType.Classic); // probably a classic if it auto detected
                            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(UDPListener.this);
                            Intent pkg = new Intent(Constants.CA_FARRELLTONSOLAR_CLASSIC_ADD_CHARGE_CONTROLLER);
                            pkg.putExtra("ChargeController", GSON.toJson(cc));
                            broadcaster.sendBroadcast(pkg);
                            sleepTime = Constants.UDPListener_Minimum_Sleep_Time;
                        }
                    } catch (SocketTimeoutException iox) {
                        // expect a timeout exception when no classic on the network
//                        Log.w(getClass().getName(), "SocketTimeoutException: " + iox);
                    } catch (IOException ex) {
                        if (socket != null && socket.isClosed()) {
                            break;
                        }
                        Log.w(getClass().getName(), "IOException: " + ex);
                    }
                    Thread.sleep(sleepTime);
                    if (sleepTime < Constants.UDPListener_Maximum_Sleep_Time) {
                        sleepTime += Constants.UDPListener_Minimum_Sleep_Time;
                    }
                } while (GetRunning());
            } catch (Exception e) {
                Log.w(getClass().getName(), "mListener Exception: " + e);
            } finally {
                SetRunning(false);
                Log.d(getClass().getName(), "closed socket and disconnected");
            }
            Log.d(getClass().getName(), "mListener exiting");
        }
    }
}
