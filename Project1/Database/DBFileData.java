package Project1.Database;

import java.util.ArrayList;
import java.util.HashMap;

public class DBFileData {
    private String filePath;
    private String fileId;
    private int desiredReplicationDegree;
    private HashMap<Integer, FileChunkData> fileChunksData;

    public DBFileData(String filePath, String fileId, int desiredReplicationDegree) {
        if (filePath == null || filePath == "")
            this.filePath = " ";    // necessary when loading tokens from the file on ServerDatabase
        else
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

    public void removeFileChunkData(int chunkNo) {
        fileChunksData.remove(chunkNo);
    }

    public ArrayList<Integer> listChunksNos() {
        ArrayList<Integer> chunksNos = new ArrayList<>();
        for (Integer key : fileChunksData.keySet())
            chunksNos.add(key);
        return chunksNos;
    }

}
