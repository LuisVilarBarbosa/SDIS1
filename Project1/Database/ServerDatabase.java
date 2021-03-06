package Project1.Database;

import Project1.General.Paths;

import java.io.*;
import java.util.*;

public class ServerDatabase {
    private static final int updateFilePeriod = 10000;  // milliseconds
    private static final String delim = "|";
    private final String dbPath;
    private long storageCapacity = 1000000000;    // bytes (default defined for the first use)
    private HashMap<String, DBFileData> backedUpFiles = new HashMap<>();    // backed up to other peers
    private HashMap<String, DBFileData> storedFiles = new HashMap<>();  //<FileID, FileData>

    public ServerDatabase(int serverId) {
        dbPath = Paths.getDatabasePath(serverId);
        try {
            FileInputStream fis = new FileInputStream(dbPath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader file = new BufferedReader(isr);

            String line = file.readLine();
            storageCapacity = Long.parseLong(line);
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
            File folder = new File(Paths.getDatabaseFolder(serverId));
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

        // update the database file to prevent failures
        createUpdateThread();

        // on program shutdown save database to file
        Runtime.getRuntime().addShutdownHook(shutdownThread());
    }

    private void loadDatabase(BufferedReader file, HashMap<String, DBFileData> filesData) throws IOException {
        String line;
        while ((line = file.readLine()) != null && !line.equals("")) {
            StringTokenizer st = new StringTokenizer(line, delim);
            String filePath = st.nextToken();
            String fileId = st.nextToken();
            int desiredReplicationDegree = Integer.parseInt(st.nextToken());
            int chunkNo = Integer.parseInt(st.nextToken());
            long size = Long.parseLong(st.nextToken());
            int perceivedReplicationDegree = Integer.parseInt(st.nextToken());
            DBFileData dbFileData;
            if ((dbFileData = filesData.get(fileId)) == null)
                dbFileData = new DBFileData(filePath, fileId, desiredReplicationDegree);
            dbFileData.addOrUpdateFileChunkData(new FileChunkData(chunkNo, size, perceivedReplicationDegree));
            filesData.put(fileId, dbFileData);
        }
    }

    private void createUpdateThread() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    saveDatabase();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 0, updateFilePeriod);
    }

    private Thread shutdownThread() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    saveDatabase();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void saveDatabase() {
        try {
            FileOutputStream fos = new FileOutputStream(dbPath);
            OutputStreamWriter file = new OutputStreamWriter(fos);

            file.write(String.valueOf(storageCapacity));
            file.write("\r\n\r\nBacked up files:\r\n");
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

    private void storeDatabase(OutputStreamWriter file, HashMap<String, DBFileData> filesData) throws IOException {
        for (HashMap.Entry<String, DBFileData> entry : filesData.entrySet()) {
            DBFileData dbFileData = entry.getValue();
            String filePath = dbFileData.getFilePath();
            String fileId = dbFileData.getFileId();
            int desiredReplicationDegree = dbFileData.getDesiredReplicationDegree();

            ArrayList<Integer> chunksNos = dbFileData.listChunksNos();
            for (int chunkNo : chunksNos) {
                FileChunkData fileChunkData = dbFileData.getFileChunkData(chunkNo);
                StringBuilder st = new StringBuilder();
                st.append(filePath).append(delim);
                st.append(fileId).append(delim);
                st.append(desiredReplicationDegree).append(delim);
                st.append(fileChunkData.getChunkNo()).append(delim);
                st.append(fileChunkData.getSize()).append(delim);
                st.append(fileChunkData.getPerceivedReplicationDegree()).append("\r\n");
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

    public ArrayList<String> getBackedUpFilesIds() {
        return listFilesIds(backedUpFiles);
    }

    public ArrayList<String> getStoredFilesIds() {
        return listFilesIds(storedFiles);
    }

    private ArrayList<String> listFilesIds(HashMap<String, DBFileData> filesData) {
        ArrayList<String> filesIds = new ArrayList<>();
        for (String key : filesData.keySet())
            filesIds.add(key);
        return filesIds;
    }

    public void removeBackedUpFile(String fileId) {
        backedUpFiles.remove(fileId);
    }

    public void removeStoredFile(String fileId) {
        storedFiles.remove(fileId);
    }

    public DBFileData getBackedUpFileData(String fileId) {
        return backedUpFiles.get(fileId);
    }

    public DBFileData getStoredFileData(String fileId) {
        return storedFiles.get(fileId);
    }

    public long getUsedStorage() {
        ArrayList<String> storedFilesIds = getStoredFilesIds();
        long usedStorage = 0;
        for(String storedFilesId : storedFilesIds) {
            DBFileData dbFileData = storedFiles.get(storedFilesId);
            ArrayList<Integer> chunksNos = dbFileData.listChunksNos();
            for (int chunkNo : chunksNos)
                usedStorage += dbFileData.getFileChunkData(chunkNo).getSize();
        }
        return usedStorage;
    }

    public long getFreeStorage() {
        return storageCapacity - getUsedStorage();
    }

    public String getBackedUpFileId(String filePath) {
        ArrayList<String> backedUpFilesIds =  getBackedUpFilesIds();
        for(int i = 0; i < backedUpFilesIds.size(); i++) {
            String id = backedUpFilesIds.get(i);
            if(backedUpFiles.get(id).getFilePath().equals(filePath))
                return id;
        }
        return null;
    }

    public String getStoredFileId(String filePath) {
        ArrayList<String> storedFilesIds =  getStoredFilesIds();
        for(int i = 0; i < storedFilesIds.size(); i++) {
            String id = storedFilesIds.get(i);
            if(storedFiles.get(id).getFilePath().equals(filePath))
                return id;
        }
        return null;
    }
    
    public void addOrUpdateBackedUpFileData(String filePath, String fileId, int desiredReplicationDegree) {
    	DBFileData fileData = new DBFileData(filePath, fileId, desiredReplicationDegree);
    	this.backedUpFiles.put(fileId, fileData);
    }
    
    public void addOrUpdateStoredFileData(String fileId, int desiredReplicationDegree) {
    	DBFileData fileData = new DBFileData("", fileId, desiredReplicationDegree);
    	this.storedFiles.put(fileId, fileData);
    }

}
