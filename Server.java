package SDIS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
	
	public static Plate[] platesList;

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: java Server <port_number>");
			return;
		}

		int serverPort = Integer.parseInt(args[0]);

		try {
			DatagramSocket socket = new DatagramSocket(serverPort);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			while(true) {
				DatagramPacket msgReceived = new DatagramPacket(data, data.length);
				socket.receive(msgReceived);

				//Processing the message
				/*String msgText = new String(msgReceived.getData(), 0, msgReceived.getLength());
				System.out.println(msgText);*/
				Plate plateReceived = new Plate(msgReceived);
				if(Objects.equals("InsertOwner", plateReceived.getOwnerName())) {
					//Modificar o plateReceived para ter o nome do owner
				}
				//TODO else if

				//Prepare the response
				InetAddress clientAddress = msgReceived.getAddress();
				int clientPort = msgReceived.getPort();
				DatagramPacket msgToSend = new DatagramPacket(data, data.length, clientAddress, clientPort);
				/*String response = "Received the message: " + msgText;
				msgToSend.setData(response.getBytes());*/
				//TODO Send a Plate as response
				
				socket.send(msgToSend);
			}

			//socket·;
			//socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Plate findPlate(String plateNumber)
	{
		for(int i = 0; i < platesList.length; i++)
		{
			if(Objects.equals(platesList[i].getPlateNumber(), plateNumber)) {
				return platesList[i];
			}
		}
		
		return null;
	}

}
