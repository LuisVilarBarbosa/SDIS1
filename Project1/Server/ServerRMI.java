package Project1.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {

    void backup(String filename, byte[] data, long size, int replicationDegree) throws RemoteException;

    byte[] restore(String filename) throws RemoteException;

    void delete(String filename) throws RemoteException;
}
