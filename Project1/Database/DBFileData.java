package Project1.Database;

import java.util.HashMap;

public class DBFileData {
    private String filePath;
    private String lastModificationDate;
    private HashMap<Integer, FileChunkData> fileChunksData;

    public DBFileData(String filePath, String lastModificationDate) {
        this.filePath = filePath;
        this.lastModificationDate = lastModificationDate;
        this.fileChunksData = new HashMap<Integer, FileChunkData>();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public FileChunkData getFileChunkData(int chunkId) {
        return fileChunksData.get(chunkId);
    }

    public int getNumFileChunks() {
        return fileChunksData.size();
    }

    public void addFileChunkData(FileChunkData fileChunkData) {
        fileChunksData.put(fileChunkData.getChunkId(), fileChunkData);
    }

}
