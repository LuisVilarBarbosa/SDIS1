package Project1.Server;

import Project1.Database.ServerDatabase;
import Project1.General.SHA256;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    //TODO backup receives filepath, not filename. Change the things needed
    public void backup(String filePath, int replicationDegree) throws RemoteException, FileNotFoundException, IOException {
    	//Responsavel por
    	// - Chamar o objeto que vai tratar da divis�o do ficheiro/envio
    	// - Guardar a info do objeto na base de dados

    	//Add file info to database table
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date(); //Actual timestamp
    	dateFormat.format(date);
    	
    	//TODO Como adiciono o ficheiro � DB? N�o percebi as fun��es
    	this.db.addFileAndDate(filePath, date.toString());
    	String fileId = calculateFileId(filePath);
    	
    	ServerFileBackup.backup(this, filePath, fileId, replicationDegree);
    }

    public void restore(String filePath) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filePath);
        ServerFileRestore.restore(this, filePath, fileId);
    }

    public void delete(String filePath) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filePath);
        ServerFileDeletion.requestDeletion(this, fileId);
    }

    public void manageStorage(long newStorageSpace) throws RemoteException {
        ServerSpaceReclaiming.updateStorageSpace(this, newStorageSpace);
    }

    public String state() throws RemoteException {
        // Concurrency is missing
        return ServerState.retrieveState(this);
    }

    private String calculateFileId(String filePath) {
        String fileId = null;
        String date = db.getDBFileData(filePath).getLastModificationDate();
        try {
            fileId = SHA256.SHA256(filePath + date);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return fileId;
    }

    public String getProtocolVersion(){ return protocolVersion; }
    public int getServerId() { return serverId; }
    public Multicast getControlChannel() { return mControlCh; }
    public Multicast getDataBackupChannel() { return mDataBackupCh; }
    public Multicast getDataRecoveryChannel() { return mDataRecoveryCh; }
    public ServerDatabase getDb() { return db; }
}
