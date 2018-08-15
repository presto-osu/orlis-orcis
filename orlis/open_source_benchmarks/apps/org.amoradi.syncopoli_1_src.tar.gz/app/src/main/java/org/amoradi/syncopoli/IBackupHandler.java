package org.amoradi.syncopoli;

import java.util.List;

public interface IBackupHandler {
    void addBackup(BackupItem bi);
    int runBackup(BackupItem bi);
    void showLog(BackupItem bi);
    void updateBackupTimestamp(BackupItem bi);
    void updateBackupList();
    List<BackupItem> getBackups();
    void syncBackups();
}
