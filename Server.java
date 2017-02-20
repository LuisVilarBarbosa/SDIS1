package SDIS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: java Server <port_number>");
			return;
		}

		Plate[] plateList;
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
				String response = "Received the message: " + msgText;
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
