package SDIS;

import java.io.IOException;
import java.net.*;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <mcast_addr> <mcast_port> <oper> <platenumber> [ownername]");
			return;
		}
/*
		//Verify oper
		String oper = args[2];
		if((!oper.equalsIgnoreCase("register") && args.length == 5) ||
				(!oper.equalsIgnoreCase("lookup") && args.length == 4)) {
			System.out.println("Operation should be [register | lookup] with [5 | 4] arguments");
			return;
		}
*/
		String groupAddress = args[0];
		int groupPort = Integer.parseInt(args[1]);
/*
		// Build majority of output response
		StringBuilder response = new StringBuilder();
		for(int i = 2; i < args.length; i++)
			response.append(args[i]).append(" ");
		response.append(": ");
*/
		try {
			MulticastSocket socket = new MulticastSocket(groupPort);
			socket.joinGroup(InetAddress.getByName(groupAddress));
			socket.setSoTimeout(3000); //3 sec
			socket.setTimeToLive(1);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket mcastReceive = new DatagramPacket(data, data.length);
			socket.receive(mcastReceive);
			String msgMulticastReceived = new String(data, 0, mcastReceive.getLength());
			System.out.println(msgMulticastReceived);
			socket.close();
			
			
			//String treatment
			String[] filter = msgMulticastReceived.split(":"); //remove "multicast:"
			String addressAndPort = filter[1];
			filter = addressAndPort.split(" "); //Separate Address from Port
			
			
			DatagramSocket serverConnectionSocket = new DatagramSocket();
			serverConnectionSocket.setSoTimeout(3000); //3 sec
			InetAddress serverAddress = InetAddress.getByName(filter[0]);
			int serverPort = Integer.parseInt(filter[1]);
			
			//Construct message
			DatagramPacket msgToSend = new DatagramPacket(data, data.length, serverAddress, serverPort);
			StringBuilder sb = new StringBuilder(args[2]);
			for(int i = 3; i < args.length; i++)
				sb.append(" ").append(args[i]);
			msgToSend.setData(sb.toString().getBytes());
			
			serverConnectionSocket.send(msgToSend);
			
			DatagramPacket msgReceived = new DatagramPacket(data, data.length, serverAddress, serverPort);
			serverConnectionSocket.receive(msgReceived);
			String msgText = new String(msgReceived.getData(), 0, msgReceived.getLength());
			System.out.println(msgText);
			
			serverConnectionSocket.close();
		} catch (SocketTimeoutException e) {
			//response.append("ERROR");
			System.out.println("Timeout");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println(response);
	}

}
