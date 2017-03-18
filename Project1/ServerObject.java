import java.rmi.RemoteException;

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
        return ServerFileRestore.restore(serverId, mControlCh, mDataRecoveryCh, db, filename);
    }

    public void delete(String filename) throws RemoteException {

    }
}
