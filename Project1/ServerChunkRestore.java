/* Generic received message: GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */
/* Generic message to send: CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body> */

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ServerChunkRestore {
    public static int chunkSize = 64000;

    public static byte[] requestChunk(int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, String fileId, int chunkNo) {
        StringBuilder st = new StringBuilder("GETCHUNK 1.0 ");
        st.append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());

        //Message m = mDataRecoveryCh.receive();
        //return m.getBody();
        return new byte[0];
    }

    public static void chunkProvider(int serverId, Multicast mControlCh, Multicast mDataRecoveryCh) {
        byte[] data = new byte[chunkSize];
        while (true) {
            try {
                byte[] request1 = mControlCh.receive();
                Message m = new Message(request1);

                byte[] request2 = mControlCh.receive();
                Message m2 = new Message(request2);

                if (m2.getMessageType().equalsIgnoreCase("CHUNK"))
                    continue;

                if (m.getMessageType().equalsIgnoreCase("GETCHUNK")) {
                    String fileId = m.getFileId();
                    String chunkNo = m.getChunkNo();

                    StringBuilder path = new StringBuilder(serverId);
                    path.append("/").append(fileId).append("/").append(chunkNo);
                    FileInputStream file = new FileInputStream(path.toString());
                    file.read(data);
                    file.close();

                    StringBuilder headerToSend = new StringBuilder("CHUNK 1.0 ");
                    headerToSend.append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");

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
