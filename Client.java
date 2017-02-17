import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16


	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5)
		{
			System.out.println("Usage: java Client <hostname> <portnumber> <operation> <operands> [operand]");
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
			
			//TEMPORARY
			String str = args[2];
			for(int i = 3; i < args.length; i++)
			{
				str += " " + args[i];
				
			}
			msgToSend.setData(str.getBytes());
			
			socket.send(msgToSend);
			socket.close();
			//socket·;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
