/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
public class ServerFileBackup {
	//TODO O que é o protocol version? Está a funcionar como file version, mas não é...
	//TODO Alguns parametros do "putchunk" são necessários?
	
	public static void backup(String protocolVersion, int serverId, int replicationDegree, byte[] data, long size) {
		
	}
}
