package Project1.Server;

/* Generic received message: REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;
import Project1.General.Constants;
import Project1.General.Paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ServerSpaceReclaiming {

    public static void updateStorageSpace(ServerObject serverObject, long newStorageSpace) {
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        ServerDatabase db = serverObject.getDb();
        db.setStorageCapacity(newStorageSpace);

        ArrayList<String> filesIds = db.getStoredFilesIds();
        long usedStorage = db.getUsedStorage();   // just for efficiency

        for (int i = 0; i < filesIds.size() && usedStorage > newStorageSpace; i++) {
            String fileId = filesIds.get(i);
            DBFileData dbFileData = db.getStoredFileData(fileId);
            ArrayList<Integer> fileChunksNos = dbFileData.listChunksNos();

            for (int j = 0; j < fileChunksNos.size() && usedStorage > newStorageSpace; j++) {
                int chunkNo = fileChunksNos.get(j);
                // delete file from the file system
                String path = Paths.getChunkPath(serverId, fileId, chunkNo);
                new File(path.toString()).delete();

                // delete file from the database
                FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                dbFileData.removeFileChunkData(chunkNo);
                usedStorage -= fileChunkData.getSize();
                if(db.getStoredFileData(fileId).getNumFileChunks() == 0)
                    db.removeStoredFile(fileId);

                // send "removed" message to the other peers
                StringBuilder sb = new StringBuilder("REMOVED ");
                sb.append(serverObject.getProtocolVersion()).append(serverObject.getServerId()).append(fileId);
                sb.append(chunkNo).append("\r\n\r\n");
                String msg = sb.toString();
                mControlCh.send(msg.getBytes());

                System.out.println("Sent: " + msg);
            }
        }
    }

    public static void monitorStorageSpaceChanges(ServerObject serverObject) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        ServerDatabase db = serverObject.getDb();

        while (true) {
            try {
                byte[] info1 = mControlCh.receive(); //Removed info
                Message m1 = new Message(info1);

                if (m1.getMessageType().equalsIgnoreCase("REMOVED") && m1.getVersion().equalsIgnoreCase(protocolVersion)) {
                    System.out.println("Received: " + m1.getHeader());
                    String fileId = m1.getFileId();
                    int chunkNo = Integer.parseInt(m1.getChunkNo());

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
                    mControlCh.receive(new Random().nextInt() % Constants.maxDelayTime);
                    Message m2 = new Message(info2);
                    System.out.println(m2.getHeader());
                    if (m2.getMessageType().equalsIgnoreCase("PUTCHUNK") &&
                            m2.getVersion().equalsIgnoreCase(protocolVersion) &&
                            m2.getSenderId().equalsIgnoreCase(m1.getSenderId()) &&
                            m2.getFileId().equalsIgnoreCase(m1.getFileId()) &&
                            m2.getChunkNo().equalsIgnoreCase(m1.getChunkNo()))
                        continue;

                    int desiredRepDeg = dbFileData.getDesiredReplicationDegree();
                    if (newRepDeg < desiredRepDeg) {
                        byte[] data = new byte[Constants.maxChunkSize];
                        String path = Paths.getChunkPath(serverId, fileId, chunkNo);
                        FileInputStream file = new FileInputStream(path);
                        file.read(data);
                        file.close();

                        ServerChunkBackup.putChunk(serverObject, fileId, data, desiredRepDeg, chunkNo);
                    }
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

}
