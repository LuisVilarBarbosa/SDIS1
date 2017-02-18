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

		int serverPort = Integer.parseInt(args[0]);

		DatagramSocket socket;
		DatagramPacket msgReceived;
		DatagramPacket msgToSend;
		try {
			socket = new DatagramSocket(serverPort);
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];

			while(true) {
				msgReceived = new DatagramPacket(data, data.length);
				socket.receive(msgReceived);

				//Processing the message
				System.out.println(msgReceived.getData());

				msgToSend = new DatagramPacket(data, data.length);
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
