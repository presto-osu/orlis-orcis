package co.loubo.icicle;

import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.URIGenerated;

public class UploadDir extends Transfer{
	
	private PersistentPutDir put;	
	private PutSuccessful putSuccessful;
	private PutFetchable putFetchable;
	private URIGenerated uriGenerated;
	private StartedCompression startedCompression;
	private FinishedCompression finishedCompression;
	private PutFailed putFailed;
	
	public UploadDir(PersistentPutDir put) {
		this.setPersistentPutDir(put);
	}

	public PersistentPutDir getPersistentPutDir() {
		return put;
	}

	public void  setPersistentPutDir(PersistentPutDir put) {
		this.put = put;
		this.dataLength = 0;
		int count = put.getFileCount();
		for(int i = 0;i < count;i++){
			this.dataLength += put.getFileDataLength(i);
		}
		this.setPriority(put.getPriority().ordinal());
	}

	public PutSuccessful getPutSuccessful() {
		return putSuccessful;
	}

	public void setPutSuccessful(PutSuccessful putSuccessful) {
		this.putSuccessful = putSuccessful;
	}

	public PutFetchable getPutFetchable() {
		return putFetchable;
	}

	public void setPutFetchable(PutFetchable putFetchable) {
		this.putFetchable = putFetchable;
	}

	public URIGenerated getUriGenerated() {
		return uriGenerated;
	}

	public void setUriGenerated(URIGenerated uriGenerated) {
		this.uriGenerated = uriGenerated;
	}

	public StartedCompression getStartedCompression() {
		return startedCompression;
	}

	public void setStartedCompression(StartedCompression startedCompression) {
		this.startedCompression = startedCompression;
	}

	public FinishedCompression getFinishedCompression() {
		return finishedCompression;
	}

	public void setFinishedCompression(FinishedCompression finishedCompression) {
		this.finishedCompression = finishedCompression;
	}

	public PutFailed getPutFailed() {
		return putFailed;
	}

	public void setPutFailed(PutFailed putFailed) {
		this.putFailed = putFailed;
	}
}
