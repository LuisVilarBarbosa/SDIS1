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

			//TODO Modificar a chamada dos DatagramPacket para incluir address 
			msgReceived = new DatagramPacket(new byte[UDP_DATAGRAM_MAX_LENGTH], UDP_DATAGRAM_MAX_LENGTH);
			msgToSend = new DatagramPacket(new byte[UDP_DATAGRAM_MAX_LENGTH], UDP_DATAGRAM_MAX_LENGTH);

			String str = args[2];
			for(int i = 3; i < args.length; i++)
				str += " " + args[i];
			msgToSend.setData(str.getBytes());

			socket.send(msgToSend);
			socket.close();
			//socket·;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
