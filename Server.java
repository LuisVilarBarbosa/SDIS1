package SDIS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: java Server <port_number> <mcast_addr> <mcast_port>");
			return;
		}

		ArrayList<Plate> plateList = new ArrayList<>();
		int serverPort = Integer.parseInt(args[0]);

		try {
			MulticastSocket socket = new MulticastSocket();
			socket.setTimeToLive(1); //To avoid network congestion
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			while(true) {
				//Prepare the multicast message to diffuse
				InetAddress multicastAddress = InetAddress.getByName(args[1]);
				int multicastPort = Integer.parseInt(args[2]);
				DatagramPacket msgToDiffuse = new DatagramPacket(data, data.length, multicastAddress, multicastPort);
				
				StringBuilder mcastMessage = new StringBuilder("multicast:");
				mcastMessage.append(args[1]).append(" ").append(args[2]).append(":");
				mcastMessage.append(InetAddress.getLocalHost().getHostAddress()).append(" ").append(args[0]);
				
				
				
				msgToDiffuse.setData(mcastMessage.toString().getBytes());
				socket.send(msgToDiffuse);
				
				System.out.println(mcastMessage.toString());
				
				//Just for avoiding overflood
				Thread.sleep(1000);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
