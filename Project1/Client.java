import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;

public class Client {

    public static void main(String args[]) {
        if (args.length != 3 && args.length != 4) {
            System.out.println("Client <peer_ap> <sub_protocol> <opnd_1> [<opnd_2>]");
            return;
        }

        String peerAccessPoint = args[0];
        String subProtocol = args[1];
        String opnd1 = args[2];
        String opnd2 = null;
        if (args.length == 4)
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

            if (args[1].equalsIgnoreCase("RESTORE"))
                serverRMI.restore(opnd1);   /* opnd1 = filename */
        } catch (RemoteException e) {
            System.err.println(e.getMessage());
        } catch (NotBoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
