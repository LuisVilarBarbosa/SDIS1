package Project1.Server;

/* Generic received message: REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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
            try {
                byte[] info1 = mControlCh.receive(); //Removed info
                Message m1 = new Message(info1);

                if (m1.getMessageType().equalsIgnoreCase("REMOVED") && m1.getVersion().equalsIgnoreCase(protocolVersion)) {
                    String fileId = m1.getFileId();
                    int chunkNo = Integer.getInteger(m1.getChunkNo());

                    // continue if chunk not stored
                    DBFileData dbFileData = db.getStoredFileData(fileId);
                    if (dbFileData == null)
                        continue;
                    FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                    if (fileChunkData == null)
                        continue;

                    int newRepDeg = fileChunkData.getPerceivedReplicationDegree() - 1;
                    fileChunkData.setPerceivedReplicationDegree(newRepDeg);

                    //Notification that other server has attended the request first
                    byte[] info2 = null;
                    mControlCh.receive(new Random().nextInt() % 400);
                    Message m2 = new Message(info2);
                    if (m2.getMessageType().equalsIgnoreCase("PUTCHUNK") &&
                            m2.getVersion().equalsIgnoreCase(protocolVersion) &&
                            m2.getSenderId().equalsIgnoreCase(m1.getSenderId()) &&
                            m2.getFileId().equalsIgnoreCase(m1.getFileId()) &&
                            m2.getChunkNo().equalsIgnoreCase(m1.getChunkNo()))
                        continue;

                    int desiredRepDeg = dbFileData.getDesiredReplicationDegree();
                    if (newRepDeg < desiredRepDeg) {
                        byte[] data = new byte[64000];
                        StringBuilder path = new StringBuilder(serverId);
                        path.append("/").append(fileId).append("/").append(chunkNo);
                        FileInputStream file = new FileInputStream(path.toString());
                        file.read(data);
                        file.close();

                        ServerChunkBackup.putChunk(, data, chunkNo, desiredRepDeg);
                    }
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
