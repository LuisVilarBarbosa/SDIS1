import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServerObject implements ServerRMI {
    private int serverId;
    private Multicast mControlCh;
    private Multicast mDataBackupCh;
    private Multicast mDataRecoveryCh;
    private ServerDatabase db;

    public ServerObject(int serverId, Multicast mControlCh, Multicast mDataBackupCh, Multicast mDataRecoveryCh, ServerDatabase db) {
        this.serverId = serverId;
        this.mControlCh = mControlCh;
        this.mDataBackupCh = mDataBackupCh;
        this.mDataRecoveryCh = mDataRecoveryCh;
        this.db = db;
    }

    public void backup(String filename, String lastModifiedDate, byte[] data) throws RemoteException {

    }

    public byte[] restore(String filename) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filename);
        return ServerFileRestore.restore(serverId, mControlCh, mDataRecoveryCh, fileId);
    }

    public void delete(String filename) throws RemoteException {
        // Concurrency is missing
        String fileId = calculateFileId(filename);
        ServerFileDeletion.requestDeletion(serverId, mControlCh, fileId);
    }

    private String calculateFileId(String filename) {
        String fileId = null;
        try {
            ArrayList<String> dates = db.getDates(filename);
            fileId = SHA256.SHA256(filename + dates.get(dates.size() - 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileId;
    }
}
