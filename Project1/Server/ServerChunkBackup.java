package Project1.Server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

import Project1.Database.DBFileData;
import Project1.Database.FileChunkData;
import Project1.Database.ServerDatabase;
import Project1.General.Paths;
import Project1.General.Constants;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {
	
	public static void putChunk(ServerObject serverObject, String fileId, byte[] data, int replicationDegree, int chunkNumber) {
		if(data == null) {
			System.err.println("PutChunk : data = null not accepted.");
			return;
		}
		String protocolVersion = serverObject.getProtocolVersion();
		Multicast mControlCh = serverObject.getControlChannel();
		Multicast mDataBackupCh = serverObject.getDataBackupChannel();
		
		StringBuilder headerBuilder = new StringBuilder("PUTCHUNK ");
		headerBuilder.append(serverObject.getProtocolVersion()).append(" ").
		append(serverObject.getServerId()).append(" ").
		append(fileId).append(" ").
		append(chunkNumber).append(" ").
		append(replicationDegree).append("\r\n\r\n");

		byte[] header = headerBuilder.toString().getBytes();
		byte[] chunk = new byte[header.length + data.length];
		System.arraycopy(header, 0, chunk, 0, header.length);
		System.arraycopy(data, 0, chunk, header.length, data.length);

		//Send the chunk
		mDataBackupCh.send(chunk);
		
		//Wait in the control channel for STORED messages
		// - Count number of STORED's received in 1 sec
		// - IF not enough THEN retransmits 2x waiting_time(1 sec) (after 5 times -> error)
		ArrayList<Message> storedConfirmations = new ArrayList<>();
		int waitTime = Constants.maxWaitTime;
		int retries = 0;
		long lastTime = System.currentTimeMillis();
		long elapsedTime = 0;
		while(retries < 5)
		{
			while(elapsedTime < waitTime) {
				try {
					byte[] msg = mControlCh.receive(waitTime);
					if(msg != null) {
						Message m = new Message(msg);
						if (m.getMessageType().equalsIgnoreCase("STORED") && m.getVersion().equalsIgnoreCase(protocolVersion))
							storedConfirmations.add(m);
					}
				} catch (SocketException e) {
					break;
				}

				elapsedTime += System.currentTimeMillis() - lastTime;
				lastTime = System.currentTimeMillis();
			}
			if(storedConfirmations.size() >= replicationDegree) {
				break;
			} else {
				waitTime = waitTime * 2;
				retries++;
			}
		}
		
		if(retries >= 5)
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
		int serverId = serverObject.getServerId();    // not used, but should be used to send STORED message
		Multicast mControlCh = serverObject.getControlChannel();
		Multicast mDataBackupCh = serverObject.getDataBackupChannel();
		ServerDatabase db = serverObject.getDb();

		while (true) {
			Message m = new Message(mDataBackupCh.receive());
			//To avoid receiving messages from himself
			if(Integer.parseInt(m.getSenderId()) == serverObject.getServerId())
				continue;

			if (m.getMessageType().equalsIgnoreCase("PUTCHUNK") && m.getVersion().equalsIgnoreCase(protocolVersion)) {
				Random randomGenerator = new Random();
				int delay = randomGenerator.nextInt(Constants.maxDelayTime);
				long delayEnding = System.currentTimeMillis() + delay;
				int actualReplicationDegree = 0;

				//During delay time, checks how many replicas of the chunk have been stored in other peers
				while (System.currentTimeMillis() < delayEnding) {
					try {
						byte[] msg = mControlCh.receive(delay);
						if (msg != null && new Message(msg).getChunkNo() == m.getChunkNo())
							actualReplicationDegree++;
					} catch (SocketException e) {
						break;
					}
				}

				//Creates and writes content to file. In enhanced protocols, this only happens if replicationDegree is not satisfied
				if (serverObject.getProtocolVersion().equals("1.0") || actualReplicationDegree < Integer.parseInt(m.getReplicationDeg())) {
					try {
						new File(Paths.getFolderPath(serverId, m.getFileId())).mkdirs();
						String filePath = Paths.getChunkPath(serverId, m.getFileId(), Integer.parseInt(m.getChunkNo()));
						FileOutputStream fileStream = new FileOutputStream(filePath);
						fileStream.write(m.getBody());
						fileStream.close();
					} catch (IOException e) {
						//TODO Could not write/create file message?
						e.printStackTrace();
						continue;
					}
				}

				//Creates and sends STORED confirmation message
				StringBuilder headerBuilder = new StringBuilder("STORED ");
				headerBuilder.append(protocolVersion).append(" ").
						append(serverId).append(" ").
						append(m.getFileId()).append(" ").
						append(m.getChunkNo()).append("\r\n\r\n");

				mControlCh.send(headerBuilder.toString().getBytes());

				//Put fileInfo in the database
				FileChunkData chunkData = new FileChunkData(
						Integer.parseInt(m.getChunkNo()),
						m.getBody().length / 1000,	// chunk size in KBytes
						actualReplicationDegree);
				DBFileData dbFileData = db.getStoredFileData(m.getFileId());
				if(dbFileData == null) {
					db.addOrUpdateStoredFileData(m.getFileId(), Integer.parseInt(m.getReplicationDeg()));
					dbFileData = db.getStoredFileData(m.getFileId());
				}
				dbFileData.addOrUpdateFileChunkData(chunkData);
			}
		}
	}

}
