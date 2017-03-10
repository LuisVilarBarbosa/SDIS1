package SDIS;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: java Server <remote_object_name>");
			return;
		}

		try {
			String remoteObjName = args[0];
			ServerObject serverObj = new ServerObject();
			UnicastRemoteObject.exportObject((Remote) serverObj, 0);
			Registry r = java.rmi.registry.LocateRegistry.getRegistry();
			r.rebind(remoteObjName, (Remote) serverObj);
			while(true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
