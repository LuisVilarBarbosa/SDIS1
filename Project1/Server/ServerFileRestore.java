package Project1.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ServerFileRestore {

    public static byte[] restore(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, String fileId) {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        try {
            byte[] data;
            int chunkNo = 0;

            do {
                data = ServerChunkRestore.requestChunk(protocolVersion, serverId, mControlCh, mDataRecoveryCh, fileId, chunkNo);
                file.write(data); //TODO Est� a acumular o ficheiro todo na stream. Modificar para mudar logo
                chunkNo++;
            } while (data.length == ServerChunkRestore.chunkSize);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Probably the file does not exist.");
        }

        return file.toByteArray();
    }
}
