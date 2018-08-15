package co.loubo.icicle;

import java.io.Serializable;

public class NodeStatus implements Serializable {
	
	private static final long serialVersionUID = -4710710888053633911L;
	private boolean isAdvanced;
	private String version;
	private double recentInputRate;
	private double recentOutputRate;
	private double uptimeSeconds;
	
	public NodeStatus(boolean isAdvanced, String version){
		this.isAdvanced = isAdvanced;
		this.setVersion(version);
	}

	public boolean isAdvanced() {
		return isAdvanced;
	}

	public void setAdvanced(boolean isAdvanced) {
		this.isAdvanced = isAdvanced;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public double getRecentInputRate() {
		return recentInputRate;
	}

	public void setRecentInputRate(double recentInputRate) {
		this.recentInputRate = recentInputRate;
	}

	public double getRecentOutputRate() {
		return recentOutputRate;
	}

	public void setRecentOutputRate(double recentOutputRate) {
		this.recentOutputRate = recentOutputRate;
	}

	public double getUptimeSeconds() {
		return uptimeSeconds;
	}

	public void setUptimeSeconds(double uptimeSeconds) {
		this.uptimeSeconds = uptimeSeconds;
	}
	
	
	
}
