package Project1.Server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

import Project1.Database.FileChunkData;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {
	private static final int maxWaitTime = 1000; /* milliseconds */
	private static final int maxDelayTime = 400;
	private static final String chunkFolder = "/chunks";
	
	
	//TODO Create a different Multicast object for each thread
	//Is this on this level, or higher?
	
	public static void putChunk(ServerObject serverObject, String fileId, byte[] data, int replicationDegree, int chunkNumber) throws RemoteException {
		Multicast mControlCh = serverObject.getControlChannel();
		Multicast mDataBackupCh = serverObject.getDataBackupChannel();	
		
		StringBuilder headerBuilder = new StringBuilder("PUTCHUNK ");
		headerBuilder.append(serverObject.getProtocolVersion()).append(" ").
		append(serverObject.getServerId()).append(" ").
		append(fileId).append(" ").
		append(chunkNumber).append(" ").
		append(replicationDegree).append(" ").
		append("\n\n");
		
		//TODO Verify if this works, or if a ByteArrayOutputStream is needed
		byte[] chunk = headerBuilder.toString().getBytes();

		//Send the chunk
		mDataBackupCh.send(chunk);
		
		//Wait in the control channel for STORED messages
		// - Count number of STORED's received in 1 sec
		// - IF not enough THEN retransmits 2x waiting_time(1 sec) (after 5 times -> error)
		ArrayList<byte[]> storedConfirmations = new ArrayList<byte[]>();
		int waitTime = maxWaitTime;
		int retries = 0;
		long lastTime = System.currentTimeMillis();
		long elapsedTime = 0;
		while(retries < 5)
		{
			while(elapsedTime < waitTime) {
				try {
					storedConfirmations.add(mControlCh.receive(waitTime));
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
			throw new RemoteException("Cannot backup: Number of retries exceeded");
		
		
		FileChunkData chunkData;
		if(data == null)
			chunkData = new FileChunkData(chunkNumber, 0, storedConfirmations.size());
		else
			chunkData = new FileChunkData(chunkNumber, data.length, storedConfirmations.size());
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
		int serverId = serverObject.getServerId();	// not used, but should be used to send STORED message
		Multicast mControlCh = serverObject.getControlChannel();
		Multicast mDataBackupCh = serverObject.getDataBackupChannel();

		
		Message chunk = new Message(mDataBackupCh.receive());
		
		//Versions not compatible
		if(!chunk.getVersion().equals(protocolVersion))
			return;
		
		Random randomGenerator = new Random();
		int delay = randomGenerator.nextInt(maxDelayTime);
		long delayEnding = System.currentTimeMillis() + delay;
		int actualReplicationDegree = 0;
		
		//During delay time, checks how many replicas of the chunk have been stored in other peers
		while(System.currentTimeMillis() < delayEnding) {
			try {
				Message storeConfirmation = new Message(mControlCh.receive(delay));
				if(storeConfirmation.getChunkNo() == chunk.getChunkNo())
					actualReplicationDegree++;
			} catch (SocketException e) {
				break;
			}
		}
		
		//Creates and writes content to file. In enhanced protocols, this only happens if replicationDegree is not satisfied
		if(serverObject.getProtocolVersion().equals("1.0") || actualReplicationDegree < Integer.parseInt(chunk.getReplicationDeg())) {
			try {
				String filePath = generateFilePath(chunk.getFileId(), chunk.getChunkNo());
				FileOutputStream fileStream = new FileOutputStream(filePath);
				fileStream.write(chunk.getBody());
				fileStream.close();
			} catch (IOException e) {
				//TODO Could not write/create file message?
				e.printStackTrace();
				return;
			}
		}
		
		//Creates and sends STORED confirmation message
		StringBuilder headerBuilder = new StringBuilder("STORED ");
		headerBuilder.append(protocolVersion).append(" ").
		append(chunk.getSenderId()).append(" ").
		append(chunk.getFileId()).append(" ").
		append(chunk.getChunkNo()).append(" ").
		append("\n\n");
		
		mControlCh.send(headerBuilder.toString().getBytes());
		
		//Put fileInfo in the database
		serverObject.getDb().addStoredFile(chunk.getFileId(), Integer.parseInt(chunk.getReplicationDeg()));
		FileChunkData chunkData = new FileChunkData(
				Integer.parseInt(chunk.getChunkNo()), 
				chunk.getBody().length, 
				actualReplicationDegree);
		serverObject.getDb().getStoredFileData(chunk.getFileId()).addOrUpdateFileChunkData(chunkData);
	}
	
	
	private static String generateFilePath(String fileId, String chunkNum) {
		return chunkFolder + "/" + fileId + "_" + chunkNum;
	}
}
