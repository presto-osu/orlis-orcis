package co.loubo.icicle;

import java.io.Serializable;

public class FriendNode implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String name;
    private String identity;
    private String trust;
    private String visibility;
    private String nodeReference;

	public FriendNode(String name, String identity, String trust, String visibility, String nodeReference){
		this.name = name;
        this.identity = identity;
        this.trust = trust;
        this.visibility = visibility;
        this.nodeReference = nodeReference;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getTrust() {
        return trust;
    }

    public void setTrust(String trust) {
        this.trust = trust;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getNodeReference() {
        return nodeReference;
    }

    public String toString()
	    {
	        return "[FriendNode: name=" + name +
	            " trust=" + trust +
	            " visibility=" + visibility +
	            "]";
	    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof FriendNode))return false;
        FriendNode otherFriendNode = (FriendNode)other;
        return otherFriendNode.getIdentity().equals(this.identity);
    }
}
