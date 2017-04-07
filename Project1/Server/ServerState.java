package Project1.Server;

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;

import java.util.ArrayList;

public class ServerState {
    private static int backedUpFiles = 1;
    private static int storedFiles = 2;

    public static String retrieveState(ServerObject serverObject) {
        ServerDatabase db = serverObject.getDb();
        StringBuilder sb = new StringBuilder();

        sb.append("Backed up files:\r\n");
        generatePartialState(db, sb, backedUpFiles, false);
        sb.append("\r\nStored files:\r\n");
        generatePartialState(db, sb, storedFiles, true);

        sb.append(db.getStorageCapacity()).append(" KBytes\r\n");
        sb.append(db.getUsedStorage()).append(" KBytes\r\n");
        return sb.toString();
    }

    private static void generatePartialState(ServerDatabase db, StringBuilder sb, int filesType, boolean appendSize) {
        ArrayList<String> filesPaths;
        if (filesType == backedUpFiles)
            filesPaths = db.getBackedUpFilesPaths();
        else if (filesType == storedFiles)
            filesPaths = db.getStoredFilesPaths();
        else {
            sb.append("generatePartialState: Invalid 'filesType'.\r\n");
            return;
        }

        for (int i = 0; i < filesPaths.size(); i++) {
            String filePath = filesPaths.get(i);
            DBFileData dbFileData;
            if (filesType == backedUpFiles)
                dbFileData = db.getBackedUpFileData(filePath);
            else // if(filesType == storedFiles)
                dbFileData = db.getStoredFileData(filePath);

            sb.append("File path: ").append(dbFileData.getFilePath()).append("\r\n");
            sb.append("File id: ").append(dbFileData.getFileId()).append("\r\n");
            sb.append("Desired replication degree: ").append(dbFileData.getDesiredReplicationDegree()).append("\r\n");
            sb.append("Chunks (Id, perceived replication degree):").append("\r\n");

            int numFileChunks = dbFileData.getNumFileChunks();
            for (int chunkNo = 0; chunkNo < numFileChunks; chunkNo++) {
                FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                sb.append(fileChunkData.getChunkNo());
                if (appendSize)
                    sb.append(fileChunkData.getSize());
                sb.append(fileChunkData.getPerceivedReplicationDegree()).append("\r\n");
            }
            sb.append("\r\n");
        }
    }
}
