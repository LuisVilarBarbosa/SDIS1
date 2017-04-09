package Project1.Client;

import Project1.Server.ServerRMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Client {

    public static void main(String args[]) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Client <peer_ap> <sub_protocol> <opnd_1> [<opnd_2>]");
            return;
        }

        String peerAccessPoint = args[0];
        String subProtocol = args[1];
        if(!checkArguments(subProtocol, args)) {
        	if(subProtocol.equalsIgnoreCase("backup")){
        		System.out.println("Client <peer_ap> BACKUP <file_path> <replication_degree>");
        	} else if(subProtocol.equalsIgnoreCase("restore")) {
        		System.out.println("Client <peer_ap> RESTORE <file_path>");
        	} else if(subProtocol.equalsIgnoreCase("delete")) {
        		System.out.println("Client <peer_ap> DELETE <file_path>");
        	} else if(subProtocol.equalsIgnoreCase("reclaim")) {
        		System.out.println("Client <peer_ap> RECLAIM <mem_space>");
        	} else if(subProtocol.equalsIgnoreCase("state")) {
        		System.out.println("Client <peer_ap> STATE");
        	} else {
        		System.out.println("INVALID ARGUMENTS");
        		return;
        	}
        }
        String opnd1 = null;
        if (args.length >= 3)
            opnd1 = args[2];
        String opnd2 = null;
        if (args.length >= 4)
            opnd2 = args[3];
        StringTokenizer st = new StringTokenizer(peerAccessPoint, ":");
        String peerHostName = null;
        String peerRemoteObjName;
        if (st.countTokens() == 1)
            peerRemoteObjName = st.nextToken();
        else if (st.countTokens() == 2) {
            peerHostName = st.nextToken();
            peerRemoteObjName = st.nextToken();
        } else
            throw new IllegalArgumentException("Invalid peer access point.");
        

        try {
            Registry r = LocateRegistry.getRegistry(peerHostName, Integer.parseInt(peerRemoteObjName));    // port defined by 'peerRemoteObjName'
            ServerRMI serverRMI = (ServerRMI) r.lookup(peerRemoteObjName);

            if (subProtocol.equalsIgnoreCase("BACKUP"))
                fileBackup(serverRMI, opnd1, opnd2);
            else if (subProtocol.equalsIgnoreCase("RESTORE"))
                fileRestore(serverRMI, opnd1);
            else if (subProtocol.equalsIgnoreCase("DELETE"))
                fileDeletion(serverRMI, opnd1);
            else if (subProtocol.equalsIgnoreCase("RECLAIM"))
                manageServerStorage(serverRMI, opnd1);
            else if (subProtocol.equalsIgnoreCase("STATE"))
                retrieveState(serverRMI);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private static void fileBackup(ServerRMI serverRMI, String filePath, String replicationDegree) throws RemoteException {
        serverRMI.backup(filePath, Integer.parseInt(replicationDegree));
    }

    private static void fileRestore(ServerRMI serverRMI, String filePath) throws RemoteException {
        serverRMI.restore(filePath);
    }

    private static void fileDeletion(ServerRMI serverRMI, String filePath) throws RemoteException {
        serverRMI.delete(filePath);
    }

    private static void manageServerStorage(ServerRMI serverRMI, String numKBytes) throws RemoteException {
        serverRMI.manageStorage(Long.parseLong(numKBytes));
    }

    private static void retrieveState(ServerRMI serverRMI) throws RemoteException {
        System.out.println(serverRMI.state());
    }
    
    private static boolean checkArguments(String subprotocol, String args[]) {
    	if(subprotocol.equalsIgnoreCase("backup"))
    			return args.length == 4;
    	else if(subprotocol.equalsIgnoreCase("restore"))
    			return args.length == 3;
    	else if(subprotocol.equalsIgnoreCase("delete"))
    			return args.length == 3;
    	else if(subprotocol.equalsIgnoreCase("reclaim"))
    			return args.length == 3;
    	else if(subprotocol.equalsIgnoreCase("state"))
    			return args.length == 2;
    	else
    		return false;
    }
}
