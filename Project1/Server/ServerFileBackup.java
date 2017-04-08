package Project1.Server;

import java.io.FileInputStream;
import java.io.IOException;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */

public class ServerFileBackup {
	
	public static final int CHUNK_MAX_SIZE = 64000;
	
	public static void backup(ServerObject serverObject, String filePath, String fileId, int replicationDegree) throws IOException {
		//Responsavel por 
		// - Abrir o ficheiro
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		//1 thread por ficheiro
		
		//Abrir o ficheiro
		FileInputStream fis = new FileInputStream(filePath);
		
		int bytesRead = CHUNK_MAX_SIZE;
		
		for(int chunkNumber = 0; bytesRead < CHUNK_MAX_SIZE; chunkNumber++){			
			
			//Import 64kbits from the file
			byte[] chunkData = null;
			bytesRead = fis.read(chunkData, 0, CHUNK_MAX_SIZE);
			
			//Send data to Chunk Backup
			ServerChunkBackup.putChunk(serverObject, fileId, chunkData, replicationDegree, chunkNumber);
		}
		
		fis.close();
	}
}
