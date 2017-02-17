import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
	
	public static void main(String[] args) {
		
		if(args.length != 1)
		{
			System.out.println("Usage: java Server <port number>");
			return;
		}
		
		int serverPort = Integer.parseInt(args[0]);
		
		DatagramSocket socket;
		DatagramPacket msgReceived;
		DatagramPacket msgToSend;
		try {
			socket = new DatagramSocket(serverPort);
			
			while(true)
			{
				msgReceived = new DatagramPacket(new byte[UDP_DATAGRAM_MAX_LENGTH], UDP_DATAGRAM_MAX_LENGTH);
				msgToSend = new DatagramPacket(new byte[UDP_DATAGRAM_MAX_LENGTH], UDP_DATAGRAM_MAX_LENGTH);
						
				socket.receive(msgReceived);
				
				//Processing the message
				System.out.println(msgReceived.getData());
			}
			
			//socket·;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			//TODO socket.close();
		}

	}

}
