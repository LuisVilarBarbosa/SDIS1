package Project1.Server;

import Project1.General.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerFileRestore {

    public static void restore(ServerObject serverObject, String filePath, String fileId) {

        String folderPath = null;
        for(int i = filePath.length() - 1; i >= 0; i--)
            if(filePath.charAt(i) == '/' || filePath.charAt(i) == '\\') {
                folderPath = filePath.substring(0, i);
                break;
            }

        File file = new File(folderPath);
        if(!file.exists()) {
            if (file.mkdirs())
                System.out.println("Restore: '" + folderPath + "' directory successfully created.");
            else
                System.out.println("Restore: error creating the directory '" + folderPath + "'");
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] data;
            int chunkNo = 0;

            do {
                data = ServerChunkRestore.requestChunk(serverObject, fileId, chunkNo);
                fileOutputStream.write(data);
                chunkNo++;
            } while (data.length == Constants.maxChunkSize);

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
