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

        sb.append("\r\nBacked up files:\r\n");
        generatePartialState(db, sb, backedUpFiles, true, false);
        sb.append("\r\nStored files:\r\n");
        generatePartialState(db, sb, storedFiles, false, true);

        sb.append("\r\nStorage capacity: ").append(db.getStorageCapacity() / 1000).append(" KBytes\r\n");
        sb.append("Used storage: ").append(db.getUsedStorage() / 1000).append(" KBytes\r\n");
        return sb.toString();
    }

    private static void generatePartialState(ServerDatabase db, StringBuilder sb, int filesType, boolean appendFilePath, boolean appendSize) {
        ArrayList<String> filesIds;
        if (filesType == backedUpFiles)
            filesIds = db.getBackedUpFilesIds();
        else if (filesType == storedFiles)
            filesIds = db.getStoredFilesIds();
        else {
            sb.append("generatePartialState: Invalid 'filesType'.\r\n");
            return;
        }

        for (int i = 0; i < filesIds.size(); i++) {
            String fileId = filesIds.get(i);
            DBFileData dbFileData;
            if (filesType == backedUpFiles)
                dbFileData = db.getBackedUpFileData(fileId);
            else // if(filesType == storedFiles)
                dbFileData = db.getStoredFileData(fileId);

            if (appendFilePath)
                sb.append("File path: ").append(dbFileData.getFilePath()).append("\r\n");
            sb.append("File id: ").append(dbFileData.getFileId()).append("\r\n");
            sb.append("Desired replication degree: ").append(dbFileData.getDesiredReplicationDegree()).append("\r\n");
            if (appendSize)
                sb.append("Chunks (id, size (KB), perceived replication degree):").append("\r\n");
            else
                sb.append("Chunks (id, perceived replication degree):").append("\r\n");

            ArrayList<Integer> chunksNos = dbFileData.listChunksNos();
            for (int chunkNo : chunksNos) {
                FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                sb.append(fileChunkData.getChunkNo()).append(" ");
                if (appendSize)
                    sb.append(fileChunkData.getSize() / 1000).append(" ");
                sb.append(fileChunkData.getPerceivedReplicationDegree()).append("\r\n");
            }
            sb.append("\r\n");
        }
    }
}
