package SDIS;

import java.io.IOException;
import java.net.*;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <port_number> <oper> <platenumber> [ownername]");
			return;
		}

		//Verify oper
		String oper = args[2];
		if((!oper.equalsIgnoreCase("register") && args.length == 5) ||
				(!oper.equalsIgnoreCase("lookup") && args.length == 4)) {
			System.out.println("Operation should be [register | lookup] with [5 | 4] arguments");
			return;
		}

		String hostName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		// Build majority of output response
		StringBuilder response = new StringBuilder();
		for(int i = 2; i < args.length; i++)
			response.append(args[i]).append(" ");
		response.append(": ");

		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(3000); //3 sec
			InetAddress address = InetAddress.getByName(hostName);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			//Construct message
			DatagramPacket msgToSend = new DatagramPacket(data, data.length, address, serverPort);
			StringBuilder sb = new StringBuilder(args[2]);
			for(int i = 3; i < args.length; i++)
				sb.append(" ").append(args[i]);
			msgToSend.setData(sb.toString().getBytes());

			socket.send(msgToSend);

			DatagramPacket msgReceived = new DatagramPacket(data, data.length, address, serverPort);
			socket.receive(msgReceived);
			String msgText = new String(msgReceived.getData(), 0, msgReceived.getLength());
			response.append(msgText);

			socket.close();
			//socket·;
		} catch (SocketTimeoutException e) {
			response.append("ERROR");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(response);
	}

}
