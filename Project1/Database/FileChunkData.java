package Project1.Database;

public class FileChunkData {
    private int chunkNo;
    private long size;  // bytes
    private int perceivedReplicationDegree;

    public FileChunkData(int chunkNo, long size, int perceivedReplicationDegree) {
        this.chunkNo = chunkNo;
        this.size = size;
        this.perceivedReplicationDegree = perceivedReplicationDegree;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public long getSize() {
        return size;
    }

    public int getPerceivedReplicationDegree() {
        return perceivedReplicationDegree;
    }

    public void setPerceivedReplicationDegree(int perceivedReplicationDegree) {
        this.perceivedReplicationDegree = perceivedReplicationDegree;
    }

}
