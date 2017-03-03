package SDIS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

	private static void requestsProcessor(int serverPort) {
		ArrayList<Plate> plateList = new ArrayList<>();

		try {
			ServerSocket receivingSocket = new ServerSocket(serverPort);

			while(true) {
				Socket connectionSocket = receivingSocket.accept();
				connectionSocket.setSoTimeout(3000); //3 sec
				InputStreamReader istreamReader = new InputStreamReader(connectionSocket.getInputStream());
				BufferedReader reader = new BufferedReader(istreamReader);
				String msgRcvText = reader.readLine();
				System.out.println("Socket read");

				//Prepare the response
				String response = "";

				String splittedMsg[] = msgRcvText.split(" ");
				String oper = splittedMsg[0];

				if(oper.equalsIgnoreCase("register") && splittedMsg.length == 3) {
					String plateNumber = splittedMsg[1];
					StringBuilder ownerName = new StringBuilder(splittedMsg[2]);
					for (int i = 3; i < splittedMsg.length; i++)
						ownerName.append(" ").append(splittedMsg[i]);
					Plate p = new Plate(plateNumber, ownerName.toString());
					if(plateList.contains(p)) {
						response = "-1 \nALREADY EXISTS";
					}
					else if(p.getPlateNumber().equalsIgnoreCase("INVALID")) {
						response = "-1 \nINVALID PLATE. Format XX-XX-XX. X = [A-Z0-9]";
					}
					else {
						plateList.add(p);
						response = Integer.toString(plateList.size());
					}
				}
				else if(oper.equalsIgnoreCase("lookup") && splittedMsg.length == 2) {
					String plateNumber = splittedMsg[1];
					boolean found = false;
					for(Plate p : plateList) {
						if(p.getPlateNumber().equals(plateNumber)) {
							found = true;
							response = p.getOwnerName();
						}
					}
					if(!found)
						response = "NOT_FOUND";
				}
				else {
					continue;	// Ignore malformed messages
				}

				OutputStream ostream = connectionSocket.getOutputStream();
				PrintWriter prtWriter = new PrintWriter(ostream, true); //True for flushing the buffer
				prtWriter.println(response);
				System.out.println("Socket wrote");
				istreamReader.close();
				ostream.close();
				connectionSocket.close();
				System.out.println(msgRcvText + " : " + response);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: java Server <srvc_port>");
			return;
		}

		int servicePort = Integer.parseInt(args[0]);
		
		Timer timerReq = new Timer("requestProcessor");

		TimerTask t2 = new TimerTask() {
			@Override
			public void run() {
				requestsProcessor(servicePort);
			}
		};
		
		timerReq.schedule(t2, 0);
	}

}
