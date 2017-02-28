package SDIS;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.TimerTask;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
	
	private static MulticastSocket generateSocket() throws IOException {
		MulticastSocket socket = new MulticastSocket();
		socket.setTimeToLive(1); //To avoid network congestion
		return socket;
	}
	
	private static void advertiser(int servicePort, InetAddress multicastAddress, int multicastPort) {
		try {
			//Prepare the multicast message to diffuse
			StringBuilder mcastMessage = new StringBuilder("multicast:");
			mcastMessage.append(InetAddress.getLocalHost().getHostAddress()).append(" ").append(servicePort);
			String mcastMsg = mcastMessage.toString();

			MulticastSocket socket = generateSocket();
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket msgToDiffuse = new DatagramPacket(data, data.length, multicastAddress, multicastPort);
			msgToDiffuse.setData(mcastMsg.getBytes());

			while(true) {
				socket.send(msgToDiffuse);
				System.out.println(mcastMsg);
				//Just for avoiding overflood
				Thread.sleep(1000);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void requestsProcessor() {
		ArrayList<Plate> plateList = new ArrayList<>();

		try {
			MulticastSocket socket = generateSocket();
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			while(true) {

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
			return;
		}

		try {
			int servicePort = Integer.parseInt(args[0]);
			InetAddress multicastAddress = InetAddress.getByName(args[1]);
			int multicastPort = Integer.parseInt(args[2]);

			TimerTask t1 = new TimerTask() {
				@Override
				public void run() {
					advertiser(servicePort, multicastAddress, multicastPort);
				}
			};
			t1.run();

			TimerTask t2 = new TimerTask() {
				@Override
				public void run() {
					requestsProcessor();
				}
			};
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
