package SDIS;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        if (args.length != 4 && args.length != 5) {
            System.out.println("Usage: java Client <host_name> <remote_object_name> <oper> <platenumber> [ownername]");
            return;
        }

        //Verify oper
        String oper = args[2];
        if ((!oper.equalsIgnoreCase("register") && args.length == 5) ||
                (!oper.equalsIgnoreCase("lookup") && args.length == 4)) {
            System.out.println("Operation should be [register | lookup] with [5 | 4] arguments");
            return;
        }

        String hostName = args[0];
        String remoteObjName = args[1];
        String plateNumber = args[3];
        String ownerName = null;
        if (args.length >= 5)
            ownerName = args[4];

        // Build majority of output response
        StringBuilder response = new StringBuilder();
        for (int i = 2; i < args.length; i++)
            response.append(args[i]).append(" ");
        response.append(": ");

        try {
            Registry r = LocateRegistry.getRegistry(hostName);
            ServerRMI serverRMI = (ServerRMI) r.lookup(remoteObjName);

            if (oper.equals("register"))
                response.append(serverRMI.register(plateNumber, ownerName));
            else if (oper.equals("lookup"))
                response.append(serverRMI.lookup(plateNumber));
            else
                response.append("ERROR");
        } catch (AccessException e) {
            response.append("ERROR");
            e.printStackTrace();
        } catch (RemoteException e) {
            response.append("ERROR");
            e.printStackTrace();
            System.err.println("Verify if the hostname is correct.");
        } catch (NotBoundException e) {
            response.append("ERROR");
            e.printStackTrace();
            System.err.println("Verify if the remote object name is correct.");
        }

        System.out.println(response);
    }

}
