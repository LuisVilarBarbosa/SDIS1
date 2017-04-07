package Project1.Database;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ServerDatabase {
    private static final String dbFilename = "database.txt";
    private static final String delim = "|";
    private final String dbPath;
    private long storageSize;    // bytes
    private HashMap<String, DBFileData> db = new HashMap<>();

    public ServerDatabase(int serverId) {
        dbPath = serverId + "/" + dbFilename;
        try {
            FileInputStream fis = new FileInputStream(dbPath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader file = new BufferedReader(isr);
            String line = file.readLine();
            storageSize = Integer.getInteger(line);
            while ((line = file.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, delim);
                String filePath = st.nextToken();
                String lastModificationDate = st.nextToken();
                int chunkId = Integer.getInteger(st.nextToken());
                int desiredReplicationDegree = Integer.getInteger(st.nextToken());
                int detectedReplicationDegree = Integer.getInteger(st.nextToken());
                DBFileData dbFileData;
                if ((dbFileData = db.get(filePath)) == null)
                    dbFileData = new DBFileData(filePath, lastModificationDate);
                dbFileData.addFileChunkData(new FileChunkData(chunkId, desiredReplicationDegree, detectedReplicationDegree));
                db.put(filePath, dbFileData);
            }
            file.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.out.println("Building new database.");
            File folder = new File(Integer.toString(serverId));
            folder.mkdirs();
            File file = new File(dbPath);
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // on program shutdown save database to file
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(dbPath);
                    OutputStreamWriter file = new OutputStreamWriter(fos);
                    file.write(storageSize + "\r\n");
                    for (HashMap.Entry<String, DBFileData> entry : db.entrySet()) {
                        DBFileData dbFileData = entry.getValue();
                        String filePath = dbFileData.getFilePath();
                        String lastModificationDate = dbFileData.getLastModificationDate();
                        int numFileChunks = dbFileData.getNumFileChunks();
                        for (int i = 1; i <= numFileChunks; i++) {
                            FileChunkData fileChunkData = dbFileData.getFileChunkData(i);
                            StringBuilder st = new StringBuilder(entry.getKey());
                            st.append(delim).append(filePath);
                            st.append(delim).append(lastModificationDate);
                            st.append(delim).append(fileChunkData.getChunkId());
                            st.append(delim).append(fileChunkData.getDesiredReplicationDegree());
                            st.append(delim).append(fileChunkData.getDetectedReplicationDegree());
                            st.append("\r\n");
                            file.write(st.toString());
                        }
                    }
                    file.close();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getLastModificationDate(String filePath) {
        return db.get(filePath).getLastModificationDate();
    }

    public void addChunk(String filePath, String lastModificationDate, int chunkId, int desiredReplicationDegree, int detectedReplicationDegree) {
        DBFileData dbFileData = db.get(filePath);
        if (dbFileData == null)
            dbFileData = new DBFileData(filePath, lastModificationDate);
        dbFileData.addFileChunkData(new FileChunkData(chunkId, desiredReplicationDegree, detectedReplicationDegree));
        db.put(filePath, dbFileData);
    }

    public void removeFile(String filePath) {
        db.remove(filePath);
    }

    public long getStorageSize() {
        return storageSize;
    }

    public void setStorageSize(long storageSize) {
        this.storageSize = storageSize;
    }

    public DBFileData getDBFileData(String fileId) {
        return db.get(fileId);
    }

}
