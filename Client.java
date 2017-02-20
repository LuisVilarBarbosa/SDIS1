package SDIS;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	private Plate plate;

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd> [opnd]");
			return;
		}
		
		//Verify oper
		if(!Objects.equals(args[2], "lookup") && !Objects.equals(args[2], "register"))
		{
			System.out.println("Operation should be [lookup | register]");
			return;
		}

		String hostName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		try {
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(3000); //3 sec
			InetAddress address = InetAddress.getByName(hostName);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			//Construct message
			DatagramPacket msgToSend = new DatagramPacket(data, data.length, address, serverPort);
			String str = args[2];
			for(int i = 3; i < args.length; i++)
				str += " " + args[i];
			msgToSend.setData(str.getBytes());
			
			socket.send(msgToSend);

			DatagramPacket msgReceived = new DatagramPacket(data, data.length, address, serverPort);
			socket.receive(msgReceived);
			String msgText = new String(msgReceived.getData(), 0, msgReceived.getLength());
			System.out.println(msgText);

			socket.close();
			//socket·;
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout reached");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Plate getPlate() {
		return plate;
	}

	public void setPlate(Plate plate) {
		this.plate = plate;
	}

}
