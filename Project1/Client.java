import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Client {

    public static void main(String args[]) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Client <peer_ap> <sub_protocol> <opnd_1> [<opnd_2>]");
            return;
        }

        String peerAccessPoint = args[0];
        String subProtocol = args[1];
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
            Registry r = LocateRegistry.getRegistry(peerHostName);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fileBackup(ServerRMI serverRMI, String filename, String replicationDegree) throws IOException {
    	FileInputStream fileInputStream = new FileInputStream(filename);
    	File file = new File(filename);
    	
    	
    	long fileSize = file.length();
    	
    	byte[] fileData = new byte[fileSize];
    	fileInputStream.read(fileData);
    	serverRMI.backup(filename, fileData);
    	fileInputStream.close();
    }

    private static void fileRestore(ServerRMI serverRMI, String filename) throws IOException {
        byte[] fileData = serverRMI.restore(filename);
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        fileOutputStream.write(fileData);
        fileOutputStream.close();
    }

    private static void fileDeletion(ServerRMI serverRMI, String filename) throws RemoteException {
        serverRMI.delete(filename);
    }

    private static void manageServerStorage(ServerRMI serverRMI, String numKBytes) {

    }

    private static void retrieveState(ServerRMI serverRMI) {

    }
}
