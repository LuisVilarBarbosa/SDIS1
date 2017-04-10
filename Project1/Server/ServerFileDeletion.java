package Project1.Server;

import Project1.Database.ServerDatabase;
import Project1.General.Paths;

import java.io.*;

/* Generic received message: DELETE <Version> <SenderId> <FileId> <CRLF><CRLF> */

public class ServerFileDeletion {

    public static void requestDeletion(ServerObject serverObject, String fileId) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        serverObject.getDb().removeBackedUpFile(fileId);

        StringBuilder st = new StringBuilder("DELETE ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());
    }

    public static void fileChunksDeleter(ServerObject serverObject) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        ServerDatabase db = serverObject.getDb();

        while (true) {
                byte[] request = mControlCh.receive();
                Message m = new Message(request);

                if (m.getMessageType().equalsIgnoreCase("DELETE") && m.getVersion().equalsIgnoreCase(protocolVersion)) {
                    String fileId = m.getFileId();

                    String path = Paths.getFolderPath(serverId, fileId);

                    if(!deleteDirectory(new File(path)))
                        System.err.println("Unable to delete the file '" + fileId + "'. Due to this, maybe there are inconsistencies in its folder (some chunks deleted and others not).");

                    db.removeStoredFile(fileId);
                }
        }
    }

    private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isDirectory())
                        deleteDirectory(file);
                    else
                        file.delete();
                }
            }
            return directory.delete();
        }
        return true;
    }
}
