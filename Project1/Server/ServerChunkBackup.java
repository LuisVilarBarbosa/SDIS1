package Project1.Server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {
	private static final int maxWaitTime = 1000; /* milliseconds */
	private static final int maxDelayTime = 400;
	private static final String chunkFolder = "/chunks";
	
	
	//TODO Create a different Multicast object for each thread
	//Is this on this level, or higher?
	
	//TODO Implement the option to use enhanced protocol, or not. MUST HAVE BOTH VERSIONS
	
	public static void putChunk(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataBackupCh, byte[] chunk, int replicationDegree) throws RemoteException {
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
				break; //TODO Would it be better to put retries = 5? In terms of performance
			} else {
				waitTime = waitTime * 2;
				retries++;
			}
		}
		
		if(retries >= 5)
			throw new RemoteException("Cannot backup: Number of retries exceeded");
		
		//TODO Keep track of PUTCHUNKS.
		//  - This should be stored in a file (non-volatile memory)
		
	}
	
	public static void storeChunk(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataBackupCh) throws FileNotFoundException {
		//Receive chunk
		//Create file name
		//After random delay (0 - 400 ms)
		// - On other thread, wait for other stores, to count the replication degree
		// - Create file
		// - Write content to file
		Message chunk = new Message(mDataBackupCh.receive());
		
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
		
		if(actualReplicationDegree < Integer.parseInt(chunk.getReplicationDeg())) {
			try {
				String filePath = generateFilePath(chunk.getFileId(), chunk.getChunkNo());
				FileOutputStream fileStream = new FileOutputStream(filePath);
				fileStream.write(chunk.getBody());
				fileStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Message stored = chunk;
		stored.setBodyData(null);
		stored.setMessageType(MESSAGE_TYPE.STORED);
		mControlCh.send(stored.generateByteArray());
		
	}
	
	
	//TODO mudar para serverID/file/chunknum(.file) nao leva o .file, � so para se perceber que � um ficheiro
	private static String generateFilePath(String fileId, String chunkNum) {
		return chunkFolder + "/" + fileId + "_" + chunkNum + ".txt";
	}
}
