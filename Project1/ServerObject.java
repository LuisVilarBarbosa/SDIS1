import java.io.File;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public void backup(String filename, byte[] data, long size, int replicationDegree) throws RemoteException {
    	//TODO (EDIT1:BYTE JA GUARDA OS DADOS) criar uma estrutura de dados que guarde tamb√©m o tamanho dos dados, em vez de byte[] data
    	
    	//Add file info to database table
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date(); //Actual timestamp
    	dateFormat.format(date);
    	
    	this.db.addFileAndDate(filename, date.toString());
    	String fileId = calculateFileId(filename);
    	
    	ServerFileBackup.backup(this, fileId, replicationDegree, data);
    	//Responsavel por
    	// - Chamar o objeto que vai tratar da divis„o do ficheiro/envio
    	// - Guardar a info do objeto na base de dados
    }

    public byte[] restore(String filename) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filename);
        return ServerFileRestore.restore(protocolVersion, serverId, mControlCh, mDataRecoveryCh, fileId);
    }

    public void delete(String filename) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filename);
        ServerFileDeletion.requestDeletion(protocolVersion, serverId, mControlCh, fileId);
    }

    private String calculateFileId(String filename) {
        String fileId = null;
        try {
            ArrayList<String> dates = db.getDates(filename);
            fileId = SHA256.SHA256(filename + dates.get(dates.size() - 1));
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
}
