package co.loubo.icicle;

import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.PersistentGet;

public class Download extends Transfer {
	
	private PersistentGet get;
	private DataFound dataFound;
	private GetFailed getFailed;
	
	public Download(PersistentGet get) {
		this.setPersistentGet(get);
	}

	public PersistentGet getPersistentGet() {
		return get;
	}

	public void setPersistentGet(PersistentGet get) {
		this.get = get;
		this.setPriority(get.getPriority().ordinal());
	}

	public void setDataFound(DataFound dataFound) {
		this.dataFound = dataFound;
	}
	
	public DataFound getDataFound(){
		return this.dataFound;
	}

	public void setGetFailed(GetFailed getFailed) {
		this.getFailed = getFailed;
	}
	
	public GetFailed getGetFailed(){
		return this.getFailed;
	}

}
