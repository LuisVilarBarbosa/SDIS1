package Project1.Server;

import Project1.Database.DBFileData;
import Project1.Database.ServerDatabase;
import Project1.General.Paths;
import Project1.General.Constants;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Random;

/* Generic received message: GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */
/* Generic message to send: CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body> */

public class ServerChunkRestore {

    public static byte[] requestChunk(ServerObject serverObject, String fileId, int chunkNo) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataRecoveryCh = serverObject.getDataRecoveryChannel();

        StringBuilder st = new StringBuilder("GETCHUNK ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());

        try {
            byte[] data = mDataRecoveryCh.receive(Constants.maxWaitTime);
            if(data == null)
                return null;
            Message m = new Message(data);
            return m.getBody();
        } catch (SocketException e) {
            return null;
        }
    }

    public static void chunkProvider(ServerObject serverObject) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataRecoveryCh = serverObject.getDataRecoveryChannel();
        ServerDatabase db = serverObject.getDb();

        byte[] data = new byte[Constants.maxChunkSize];
        while (true) {
            try {
                byte[] request1 = mControlCh.receive(); //Restore request
                Message m = new Message(request1);

                //Notification that other server has attended the request first
                byte[] request2 = mDataRecoveryCh.receive(new Random().nextInt(Constants.maxDelayTime));

                if(request2 != null) {
                    Message m2 = new Message(request2);
                    if (m2.getMessageType().equalsIgnoreCase("CHUNK") &&
                            m2.getFileId().equals(m.getFileId()) &&
                            m2.getChunkNo().equalsIgnoreCase(m.getChunkNo())) //If the other server attended the same request
                        continue;
                }

                if (m.getMessageType().equalsIgnoreCase("GETCHUNK") && m.getVersion().equalsIgnoreCase(protocolVersion)) { //This server attends the request
                    String fileId = m.getFileId();
                    int chunkNo = Integer.parseInt(m.getChunkNo());

                    DBFileData dbFileData = db.getStoredFileData(fileId);
                    if(dbFileData != null && dbFileData.getFileChunkData(chunkNo) != null) {    // if this server stored the chunk
                        String path = Paths.getChunkPath(serverId, fileId, chunkNo);
                        FileInputStream file = new FileInputStream(path);
                        int bytesRead = file.read(data);
                        file.close();

                        StringBuilder headerToSend = new StringBuilder("CHUNK ");
                        headerToSend.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");

                        ByteArrayOutputStream msg = new ByteArrayOutputStream();
                        msg.write(headerToSend.toString().getBytes());
                        msg.write(data, 0, bytesRead);
                        mDataRecoveryCh.send(msg.toByteArray());
                        msg.close();
                    }
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
