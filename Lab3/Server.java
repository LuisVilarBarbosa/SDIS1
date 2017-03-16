import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private static ArrayList<Plate> plateList = new ArrayList<>();

    private static void dispatcher(int serverPort) {
        try {
            ServerSocket receivingSocket = new ServerSocket(serverPort);
            while (true) {
                Socket connectionSocket = receivingSocket.accept();
                Timer timerReq = new Timer();
                TimerTask t = new TimerTask() {
                    @Override
                    public void run() {
                        requestsProcessor(connectionSocket);
                    }
                };
                timerReq.schedule(t, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Prevent process lock if a TimerTask is running
            System.exit(1);
        }
    }

    private static void requestsProcessor(Socket connectionSocket) {
        try {
            connectionSocket.setSoTimeout(3000); //3 sec
            InputStreamReader istreamReader = new InputStreamReader(connectionSocket.getInputStream());
            BufferedReader reader = new BufferedReader(istreamReader);
            String msgRcvText = reader.readLine();

            //Prepare the response
            String response = "";

            String splittedMsg[] = msgRcvText.split(" ");
            String oper = splittedMsg[0];

            if (oper.equalsIgnoreCase("register") && splittedMsg.length == 3) {
                String plateNumber = splittedMsg[1];
                StringBuilder ownerName = new StringBuilder(splittedMsg[2]);
                for (int i = 3; i < splittedMsg.length; i++)
                    ownerName.append(" ").append(splittedMsg[i]);
                Plate p = new Plate(plateNumber, ownerName.toString());
                if (plateList.contains(p)) {
                    response = "-1 \nALREADY EXISTS";
                } else if (p.getPlateNumber().equalsIgnoreCase("INVALID")) {
                    response = "-1 \nINVALID PLATE. Format XX-XX-XX. X = [A-Z0-9]";
                } else {
                    plateList.add(p);
                    response = Integer.toString(plateList.size());
                }
            } else if (oper.equalsIgnoreCase("lookup") && splittedMsg.length == 2) {
                String plateNumber = splittedMsg[1];
                boolean found = false;
                for (Plate p : plateList) {
                    if (p.getPlateNumber().equals(plateNumber)) {
                        found = true;
                        response = p.getOwnerName();
                    }
                }
                if (!found)
                    response = "NOT_FOUND";
            } else {
                // Ignore malformed messages
            }

            OutputStream ostream = connectionSocket.getOutputStream();
            PrintWriter prtWriter = new PrintWriter(ostream, true); //True for flushing the buffer
            prtWriter.println(response);

            istreamReader.close();
            ostream.close();
            connectionSocket.close();
            System.out.println(msgRcvText + " : " + response);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <srvc_port>");
            return;
        }

        int servicePort = Integer.parseInt(args[0]);
        dispatcher(servicePort);
    }

}
