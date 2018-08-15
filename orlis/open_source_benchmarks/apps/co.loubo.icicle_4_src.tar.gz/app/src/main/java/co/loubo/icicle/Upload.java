package co.loubo.icicle;

import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.URIGenerated;

public class Upload extends Transfer{
	
	private PersistentPut put;
	private PutSuccessful putSuccessful;
	private PutFetchable putFetchable;
	private URIGenerated uriGenerated;
	private StartedCompression startedCompression;
	private FinishedCompression finishedCompression;
	private PutFailed putFailed;
	
	public Upload(PersistentPut put) {
		this.setPersistentPut(put);
	}

	public PersistentPut getPersistentPut() {
		return put;
	}

	public void setPersistentPut(PersistentPut put) {
		this.put = put;
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
