package Project1.Server;

import Project1.Database.ServerDatabase;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    public static void main(String args[]) {
        if (args.length != 9) {
            System.out.println("Usage: Server <protocol_version> <serverId> <srvc_access_point> " +
                    "<mcast_control_ip> <mcast_control_port> <mcast_data_backup_ip> <mcast_data_backup_port> " +
                    "<mcast_data_recovery_ip> <mcast_data_recovery_port>");
            return;
        }

        String protocolVersion = args[0];
        int serverId = Integer.parseInt(args[1]);
        String accessPoint = args[2];
        String mControlIp = args[3];
        int mControlPort = Integer.parseInt(args[4]);
        String mDataBackupIp = args[5];
        int mDataBackupPort = Integer.parseInt(args[6]);
        String mDataRecoveryIp = args[7];
        int mDataRecoveryPort = Integer.parseInt(args[8]);

        Multicast mControlCh = new Multicast(mControlIp, mControlPort);
        Multicast mDataBackupCh = new Multicast(mDataBackupIp, mDataBackupPort);
        Multicast mDataRecoveryCh = new Multicast(mDataRecoveryIp, mDataRecoveryPort);

        StringTokenizer st = new StringTokenizer(accessPoint, ":");
        String hostName = null;
        String remoteObjName;
        if (st.countTokens() == 1)
            remoteObjName = st.nextToken();
        else if (st.countTokens() == 2) {
            hostName = st.nextToken();
            remoteObjName = st.nextToken();
        } else
            throw new IllegalArgumentException("Invalid access point.");

        ServerDatabase db = new ServerDatabase(serverId);
        try {
            ServerObject serverObj = new ServerObject(protocolVersion, serverId, mControlCh, mDataBackupCh, mDataRecoveryCh, db);
            ServerRMI serverRMI = (ServerRMI) UnicastRemoteObject.exportObject(serverObj, 0);
            Registry r = LocateRegistry.createRegistry(Integer.parseInt(remoteObjName));    // port defined by 'remoteObjName'
        	
	        Timer timer1 = new Timer();
	        TimerTask timerTask1 = new TimerTask() {
	            @Override
	            public void run() {
	                ServerChunkBackup.storeChunk(serverObj);
	            }
	        };
	        timer1.schedule(timerTask1, 0);
	
	        Timer timer2 = new Timer();
	        TimerTask timerTask2 = new TimerTask() {
	            @Override
	            public void run() {
	                ServerChunkRestore.chunkProvider(serverObj);
	            }
	        };
	        timer2.schedule(timerTask2, 0);
	
	        Timer timer3 = new Timer();
	        TimerTask timerTask3 = new TimerTask() {
	            @Override
	            public void run() {
	                ServerFileDeletion.fileChunksDeleter(serverObj);
	            }
	        };
	        timer3.schedule(timerTask3, 0);
	
	        Timer timer4 = new Timer();
	        TimerTask timerTask4 = new TimerTask() {
	            @Override
	            public void run() {
	                ServerSpaceReclaiming.monitorStorageSpaceChanges(serverObj);
	            }
	        };
	        timer4.schedule(timerTask4, 0);

            r.rebind(remoteObjName, serverRMI);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
