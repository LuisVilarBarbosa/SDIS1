package Project1.Server;

import Project1.General.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */

public class ServerFileBackup {
	
	public static void backup(ServerObject serverObject, String filePath, String fileId, int replicationDegree) throws IOException {
		//Responsavel por 
		// - Abrir o ficheiro
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		//1 thread por ficheiro
		
		//Abrir o ficheiro
		FileInputStream fis = new FileInputStream(filePath);

		int bytesRead = Constants.maxChunkSize;

		for(int chunkNumber = 0; bytesRead == Constants.maxChunkSize; chunkNumber++){
			byte[] chunkData = new byte[Constants.maxChunkSize];
			bytesRead = fis.read(chunkData);

			//Send data to Chunk Backup
			if(bytesRead != Constants.maxChunkSize)
				chunkData = Arrays.copyOf(chunkData, bytesRead);
			ServerChunkBackup.putChunk(serverObject, fileId, chunkData, replicationDegree, chunkNumber);
		}
		
		fis.close();
	}
}
