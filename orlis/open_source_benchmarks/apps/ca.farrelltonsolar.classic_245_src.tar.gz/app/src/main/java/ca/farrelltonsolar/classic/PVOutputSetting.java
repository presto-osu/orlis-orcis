package ca.farrelltonsolar.classic;

/**
 * Created by Me on 5/22/2016.
 */
public class PVOutputSetting {
    final transient Object lock = new Object();
    private String logDate; //date logs from classic were recorded for upload to PVOutput.org
    private String uploadDate; // last date the logs were uploaded to pvoutput.org
    private String SID; // pvoutput system id

    public synchronized String getSID() {
        return SID;
    }

    public synchronized void setSID(String SID) {
        this.SID = SID;
    }

    public synchronized String getPVOutputLogFilename() {
        return logDate;
    }

    public synchronized void setPVOutputLogFilename(String logDate) {
        this.logDate = String.format("PVOutput_%s_%s.log", logDate, SID) ;
    }

    public synchronized void resetPVOutputEntry() {
        String fname = getPVOutputLogFilename();
        if (fname != null && fname.length() > 0) {
            MonitorApplication.getAppContext().deleteFile(fname);
        }
        synchronized (lock) {
            uploadDate = "";
            logDate = "";
        }
    }

    public synchronized String uploadDate() {
        return uploadDate;
    }

    public synchronized void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}
