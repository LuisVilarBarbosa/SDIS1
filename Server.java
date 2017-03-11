package SDIS;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        try {
            String remoteObjName = args[0];
            ServerObject serverObj = new ServerObject();
            ServerRMI serverRMI = (ServerRMI) UnicastRemoteObject.exportObject(serverObj, 0);
            Registry r = LocateRegistry.createRegistry(1099);    // default port
            r.rebind(remoteObjName, (Remote) serverRMI);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
