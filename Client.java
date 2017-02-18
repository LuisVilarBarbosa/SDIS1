import java.io.IOException;
import java.net.*;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16


	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd> [opnd]");
			return;
		}

		String hostName = args[0];
		int serverPort = Integer.parseInt(args[1]);

		DatagramSocket socket;
		DatagramPacket msgReceived;
		DatagramPacket msgToSend;
		try {
			socket = new DatagramSocket();
			InetAddress address = InetAddress.getByName(hostName);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			msgToSend = new DatagramPacket(data, data.length, address, serverPort);
			String str = args[2];
			for(int i = 3; i < args.length; i++)
				str += " " + args[i];
			msgToSend.setData(str.getBytes());
			socket.send(msgToSend);

			//msgReceived = new DatagramPacket(data, data.length, address, serverPort);

			socket.close();
			//socket·;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
