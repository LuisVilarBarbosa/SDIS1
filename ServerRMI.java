package SDIS;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {

    String register(String plateNumber, String ownerName) throws RemoteException;

    String lookup(String plateNumber) throws RemoteException;
}
