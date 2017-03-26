import javax.naming.directory.InvalidAttributesException;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */
public class ServerFileBackup {
	
	/**
	 * Returns the number of chunks necessary to send the file.
	 * The last chunk is always shorter. In case the filesize/MAX_BODY_SIZE is an integer, the last chunk
	 * has bodySize = 0;
	 * @param filesize
	 * @return number of chunks
	 */
	private static long totalChunkNumber(long filesize) {
		return (filesize / Message.MAX_BODY_SIZE) + 1;
	}
	
	public static void backup(ServerObject serverInfo, String fileId, int replicationDegree, byte[] data, long size) {
		//Responsavel por 
		// - Dividir em chunks
		// - Criar chunk no e ?Message?
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
			
			//TODO é preciso estas 2 variaveis? Ja nao consigo pensar direito
			bytesRead = bytesRead + chunkBodySize;
			bytesRemaining = bytesRemaining - chunkBodySize;
			
			//TODO Send data to Chunk Backup
			ServerChunkBackup.doStuff();
		}
		
	}
}
