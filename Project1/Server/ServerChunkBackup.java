package Project1.Server;

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;
import Project1.General.Constants;
import Project1.General.Paths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Random;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {

    public static void putChunk(ServerObject serverObject, String fileId, byte[] data, int replicationDegree, int chunkNumber) {
        if (data == null) {
            System.err.println("PutChunk: data = null not accepted.");
            return;
        }
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataBackupCh = serverObject.getDataBackupChannel();

        StringBuilder headerBuilder = new StringBuilder("PUTCHUNK ");
        headerBuilder.append(serverObject.getProtocolVersion()).append(" ").
                append(serverId).append(" ").
                append(fileId).append(" ").
                append(chunkNumber).append(" ").
                append(replicationDegree).append("\r\n\r\n");

        String header = headerBuilder.toString();
        byte[] headerBytes = header.getBytes();
        byte[] chunk = new byte[headerBytes.length + data.length];
        System.arraycopy(headerBytes, 0, chunk, 0, headerBytes.length);
        System.arraycopy(data, 0, chunk, headerBytes.length, data.length);

        //Send the chunk
        mDataBackupCh.send(chunk);
        System.out.println("Sent: " + header);

        //Wait in the control channel for STORED messages
        // - Count number of STORED's received in 1 sec
        // - IF not enough THEN retransmits 2x waiting_time(1 sec) (after 5 times -> error)
        HashSet<String> storedConfirmations = new HashSet<String>();
        int waitTime = Constants.maxWaitTime;
        int retries = 0;
        long lastTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (retries < 5) {
            while (elapsedTime < waitTime) {
                try {
                    byte[] msg = mControlCh.receive(waitTime);
                    if (msg != null) {
                        Message m = new Message(msg);
                        if (m.getMessageType().equalsIgnoreCase("STORED") &&
                                Integer.parseInt(m.getSenderId()) != serverId &&
                                m.getVersion().equalsIgnoreCase(protocolVersion) &&
                                m.getFileId().equals(fileId) &&
                                Integer.parseInt(m.getChunkNo()) == chunkNumber) {
                            System.out.println("Received: " + m.getHeader());
                            storedConfirmations.add(m.getHeader());
                        }
                    }
                } catch (SocketException e) {
                    break;
                }
                elapsedTime += System.currentTimeMillis() - lastTime;
                lastTime = System.currentTimeMillis();
            }
            if (storedConfirmations.size() >= replicationDegree) {
                break;
            } else {
                waitTime = waitTime * 2;
                retries++;
            }
        }

        if (retries >= 5)
            System.out.println("Cannot backup '" + fileId + "' - chunk no. " + chunkNumber + ": Number of retries exceeded");

        FileChunkData chunkData = new FileChunkData(chunkNumber, data.length, storedConfirmations.size());
        serverObject.getDb().getBackedUpFileData(fileId).addOrUpdateFileChunkData(chunkData);
    }

    public static void storeChunk(ServerObject serverObject) {
        //Receive chunk
        //Wait a random delay, while checking the replication degree
        //After random delay (0 - 400 ms)
        // - Create file
        // - Write content to file
        // - Send STORED confirmation
        // - Update peer's database

        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataBackupCh = serverObject.getDataBackupChannel();
        ServerDatabase db = serverObject.getDb();

        while (true) {
            Message m = new Message(mDataBackupCh.receive());
            System.out.println("Received: " + m.getHeader());

            if (m.getMessageType().equalsIgnoreCase("PUTCHUNK") && m.getVersion().equalsIgnoreCase(protocolVersion) && Integer.parseInt(m.getSenderId()) != serverId) {
                int delay = new Random().nextInt(Constants.maxDelayTime);
                long delayEnding = System.currentTimeMillis() + delay;
                HashSet<String> receivedMessages = new HashSet<>();


                //During delay time, checks how many replicas of the chunk have been stored in other peers
                while (System.currentTimeMillis() < delayEnding) {
                    try {
                        byte[] msg = mControlCh.receive(delay);
                        Message feedbackMessage;
                        if (msg != null) {
                            feedbackMessage = new Message(msg);
                            if (feedbackMessage.getMessageType().equalsIgnoreCase("STORED") &&
                                    feedbackMessage.getVersion().equalsIgnoreCase(protocolVersion) &&
                                    Integer.parseInt(feedbackMessage.getSenderId()) != serverId &&
                                    feedbackMessage.getFileId().equals(m.getFileId()) &&
                                    feedbackMessage.getChunkNo().equalsIgnoreCase(m.getChunkNo()) &&
                                    !receivedMessages.contains(feedbackMessage.getHeader())) {
                                System.out.println("Received: " + feedbackMessage.getHeader());
                                receivedMessages.add(feedbackMessage.getHeader());
                            }
                        }
                    } catch (SocketException e) {
                        break;
                    }
                }
                int actualReplicationDegree = receivedMessages.size();
                //Creates and writes content to file. In enhanced protocols, this only happens if replicationDegree is not satisfied
                if (serverObject.getProtocolVersion().equals("1.0") || actualReplicationDegree < Integer.parseInt(m.getReplicationDeg())) {
                    try {
                        new File(Paths.getFolderPath(serverId, m.getFileId())).mkdirs();
                        String filePath = Paths.getChunkPath(serverId, m.getFileId(), Integer.parseInt(m.getChunkNo()));
                        FileOutputStream fileStream = new FileOutputStream(filePath);
                        fileStream.write(m.getBody());
                        fileStream.close();

                        //Creates and sends STORED confirmation message
                        StringBuilder headerBuilder = new StringBuilder("STORED ");
                        headerBuilder.append(protocolVersion).append(" ").
                                append(serverId).append(" ").
                                append(m.getFileId()).append(" ").
                                append(m.getChunkNo()).append("\r\n\r\n");

                        String header = headerBuilder.toString();
                        mControlCh.send(header.getBytes());
                        System.out.println("Sent: " + header);

                        //Put fileInfo in the database
                        if (serverObject.getProtocolVersion().equals("1.0") || actualReplicationDegree < Integer.parseInt(m.getReplicationDeg())) {
                            FileChunkData chunkData = new FileChunkData(
                                    Integer.parseInt(m.getChunkNo()),
                                    m.getBody().length,
                                    actualReplicationDegree);
                            DBFileData dbFileData = db.getStoredFileData(m.getFileId());
                            if (dbFileData == null) {
                                db.addOrUpdateStoredFileData(m.getFileId(), Integer.parseInt(m.getReplicationDeg()));
                                dbFileData = db.getStoredFileData(m.getFileId());
                            }
                            dbFileData.addOrUpdateFileChunkData(chunkData);
                        }
                    } catch (IOException e) {
                        System.err.println("Unable to write the received chunk to a file.");
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
    }

}
