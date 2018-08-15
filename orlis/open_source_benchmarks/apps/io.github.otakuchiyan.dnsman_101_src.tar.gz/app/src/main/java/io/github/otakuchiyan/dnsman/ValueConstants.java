package io.github.otakuchiyan.dnsman;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.lang.Integer;

public interface ValueConstants {
    int[] NET_TYPE_LIST = {
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_MOBILE,
            ConnectivityManager.TYPE_BLUETOOTH,
            ConnectivityManager.TYPE_ETHERNET,
            ConnectivityManager.TYPE_WIMAX
    };
    int[] NET_TYPE_RESOURCES = {
            R.string.category_wifi,
            R.string.category_mobile,
            R.string.category_bluetooth,
            R.string.category_ethernet,
            R.string.category_wimax
    };

    //Default interfaces name
    String INTERFACE_WLAN0 = "wlan0";
    String INTERFACE_RMNET0 = "rmnet0";
    String INTERFACE_BT_PAN = "bt-pan";
    String INTERFACE_ETH0 = "eth0";

    //It suitable most devices
    String[] NETWORK_INTERFACES = {
            INTERFACE_WLAN0,
            INTERFACE_RMNET0,
            INTERFACE_BT_PAN,
            INTERFACE_ETH0,
            "" //TODO: Find out what is the interface name of WiMAX
    };

    String[] DEFAULT_DNS_LIST = {
            "127.0.0.1",
            "192.168.0.1",
            "192.168.100.1",
            "8.8.8.8",
            "8.8.4.4",
            "208.67.222.222",
            "208.67.220.220"
    };

    String KEY_DNS_LIST = "dns_list";
    String KEY_FIRST_BOOT = "first_boot";
    String KEY_HIJACKED_LAST_DNS = "hijacked_last_dns";
    String KEY_NETWORK_DNS1 = "network_dns1";
    String KEY_NETWORK_DNS2 = "network_dns2";
    String KEY_LAST_DNS1 = "last_dns1";
    String KEY_LAST_DNS2 = "last_dns2";
    String KEY_IS_RESTORE = "is_restore";
    String KEY_IS_ROOT = "is_root";

    String KEY_PREF_AUTO_SETTING = "pref_auto_setting";
    String KEY_PREF_FULL_KEYBOARD = "pref_full_keyboard";
    String KEY_PREF_INDIVIDUAL_MODE = "pref_individual_mode";
    String KEY_PREF_AUTO_FLUSH = "pref_auto_flush";

    String KEY_PREF_TOAST = "pref_toast";
    String TOAST_SHOW = "show";
    String TOAST_ERROR = "error";
    String TOAST_NEVER = "never";

    String KEY_PREF_NOTIFICATION = "pref_notification";

    String KEY_PREF_METHOD = "pref_method";
    String METHOD_VPN = "vpn";
    String METHOD_ACCESSIBILITY = "accessibility";
    String METHOD_NDC = "ndc";
    String METHOD_IPTABLES = "iptables";
    String METHOD_DELETE_RULES = "delete_rules";
    String METHOD_MODULE = "module";
    String METHOD_SETPROP = "setprop";

    String KEY_PREF_NDC_INTERFACE = "pref_ndc_interface";

    String[] METHODS = {
            METHOD_VPN,
            METHOD_ACCESSIBILITY,
            METHOD_NDC,
            METHOD_IPTABLES,
            METHOD_MODULE,
            METHOD_SETPROP
    };

    int[] METHOD_RESOURCES = {
            R.string.pref_method_vpn,
            R.string.pref_method_accessibility,
            R.string.pref_method_ndc,
            R.string.pref_method_iptables,
            R.string.pref_method_module,
            R.string.pref_method_setprop
    };

    String KEY_NDC_WLAN = "pref_ndc_wlan";
    String KEY_NDC_RMNET = "pref_ndc_rmnet";
    String KEY_NDC_BT = "pref_ndc_bt";
    String KEY_NDC_ETH = "pref_ndc_eth";

    String KEY_VPN_DELAY = "pref_vpn_delay";

    String[] KEY_CUSTOM_INTERFACES = {
            KEY_NDC_WLAN,
            KEY_NDC_RMNET,
            KEY_NDC_BT,
            KEY_NDC_ETH,
            ""
    };


    //EXTRAs
    String EXTRA_METHOD = "extra.method";
    String EXTRA_RESULT_CODE = "extra.result_code";
    String EXTRA_DNS1 = "extra.DNS1";
    String EXTRA_DNS2 = "extra.DNS2";


    //prop
    String SETPROP_COMMAND_PREFIX = "setprop net.dns";
    String GETPROP_COMMAND_PREFIX = "getprop net.dns";

    String[] CHECKPROP_COMMANDS = {
            GETPROP_COMMAND_PREFIX + "1",
            GETPROP_COMMAND_PREFIX + "2"
    };

    //IPTABLES mode
    String SETRULE_COMMAND = "iptables -t nat %s OUTPUT -p %s --dport 53 -j DNAT --to-destination %s\n";
    String CHECKRULE_COMMAND_PREFIX = "iptables -t nat -L OUTPUT | grep ";

    //NDC mode
    String NDC_COMMAND_PREFIX = "ndc resolver";
    String SETIFDNS_COMMAND_BELOW_42 = NDC_COMMAND_PREFIX + " setifdns %s %s %s\n";
    String SETIFDNS_COMMAND = NDC_COMMAND_PREFIX + " setifdns %s '' %s %s\n";
    String SETNETDNS_COMMAND = NDC_COMMAND_PREFIX + " setnetdns %s '' %s %s\n";
    String SETDEFAULTIF_COMMAND = NDC_COMMAND_PREFIX + " setdefaultif";

    String FLUSHNET_COMMAND = NDC_COMMAND_PREFIX + " flushnet %s\n";
    String FLUSHDEFAULTIF_COMMAND = NDC_COMMAND_PREFIX + " flushdefaultif\n";

    String PACKAGE_NAME = "io.github.otakuchiyan.dnsman";
    String ACTION_SET_DNS = PACKAGE_NAME + ".ACTION_SET_DNS";
    String ACTION_CHANGE_AUTO_SETTING = PACKAGE_NAME + ".CHANGE_AUTO_SETTING";
    String ACTION_NETWORK_CONNECTED = PACKAGE_NAME + ".NETWORK_CONNECTED";

    //<=1000 is no error
    int RESTORE_SUCCEED = 4;

    int ERROR_NO_DNS = 1002;
    int ERROR_GET_NETID_FAILED = 1003;
    int ERROR_NULL_VPN = 1004;
    int ERROR_BAD_ADDRESS = 1005;
    int ERROR_GET_CURRENT_NETWORK_FAILED = 1006;

    int ERROR_UNKNOWN = 9999;

    int REQUEST_DNS_CHANGE = 0x00;
    int REQUEST_VPN = 0x01;
    int REQUEST_METHOD_CHANGE = 0x02;
}
