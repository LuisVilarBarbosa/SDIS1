import java.rmi.RemoteException;

import javax.naming.directory.InvalidAttributesException;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
public class ServerFileBackup {
	
	public static void backup(ServerObject serverInfo, String fileId, int replicationDegree, byte[] data, long size) throws RemoteException {
		//Responsavel por 
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		long bytesRead = 0;
		int bytesRemaining = (int)(size - bytesRead);
		
		for(long chunkNumber = 0; bytesRead < size; chunkNumber++){
			
			Message chunk = new Message(serverInfo, fileId, (int)chunkNumber, replicationDegree);
			int chunkBodySize;
			
			try {
				chunkBodySize = chunk.generateHeader();
			} catch (InvalidAttributesException e) {
				chunkNumber--;
				continue;
			}
			byte[] chunkBody;
			
			if(bytesRemaining < chunkBodySize) //Last Chunk
			{
				chunkBody = new byte[bytesRemaining];
				System.arraycopy(data, (int)bytesRead, chunkBody, 0, bytesRemaining);
			} else {
				chunkBody = new byte[chunkBodySize];
				System.arraycopy(data, (int)bytesRead, chunkBody, 0, chunkBodySize);
			}
			
			chunk.setBodyData(chunkBody);
			
			//TODO é preciso estas 2 variaveis? Ja nao consigo pensar direito
			bytesRead = bytesRead + chunkBodySize;
			bytesRemaining = bytesRemaining - chunkBodySize;
			
			//TODO Send data to Chunk Backup
			ServerChunkBackup.putChunk(serverInfo.getProtocolVersion(), 
					serverInfo.getServerId(), 
					serverInfo.getControlChannel(),
					serverInfo.getDataBackupChannel(), 
					chunk, replicationDegree);
		}
		
	}
}
