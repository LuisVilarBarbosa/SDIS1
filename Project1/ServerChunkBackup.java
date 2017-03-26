/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
/* Generic message to send: STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerChunkBackup {
	private static final int maxWaitTime = 400; /* milliseconds */
	
	public int putChunk(ServerObject serverInfo, String fileId, int chunkNum, int replicationDegree, byte[] data) {
		Message chunk = new Message(serverInfo, fileId, chunkNum, replicationDegree);
		chunk.setBodyData(data);
		
		return 0;
	}
}
