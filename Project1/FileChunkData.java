public class FileChunkData {
    private int chunkId;
    private int desiredReplicationDegree;
    private int detectedReplicationDegree;

    public FileChunkData(int chunkId, int desiredReplicationDegree, int detectedReplicationDegree) {
        this.chunkId = chunkId;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.detectedReplicationDegree = detectedReplicationDegree;
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getDesiredReplicationDegree() {
        return desiredReplicationDegree;
    }

    public int getDetectedReplicationDegree() {
        return detectedReplicationDegree;
    }

    public void setDetectedReplicationDegree(int detectedReplicationDegree) {
        this.detectedReplicationDegree = detectedReplicationDegree;
    }

}
