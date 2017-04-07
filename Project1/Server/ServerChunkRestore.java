package Project1.Server;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

/* Generic received message: GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */
/* Generic message to send: CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body> */

public class ServerChunkRestore {
    private static final int maxWaitTime = 400; /* milliseconds */
    public static int chunkSize = 64000;

    public static byte[] requestChunk(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, String fileId, int chunkNo) {
        StringBuilder st = new StringBuilder("GETCHUNK ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());

        byte[] data = mDataRecoveryCh.receive();
        Message m = new Message(data);
        return m.getBody();
    }

    public static void chunkProvider(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataRecoveryCh) {
        byte[] data = new byte[chunkSize];
        while (true) {
            try {
                byte[] request1 = mControlCh.receive(); //Restore request
                Message m = new Message(request1);

                //Notification that other server has attended the request first
                byte[] request2 = null;
                for (long waitTime = new Random().nextInt() % maxWaitTime, ini = System.currentTimeMillis(); request2 == null && System.currentTimeMillis() - ini < waitTime; )
                    request2 = mDataRecoveryCh.receive();

                if(request2 != null) {
                    Message m2 = new Message(request2);
                    if (m2.getMessageType().equalsIgnoreCase("CHUNK")) //If the other server attended the same request
                        continue;
                }

                if (m.getMessageType().equalsIgnoreCase("GETCHUNK") && m.getVersion().equalsIgnoreCase(protocolVersion)) { //Else, this server attends the request
                    String fileId = m.getFileId();
                    String chunkNo = m.getChunkNo();

                    StringBuilder path = new StringBuilder(serverId);
                    path.append("/").append(fileId).append("/").append(chunkNo);
                    FileInputStream file = new FileInputStream(path.toString());
                    file.read(data);
                    file.close();

                    StringBuilder headerToSend = new StringBuilder("CHUNK ");
                    headerToSend.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");

                    ByteArrayOutputStream msg = new ByteArrayOutputStream();
                    msg.write(headerToSend.toString().getBytes());
                    msg.write(data);
                    mDataRecoveryCh.send(msg.toByteArray());
                    msg.close();
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
