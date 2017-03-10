package SDIS;

import java.rmi.Remote;

public interface ServerRMI extends Remote {
	String register(String ownerName, String plateNumber);
	String lookup(String plateNumber);
}
