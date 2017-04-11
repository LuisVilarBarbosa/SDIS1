package Project1.Server;

import Project1.Database.ServerDatabase;
import Project1.General.SHA256;

import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerObject implements ServerRMI {
    private String protocolVersion;
    private int serverId;
    private Multicast mControlCh;
    private Multicast mDataBackupCh;
    private Multicast mDataRecoveryCh;
    private ServerDatabase db;

    public ServerObject(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataBackupCh, Multicast mDataRecoveryCh, ServerDatabase db) {
        this.protocolVersion = protocolVersion;
        this.serverId = serverId;
        this.mControlCh = mControlCh;
        this.mDataBackupCh = mDataBackupCh;
        this.mDataRecoveryCh = mDataRecoveryCh;
        this.db = db;
    }

    public void backup(String filePath, int replicationDegree) throws RemoteException {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date(); //Actual time
    	dateFormat.format(date);

    	//If file exists, we assume that is outdated
    	if(this.db.getBackedUpFileId(filePath) != null) {
            delete(filePath);
    	}
    	
    	String fileId = calculateFileId(filePath, date.toString());
    	ServerFileBackup.backup(this, filePath, fileId, replicationDegree);
    }

    public void restore(String filePath) throws RemoteException {
        String fileId = db.getBackedUpFileId(filePath);
        if(fileId == null)
            showFileIdError(filePath);
        else
            ServerFileRestore.restore(this, filePath, fileId);
    }

    public void delete(String filePath) throws RemoteException {
        String fileId = db.getBackedUpFileId(filePath);
        if(fileId == null)
            showFileIdError(filePath);
        else
            ServerFileDeletion.requestDeletion(this, fileId);
    }

    public void manageStorage(long newStorageSpace) throws RemoteException {
        ServerSpaceReclaiming.updateStorageSpace(this, newStorageSpace * 1000); // passing KBytes to bytes
    }

    public String state() throws RemoteException {
        return ServerState.retrieveState(this);
    }

    private String calculateFileId(String filePath, String lastModificationDate) {
        String fileId = null;
        try {
            fileId = SHA256.SHA256(filePath + lastModificationDate);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return fileId;
    }

    public String getProtocolVersion(){ return protocolVersion; }
    public int getServerId() { return serverId; }
    public Multicast getControlChannel() { return mControlCh.clone(); }
    public Multicast getDataBackupChannel() { return mDataBackupCh.clone(); }
    public Multicast getDataRecoveryChannel() { return mDataRecoveryCh.clone(); }
    public ServerDatabase getDb() { return db; }

    private void showFileIdError(String filePath) {
        System.out.println("The requested file does not exist: " + filePath);
    }
}
