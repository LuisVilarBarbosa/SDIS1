package Project1.Database;

import java.util.HashMap;

public class DBFileData {
    private String filePath;
    private String fileId;
    private int desiredReplicationDegree;
    private HashMap<Integer, FileChunkData> fileChunksData;

    public DBFileData(String filePath, String fileId, int desiredReplicationDegree) {
        this.filePath = filePath;
        this.fileId = fileId;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.fileChunksData = new HashMap<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public FileChunkData getFileChunkData(int chunkNo) {
        return fileChunksData.get(chunkNo);
    }

    public int getNumFileChunks() {
        return fileChunksData.size();
    }

    public void addOrUpdateFileChunkData(FileChunkData fileChunkData) {
        fileChunksData.put(fileChunkData.getChunkNo(), fileChunkData);
    }

}
