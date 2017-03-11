package SDIS;

import java.rmi.Remote;

public interface ServerRMI extends Remote {
	String register(String plateNumber, String ownerName);
	String lookup(String plateNumber);
}
