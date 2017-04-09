package Project1.Server;

import Project1.General.Constants;

import java.io.*;
import java.rmi.RemoteException;
import java.util.Arrays;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */

public class ServerFileBackup {
	
	public static void backup(ServerObject serverObject, String filePath, String fileId, int replicationDegree) {
		//Responsavel por 
		// - Abrir o ficheiro
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		//1 thread por ficheiro
		
		//Abrir o ficheiro

		serverObject.getDb().addOrUpdateBackedUpFileData(filePath, fileId, replicationDegree);

		try {
			FileInputStream fis = new FileInputStream(filePath);

			int bytesRead = Constants.maxChunkSize;

			for (int chunkNumber = 0; bytesRead == Constants.maxChunkSize; chunkNumber++) {
				byte[] chunkData = new byte[Constants.maxChunkSize];
				bytesRead = fis.read(chunkData);
				System.out.println("Chunk number:" + chunkNumber);

				//Send data to Chunk Backup
				if (bytesRead != Constants.maxChunkSize)
					chunkData = Arrays.copyOf(chunkData, bytesRead);
				ServerChunkBackup.putChunk(serverObject, fileId, chunkData, replicationDegree, chunkNumber);
			}

			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
