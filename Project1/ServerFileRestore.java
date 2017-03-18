import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ServerFileRestore {

    public static byte[] restore(int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, String fileId) {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        try {
            byte[] data;
            int chunkNo = 1;

            do {
                data = ServerChunkRestore.requestChunk(serverId, mControlCh, mDataRecoveryCh, fileId, chunkNo);
                file.write(data);
            } while (data.length == ServerChunkRestore.chunkSize);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Probably the file does not exist.");
        }

        return file.toByteArray();
    }
}
