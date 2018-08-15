package com.jparkie.aizoban.utils;

public class DownloadUtils {
    public static final long TIMEOUT = 100;

    public static final int FLAG_FAILED = -200;
    public static final int FLAG_PAUSED = -100;
    public static final int FLAG_PENDING = 0;
    public static final int FLAG_RUNNING = 100;
    public static final int FLAG_COMPLETED = 200;
    public static final int FLAG_CANCELED = 1337;

    private DownloadUtils() {
        throw new AssertionError();
    }
}
