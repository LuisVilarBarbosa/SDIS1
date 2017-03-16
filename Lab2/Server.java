import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
	
	private static void advertiser(int servicePort, InetAddress multicastAddress, int multicastPort) {
		try {
			//Prepare the multicast message to diffuse
			StringBuilder mcastMessage = new StringBuilder("multicast:");
			mcastMessage.append(InetAddress.getLocalHost().getHostAddress()).append(" ").append(servicePort);
			String mcastMsg = mcastMessage.toString();

			MulticastSocket socket = new MulticastSocket();
			socket.setTimeToLive(1); //To avoid network congestion
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket msgToDiffuse = new DatagramPacket(data, data.length, multicastAddress, multicastPort);
			msgToDiffuse.setData(mcastMsg.getBytes());

			while(true) {
				socket.send(msgToDiffuse);
				System.out.println(mcastMsg);
				//Just to avoid flooding
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
	
	private static void requestsProcessor(int serverPort) {
		ArrayList<Plate> plateList = new ArrayList<>();

		try {
			DatagramSocket socket = new DatagramSocket(serverPort);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket msgReceived = new DatagramPacket(data, data.length);

			System.out.println("Starting receiving messages");

			while(true) {
				socket.receive(msgReceived);
				String msgRcvText = new String(data, 0, msgReceived.getLength());

				//Prepare the response
				InetAddress clientAddress = msgReceived.getAddress();
				int clientPort = msgReceived.getPort();
				DatagramPacket msgToSend = new DatagramPacket(data, data.length, clientAddress, clientPort);
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
				msgToSend.setData(response.getBytes());
				socket.send(msgToSend);
				System.out.println(msgRcvText + " : " + response);
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
			
			Timer timerAdv = new Timer("advertiser");
			Timer timerReq = new Timer("requestProcessor");

			TimerTask t1 = new TimerTask() {
				@Override
				public void run() {
					advertiser(servicePort, multicastAddress, multicastPort);
				}
			};
			TimerTask t2 = new TimerTask() {
				@Override
				public void run() {
					requestsProcessor(servicePort);
				}
			};
			
			timerAdv.schedule(t1, 0);
			timerReq.schedule(t2, 0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
