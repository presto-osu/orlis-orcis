package co.loubo.icicle;

import android.annotation.SuppressLint;

import java.util.ArrayList;

public class Constants {

	public static final int	MsgEXIT		= 1000;
	public static final int MsgGetNode = 1001;
	public static final int MsgGetSSKeypair = 1002;
	public static final int MsgGetPersistentRequests = 1003;
	public static final int MsgGetPeers = 1004;
	public static final int MsgFileUpload = 1005;
	public static final int MsgAddNoderef = 1006;
    public static final int MsgUpdatePriority = 1007;
	public static final int MsgSendTextFeed = 1008;
	protected static final Object FNconnected = "CONNECTED";
	protected static final Object FNbackedoff = "BACKED OFF";
	public static final int Activity_File_Upload = 20;
	public static final int Activity_Settings = 21;
	public static final int Activity_Reference = 22;
    public static final int Activity_Messages = 23;
	protected static final String BROADCAST_UPDATE_STATUS = "co.loubo.icicle.update_status";
	protected static final String BROADCAST_UPDATE_DOWNLOADS = "co.loubo.icicle.update_downloads";
	protected static final String BROADCAST_UPDATE_UPLOADS = "co.loubo.icicle.update_uploads";
	protected static final String BROADCAST_UPDATE_PEERS = "co.loubo.icicle.update_peers";
    public static final String LOCAL_NODE_SELECTED = "co.loubo.icicle.local_node_selected";
	public static final String IS_CONNECTED = "isConnected";
    public static final String HAS_LOCAL_NODES = "has_local_nodes";
	public static final String STATUS = "status";
	public static final String DOWNLOADS = "downloads";
	public static final String UPLOADS = "uploads";
	public static final String PEERS = "peers";
	public static final String UPLOAD_DIRS = "uploadDirs";
	public static final String PREF_LOCAL_NODES = "localNodes";
    public static final String PREF_FRIEND_NODES = "friendNodes";
	public static final String PREF_ACTIVE_LOCAL_NODE = "activeLocalNode";
	public static final String PREF_UPLOAD_KEY = "uploadKey";
	public static final String PREF_REFRESH_RATE = "refresh_rate";
	public static final String PREF_WIFI_ONLY = "wifiOnly";
	protected static final int DEFAULT_FCP_PORT = 9481;
	public static final String PREF_DEVICE_ID = "deviceID";
	public static final String KEY_TYPE_CHK = "CHK@";
	public static final String KEY_TYPE_SSK = "SSK@";
	public static final String KEY_TYPE_DEFAULT = KEY_TYPE_SSK;
    public static final String DEFAULT_TRUST = "NORMAL";
    public static final String DEFAULT_VISIBILITY = "NO";
    public static final String CHECKED_ITEM = "CheckedItem";
    public static final String SELECTED_URI = "SelectedUri";
    public static final String SELF = "Me";
    public static String ToastMessage = "ToastMessage";
	public static String MSGRecipientName = "RecipientName";
	public static String MSGRecipientIdentity = "RecipientIdentity";
	
	public static int numberOfTabs = 4;
    public static final int TAB_STATUS = 0;
    public static final int TAB_DOWNLOADS = 1;
    public static final int TAB_UPLOADS = 2;
    public static final int TAB_PEERS = 3;
	public static int debounceInterval = 500;

    public static ArrayList<String> TrustValues = new ArrayList<String>(){{add("HIGH");add("NORMAL");add("LOW");}};
    public static ArrayList<String> VisibilityValues = new ArrayList<String>(){{add("YES");add("NAME_ONLY");add("NO");}};
	
	//http://stackoverflow.com/a/3758880
	@SuppressLint("DefaultLocale")
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	
}
