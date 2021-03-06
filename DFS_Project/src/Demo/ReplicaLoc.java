package Demo;
import java.io.Serializable;


public class ReplicaLoc implements Serializable {
	
	public static final long serialVersionUID = -4113307750760738108L;
	
	private String address;
	private int id;
	private boolean isAlive;

	private int port;
	
	public ReplicaLoc(int id, String address,int port,boolean isAlive) {
		this.id=id;
		this.address = address;
		this.port = port;
		this.isAlive = isAlive;
	}
	
	boolean isAlive() {
		return isAlive;
	}
	
	int getId() {
		return id;
	}
	int getPort() {
		return port;
	}
	
	void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
	
	String getAddress() {
		return address;
	}
}
