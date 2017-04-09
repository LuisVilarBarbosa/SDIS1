package Project1.General;

public class Paths {
    private static final char pathDelim = '/';

    private static StringBuilder calculateFolderPath(int serverId, String fileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("servers_data").append(pathDelim).append(serverId).append(pathDelim).append(fileId);
        return sb;
    }

    public static String getFolderPath(int serverId, String fileId) {
        StringBuilder sb = calculateFolderPath(serverId, fileId);
        return sb.toString();
    }

    public static String getChunkPath(int serverId, String fileId, int chunkNo) {
        StringBuilder sb = calculateFolderPath(serverId, fileId).append(pathDelim).append(chunkNo);
        return sb.toString();
    }

}
