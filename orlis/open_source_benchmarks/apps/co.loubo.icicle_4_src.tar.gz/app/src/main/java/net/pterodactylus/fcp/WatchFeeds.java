package net.pterodactylus.fcp;

/**
 * The “WatchFeeds” messages enables clients to watch for N2N messages. This code was taken from
 * net.pterodactylus.fcp.WatchGlobal and modified.
 *
 */
public class WatchFeeds extends FcpMessage {

    /**
     * Enables or disables watching for N2N messages.
     *
     * @param enabled
     *            <code>true</code> to watch for N2N messages, <code>false</code> to disable watching for N2N messages
     */
    public WatchFeeds(boolean enabled) {
        super("WatchFeeds");
        setField("Enabled", String.valueOf(enabled));
    }

}
