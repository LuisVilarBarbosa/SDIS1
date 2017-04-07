package Project1.Server;

import java.rmi.RemoteException;

import javax.naming.directory.InvalidAttributesException;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */

public class ServerFileBackup {
	
	public static void backup(ServerObject serverInfo, String fileId, int replicationDegree, byte[] data) throws RemoteException {
		//Responsavel por 
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		//1 thread por ficheiro
		
		int bytesRead = 0;
		
		for(long chunkNumber = 0; bytesRead < data.length; chunkNumber++){			
			//TODO Generate header in ServerChunkBackup
			StringBuilder headerBuilder = new StringBuilder("PUTCHUNK ");
			headerBuilder.append(serverInfo.getProtocolVersion()).append(" ").
			append(serverInfo.getServerId()).append(" ").
			append(fileId).append(" ").
			append(chunkNumber).append(" ").
			append(replicationDegree).append(" ").
			append("\n\n");
			
			//TODO Verify if this works, or if a ByteArrayOutputStream is needed
			byte[] chunk = headerBuilder.toString().getBytes();
			
			//TODO Import 64kbits from the data array
			
			//TODO Send data to Chunk Backup
			ServerChunkBackup.putChunk(serverInfo.getProtocolVersion(), 
					serverInfo.getServerId(), 
					serverInfo.getControlChannel(),
					serverInfo.getDataBackupChannel(), 
					chunk, replicationDegree, chunkNo);
		}
		
	}
}
