package Project1.Server;

import java.io.*;

/* Generic received message: DELETE <Version> <SenderId> <FileId> <CRLF><CRLF> */

public class ServerFileDeletion {
    public static final int chunkSize = 64000;

    public static void requestDeletion(String protocolVersion, int serverId, Multicast mControlCh, String fileId) {
        StringBuilder st = new StringBuilder("DELETE ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());
    }

    public static void fileChunksDeleter(String protocolVersion, int serverId, Multicast mControlCh) {
        byte[] data = new byte[chunkSize];
        while (true) {
                byte[] request = mControlCh.receive();
                Message m = new Message(request);

                if (m.getMessageType().equalsIgnoreCase("DELETE") && m.getVersion().equalsIgnoreCase(protocolVersion)) {
                    String fileId = m.getFileId();

                    StringBuilder path = new StringBuilder(serverId);
                    path.append("/").append(fileId);

                    deleteDirectory(new File(path.toString()));
                }
        }
    }

    private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null != files) {
                for(int i = 0; i < files.length; i++) {
                    if(files[i].isDirectory())
                        deleteDirectory(files[i]);
                    else
                        files[i].delete();
                }
            }
        }
        return directory.delete();
    }
}
