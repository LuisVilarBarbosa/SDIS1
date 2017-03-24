import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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

    public void backup(String filename, byte[] data) throws RemoteException {

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
}
