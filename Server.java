package SDIS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: java Server <port_number>");
			return;
		}

		ArrayList<Plate> plateList = new ArrayList<>();
		int serverPort = Integer.parseInt(args[0]);

		try {
			DatagramSocket socket = new DatagramSocket(serverPort);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			while(true) {
				DatagramPacket msgReceived = new DatagramPacket(data, data.length);
				socket.receive(msgReceived);

				//Printing and processing the message
				String msgText = new String(msgReceived.getData(), 0, msgReceived.getLength());
				System.out.println(msgText);

				//Prepare the response
				InetAddress clientAddress = msgReceived.getAddress();
				int clientPort = msgReceived.getPort();
				DatagramPacket msgToSend = new DatagramPacket(data, data.length, clientAddress, clientPort);
				String response = "";

				String splittedMsg[] = msgText.split(" ");
				String oper = splittedMsg[0];

				if(splittedMsg.length < 2) {
					continue;	// Ignore malformed messages
				}
				else if(oper.equalsIgnoreCase("register")) {
					String plateNumber = splittedMsg[1];
					StringBuilder ownerName = new StringBuilder();
					for (int i = 2; i < args.length; i++)
						ownerName.append(args[i]);
					Plate p = new Plate(plateNumber, ownerName.toString());
					if(plateList.contains(p))
						response = "-1";
					else {
						plateList.add(p);
						response = Integer.toString(plateList.size());
					}
				}
				else if(oper.equalsIgnoreCase("lookup")) {
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

				msgToSend.setData(response.getBytes());
				socket.send(msgToSend);
			}

			//socket·;
			//socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
