import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ServerFileRestore {

    public static byte[] restore(int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, ServerDatabase db, String filename) {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        try {
            ArrayList<String> dates = db.getDates(filename);
            String fileId = SHA256.SHA256(filename + dates.get(dates.size() - 1));
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
