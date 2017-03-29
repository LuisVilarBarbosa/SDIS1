import java.rmi.RemoteException;
import java.util.ArrayList;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {
	private static final int maxWaitTime = 1000; /* milliseconds */
	
	public static void putChunk(String protocolVersion, int serverId, Multicast mControlCh, Multicast mDataBackupCh, Message message, int replicationDegree) throws RemoteException {
		//Send the chunk
		mDataBackupCh.send(message.generateByteArray());
		
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
				storedConfirmations.add(mControlCh.receive());
				
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
		
		//Keep track of PUTCHUNKS.
		//  - This should be stored in a file (non-volatile memory)
		
	}
}
