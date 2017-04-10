package Project1.Server;

import Project1.Database.ServerDatabase;
import Project1.General.Paths;

import java.io.*;
import java.util.*;

/* Generic received message: DELETE <Version> <SenderId> <FileId> <CRLF><CRLF> */

public class ServerFileDeletion {
    // All of this variables are only used by the enhanced version
    private static HashMap<String,Long> someOldMessages = new HashMap<>();
    private static final int sendPeriod = 60000;  /* milliseconds */
    private static final int messageExpirationTime = 15 * sendPeriod;

    public static void requestDeletion(ServerObject serverObject, String fileId) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        serverObject.getDb().removeBackedUpFile(fileId);

        StringBuilder st = new StringBuilder("DELETE ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append("\r\n\r\n");
        String msg = st.toString();
        mControlCh.send(msg.getBytes());
        System.out.println("Sent: " + msg);

        if(!serverObject.getProtocolVersion().equalsIgnoreCase("1.0"))
            someOldMessages.put(msg, System.currentTimeMillis());
    }

    public static void fileChunksDeleter(ServerObject serverObject) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        ServerDatabase db = serverObject.getDb();

        while (true) {
                byte[] request = mControlCh.receive();
                Message m = new Message(request);

                if (m.getMessageType().equalsIgnoreCase("DELETE") && m.getVersion().equalsIgnoreCase(protocolVersion) && Integer.parseInt(m.getSenderId()) != serverId) {
                    System.out.println("Received: " + m.getHeader());
                    String fileId = m.getFileId();

                    String path = Paths.getFolderPath(serverId, fileId);

                    if(!deleteDirectory(new File(path)))
                        System.err.println("Unable to delete the file '" + fileId + "'. Due to this, maybe there are inconsistencies in its folder (some chunks deleted and others not).");

                    db.removeStoredFile(fileId);
                }
        }
    }

    // for enhanced version
    public static void messagesForwarder(ServerObject serverObject) {
        Multicast mControlChannel = serverObject.getControlChannel();

        while(true) {
            long currentTime = System.currentTimeMillis();
            for (String msg : someOldMessages.keySet()) {
                mControlChannel.send(msg.getBytes());
                System.out.println("Sent: " + msg);
                if (someOldMessages.get(msg) < currentTime - messageExpirationTime)
                    someOldMessages.remove(msg);
            }

            try {
                Thread.sleep(sendPeriod);
            } catch (InterruptedException e) {}
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
