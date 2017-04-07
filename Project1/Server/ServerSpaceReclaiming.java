package Project1.Server;

/* Generic received message: REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;

import java.io.File;
import java.util.ArrayList;

public class ServerSpaceReclaiming {

    public static void updateStorageSpace(ServerObject serverObject, long newStorageSpace) {
        int serverId = serverObject.getServerId();
        ServerDatabase db = serverObject.getDb();
        ArrayList<String> filesIds = db.getStoredFilesIds();
        long usedStorage = db.getUsedStorage();   // just for efficiency, it could be db.getUsedStorage()

        for (int i = 0; i < filesIds.size() && usedStorage > newStorageSpace; i++) {
            String fileId = filesIds.get(i);
            DBFileData dbFileData = db.getStoredFileData(fileId);
            int numFileChunks = dbFileData.getNumFileChunks();

            for (int chunkNo = 0; chunkNo < numFileChunks && usedStorage > newStorageSpace; chunkNo++) {
                // delete file from the file system
                StringBuilder path = new StringBuilder(serverId);
                path.append("/").append(fileId).append("/").append(chunkNo);
                new File(path.toString()).delete();

                // delete file from the database
                FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                dbFileData.removeFileChunkData(chunkNo);
                usedStorage -= fileChunkData.getSize();

                // send "removed" message to the other peers
                StringBuilder sb = new StringBuilder("REMOVED ");
                sb.append(serverObject.getProtocolVersion()).append(serverObject.getServerId()).append(fileId);
                sb.append(chunkNo).append("\r\n\r\n");
            }
        }
    }

    public static void monitorStorageSpaceChanges(String protocolVersion, int serverId, Multicast mControlCh, ServerDatabase db) {
        while (true) {

        }
    }

}
