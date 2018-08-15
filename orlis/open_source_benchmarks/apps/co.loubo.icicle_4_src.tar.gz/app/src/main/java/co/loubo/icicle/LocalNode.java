package co.loubo.icicle;

import java.io.Serializable;

public class LocalNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String address;
	private int port;
    private String nodeReference;
    private String encodedNodeReference;
	
	public LocalNode(){
		this("","",0);
	}
	
	public LocalNode(String name, String address, int port){
		this.name = name;
		this.address = address;
		this.port = port;
        this.nodeReference = "";
        this.encodedNodeReference = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

    public String getNodeReference() {
        return nodeReference;
    }

    public void setNodeReference(String nodeReference) {
        this.nodeReference = nodeReference;
    }

    public String getEncodedNodeReference() {
        return encodedNodeReference;
    }

    public void setEncodedNodeReference(String encodedNodeReference) {
        this.encodedNodeReference = encodedNodeReference;
    }

    public String toString()
	    {
	        return "[LocalNode: name=" + name + 
	            " address=" + address +
	            " port=" + port +
	            "]";
	    }    
	
}
