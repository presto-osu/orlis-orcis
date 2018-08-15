package org.pixmob.freemobile.netstat;

import android.telephony.TelephonyManager;

/**
 * Network classes (2G, 3G, …).
 */
public enum NetworkClass {
	NC_UNKNOWN, NC_2G, NC_3G, NC_4G;

	/**
	 * Converts a network type into a network class.
	 *
	 * @param networkType the network type
	 * @return the network class
	 */
	public static NetworkClass getNetworkClass(int networkType) {
		switch (networkType) {
	        case TelephonyManager.NETWORK_TYPE_GPRS:
	        case TelephonyManager.NETWORK_TYPE_EDGE:
	        case TelephonyManager.NETWORK_TYPE_CDMA:
	        case TelephonyManager.NETWORK_TYPE_1xRTT:
	        case TelephonyManager.NETWORK_TYPE_IDEN:
	            return NC_2G;
	        case TelephonyManager.NETWORK_TYPE_UMTS:
	        case TelephonyManager.NETWORK_TYPE_EVDO_0:
	        case TelephonyManager.NETWORK_TYPE_EVDO_A:
	        case TelephonyManager.NETWORK_TYPE_HSDPA:
	        case TelephonyManager.NETWORK_TYPE_HSUPA:
	        case TelephonyManager.NETWORK_TYPE_HSPA:
	        case TelephonyManager.NETWORK_TYPE_EVDO_B:
	        case TelephonyManager.NETWORK_TYPE_EHRPD:
	        case TelephonyManager.NETWORK_TYPE_HSPAP:
	            return NC_3G;
	        case TelephonyManager.NETWORK_TYPE_LTE:
	            return NC_4G;
	        default:
	            return NC_UNKNOWN;
		}
	}
}
