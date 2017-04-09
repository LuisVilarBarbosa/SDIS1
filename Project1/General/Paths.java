package Project1.General;

public class Paths {
    private static final char pathDelim = '/';
    private static final String serversFolder = "servers_data";
    private static final String dbFilename = "database.txt";

    private static StringBuilder calculateFolderPath(int serverId, String fileId) {
        StringBuilder sb = new StringBuilder();
        sb.append(serversFolder).append(pathDelim).append(serverId).append(pathDelim).append(fileId);
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

    private static StringBuilder calculateDatabaseFolder(int serverId) {
        StringBuilder sb = new StringBuilder();
        sb.append(serversFolder).append(pathDelim).append(serverId);
        return sb;
    }

    public static String getDatabaseFolder(int serverId) {
        StringBuilder sb = calculateDatabaseFolder(serverId);
        return sb.toString();
    }

    public static String getDatabasePath(int serverId) {
        StringBuilder sb = calculateDatabaseFolder(serverId).append(pathDelim).append(dbFilename);
        return sb.toString();
    }

}
