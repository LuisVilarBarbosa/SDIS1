package Project1.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerFileRestore {

    public static void restore(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataRecoveryCh, String filePath, String fileId) {
        File file = new File(filePath);
        if(file.mkdirs())
            System.out.println("Restore: '" + filePath + "' directory successfully created.");
        else
            System.out.println("Restore: error creating the directory '" + filePath + "'");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] data;
            int chunkNo = 0;

            do {
                data = ServerChunkRestore.requestChunk(protocolVersion, serverId, mControlCh, mDataRecoveryCh, fileId, chunkNo);
                fileOutputStream.write(data);
                chunkNo++;
            } while (data.length == ServerChunkRestore.chunkSize);

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Probably the file does not exist.");
        }
    }
}
