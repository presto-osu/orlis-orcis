package co.loubo.icicle;

import net.pterodactylus.fcp.SimpleProgress;

public class Transfer {
	
	private SimpleProgress progress;
	public long dataLength;
	private int priority;
	
	public void updateProgress(SimpleProgress simpleProgress) {
		this.progress = simpleProgress;
	}
	public SimpleProgress getProgress(){
		return this.progress;
	}
	
	public void updateDataLength(String length){
		this.dataLength = Long.parseLong(length);
	}
	
	public long getDataLength(){
		return this.dataLength;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
}
