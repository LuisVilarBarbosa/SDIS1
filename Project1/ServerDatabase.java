import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ServerDatabase {
    private final String dbPath;
    private static final String dbFilename = "database.txt";
    private static final String delim = "|";
    HashMap<String, ArrayList<String>> db = new HashMap<>();

    public ServerDatabase(int serverId) {
        dbPath = serverId + "/" + dbFilename;
        try {
            FileInputStream fis = new FileInputStream(dbPath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader file = new BufferedReader(isr);
            String line;
            while ((line = file.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, delim);
                String filename = st.nextToken();
                ArrayList<String> lastModificationDates = new ArrayList<>();
                int numTokens = st.countTokens();
                for (int i = 0; i < numTokens; i++)
                    lastModificationDates.add(st.nextToken());
                db.put(filename, lastModificationDates);
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
                    for (HashMap.Entry<String, ArrayList<String>> entry : db.entrySet()) {
                        ArrayList<String> dates = entry.getValue();
                        StringBuilder st = new StringBuilder(entry.getKey());
                        for (int i = 0; i < dates.size(); i++)
                            st.append(delim).append(dates.get(i));
                        st.append("\r\n");
                        file.write(st.toString());
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

    public ArrayList<String> getDates(String filename) {
        return db.get(filename);
    }

    public void addFileAndDate(String filename, String date) {
        ArrayList<String> dates = getDates(filename);
        if (dates == null)
            dates = new ArrayList<>();
        dates.add(date);
        db.put(filename, dates);
    }
    
    public void removeFileAndDate(String filename){
    	//TODO
    }
}
