package Project1.Database;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ServerDatabase {
    private static final String dbFilename = "database.txt";
    private static final String delim = "|";
    private final String dbPath;
    private long storageCapacity;    // KBytes
    private HashMap<String, DBFileData> backedUpFiles = new HashMap<>();    // backed up to other peers
    private HashMap<String, DBFileData> storedFiles = new HashMap<>();

    public ServerDatabase(int serverId) {
        dbPath = serverId + "/" + dbFilename;
        try {
            FileInputStream fis = new FileInputStream(dbPath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader file = new BufferedReader(isr);

            String line = file.readLine();
            storageCapacity = Integer.getInteger(line);
            file.readLine();    // empty line
            file.readLine();    // 'Backed up files:' line
            loadDatabase(file, backedUpFiles);
            file.readLine();    // 'Stored files:' line
            loadDatabase(file, storedFiles);

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
        Runtime.getRuntime().addShutdownHook(shutdownThread());
    }

    private void loadDatabase(BufferedReader file, HashMap<String, DBFileData> filesData) throws IOException {
        String line;
        while ((line = file.readLine()) != null && !line.equals("")) {
            StringTokenizer st = new StringTokenizer(line, delim);
            String filePath = st.nextToken();
            String fileId = st.nextToken();
            int desiredReplicationDegree = Integer.getInteger(st.nextToken());
            int chunkNo = Integer.getInteger(st.nextToken());
            long size = Long.getLong(st.nextToken());
            int perceivedReplicationDegree = Integer.getInteger(st.nextToken());
            DBFileData dbFileData;
            if ((dbFileData = filesData.get(filePath)) == null)
                dbFileData = new DBFileData(filePath, fileId, desiredReplicationDegree);
            dbFileData.addOrUpdateFileChunkData(new FileChunkData(chunkNo, size, perceivedReplicationDegree));
            filesData.put(filePath, dbFileData);
        }
    }

    private Thread shutdownThread() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(dbPath);
                    OutputStreamWriter file = new OutputStreamWriter(fos);

                    file.write(storageCapacity + "\r\n\r\nBacked up files:\r\n");
                    storeDatabase(file, backedUpFiles);
                    file.write("\r\nStored files:\r\n");
                    storeDatabase(file, storedFiles);

                    file.close();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void storeDatabase(OutputStreamWriter file, HashMap<String, DBFileData> filesData) throws IOException {
        for (HashMap.Entry<String, DBFileData> entry : filesData.entrySet()) {
            DBFileData dbFileData = entry.getValue();
            String filePath = dbFileData.getFilePath();
            String fileId = dbFileData.getFileId();
            int desiredReplicationDegree = dbFileData.getDesiredReplicationDegree();
            int numFileChunks = dbFileData.getNumFileChunks();
            for (int i = 1; i <= numFileChunks; i++) {
                FileChunkData fileChunkData = dbFileData.getFileChunkData(i);
                StringBuilder st = new StringBuilder(entry.getKey());
                st.append(delim).append(filePath);
                st.append(delim).append(fileId);
                st.append(delim).append(desiredReplicationDegree);
                st.append(delim).append(fileChunkData.getChunkNo());
                st.append(delim).append(fileChunkData.getSize());
                st.append(delim).append(fileChunkData.getPerceivedReplicationDegree());
                st.append("\r\n");
                file.write(st.toString());
            }
        }
    }

    public long getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(long storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public ArrayList<String> getBackedUpFilesPaths() {
        return listFilesPaths(backedUpFiles);
    }

    public ArrayList<String> getStoredFilesPaths() {
        return listFilesPaths(storedFiles);
    }

    private ArrayList<String> listFilesPaths(HashMap<String, DBFileData> filesData) {
        ArrayList<String> filesPaths = new ArrayList<>();
        for (String key : filesData.keySet())
            filesPaths.add(key);
        return filesPaths;
    }

    public void removeStoredFile(String filePath) {
        storedFiles.remove(filePath);
    }

    public DBFileData getBackedUpFileData(String filePath) {
        return backedUpFiles.get(filePath);
    }

    public DBFileData getStoredFileData(String filePath) {
        return storedFiles.get(filePath);
    }

    public long getUsedStorage() {
        ArrayList<String> storedFilesPaths = getStoredFilesPaths();
        int storedFilesPathsSize = storedFilesPaths.size();
        long usedStorage = 0;
        for(int i = 0; i < storedFilesPathsSize; i++) {
            DBFileData dbFileData = storedFiles.get(storedFilesPaths.get(i));
            int numFileChunks = dbFileData.getNumFileChunks();
            for(int j = 0; j < numFileChunks; j++)
                usedStorage += dbFileData.getFileChunkData(j).getSize();
        }
        return usedStorage;
    }

    public long getFreeStorage() {
        return storageCapacity - getUsedStorage();
    }

}
