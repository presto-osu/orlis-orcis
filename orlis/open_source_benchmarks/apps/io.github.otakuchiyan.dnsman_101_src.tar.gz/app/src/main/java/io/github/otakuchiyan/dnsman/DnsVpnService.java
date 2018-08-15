package io.github.otakuchiyan.dnsman;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Enumeration;

public class DnsVpnService extends VpnService implements ValueConstants{
    private ParcelFileDescriptor fd;
    private Thread vpnThread;

    public DnsVpnService() {
    }

    public static void perform(Context c, String dns1, String dns2){
        Intent i = new Intent(c, DnsVpnService.class);
        i.putExtra(ValueConstants.EXTRA_DNS1, dns1);
        i.putExtra(ValueConstants.EXTRA_DNS2, dns2);
        c.startService(i);
    }

    //FIXME: cannot stop vpn
    public int disconnect(){
        if(vpnThread != null) {
            //try {
                //vpnThread.join(1000);
                vpnThread.interrupt();
                stopSelf();
                return ValueConstants.RESTORE_SUCCEED;
            //} catch (InterruptedException e) {
              //  Log.e("VpnService", "vpnThread did not exit");
            //}
        }
        return ValueConstants.ERROR_NULL_VPN;
    }

    private String getAddress(){
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                 ifaces.hasMoreElements(); ) {
                Enumeration<InetAddress> addresses = ifaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String addr = addresses.nextElement().getHostAddress();
                    if (!addr.equals("127.0.0.1") &&
                            !addr.equals("0.0.0.0") &&
                            !addr.equals("::1%1") &&
                            //Escaping IPv6
                            addr.charAt(5) != ':') {
                        return addr;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId){
        final String dns1 = i.getStringExtra(ValueConstants.EXTRA_DNS1);
        final String dns2 = i.getStringExtra(ValueConstants.EXTRA_DNS2);

        vpnThread = new Thread(new Runnable() {
                @Override
                public void run() {
                try {
                    String addr = getAddress();
                    String real_addr = "";

                    //Escaping IPv6 address suffix - "<Real address>%wlan0"
                    for (int i = addr.length() - 1; i != 0; i--) {
                        if (addr.charAt(i) == '%') {
                            real_addr = addr.substring(0, i);
                        }
                    }

                    //If no suffix
                    if (real_addr.equals("")) {
                        real_addr = addr;
                    }

                    Log.d("DnsVpn", "addr = " + real_addr);
                    DatagramChannel tunnel = DatagramChannel.open();

                    tunnel.connect(new InetSocketAddress(addr, 8087));
                    tunnel.configureBlocking(false);

                    Builder vpn = new Builder();
                    vpn.setSession("DnsVpnService")
                            .addAddress(real_addr, 24);
                    if (!dns1.equals("")) {
                        vpn.addDnsServer(dns1);
                    }
                    if (!dns2.equals("")) {
                        vpn.addDnsServer(dns2);
                    }
                    fd = vpn.establish();
                    sendResult(0, dns1, dns2);

                    while (true) {
                        Thread.sleep(10000);
                    }
                } catch (Exception e) {
                    if(e instanceof IllegalArgumentException){
                        e.printStackTrace();
                        sendResult(ERROR_BAD_ADDRESS);
                    }
                } finally {
                    try {
                        if (fd != null) {
                            fd.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        vpnThread.start();
        return START_STICKY;
    }

    private void sendResult(int result_code){
        sendResult(result_code, "", "");
    }

    private void sendResult(int result_code, String dns1, String dns2){
        Intent result_intent = new Intent(ACTION_SET_DNS);
        result_intent.putExtra(EXTRA_RESULT_CODE, result_code);
        result_intent.putExtra(EXTRA_DNS1, dns1);
        result_intent.putExtra(EXTRA_DNS2, dns2);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(result_intent);
    }
}
