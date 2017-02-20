package SDIS;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

public class Client {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <port_number> <oper> <platenumber> [ownername]");
			return;
		}
		
		//Verify oper
		if(!Objects.equals(args[2], "lookup") && !Objects.equals(args[2], "register"))
		{
			System.out.println("Operation should be [lookup | register]");
			return;
		}
		
		Plate plate;
		Plate plateReceived;
		
		if(args.length == 4) {
			plate = new Plate(args[3], "InsertOwner");
		} else if(args.length == 5) {
			plate = new Plate(args[3], args[4]);
		} else {
			//TODO error. Add a exception
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
			/*
			DatagramPacket msgToSend = new DatagramPacket(data, data.length, address, serverPort);
			String str = args[2];
			for(int i = 3; i < args.length; i++)
				str += " " + args[i];
			msgToSend.setData(str.getBytes());*/
			
			DatagramPacket msgToSend = plate.toDatagramPacket(address, serverPort);
			
			socket.send(msgToSend);

			DatagramPacket msgReceived = new DatagramPacket(data, data.length, address, serverPort);
			socket.receive(msgReceived);
			
			plateReceived = new Plate(msgReceived);
			if(args.length == 4) {
				System.out.println(plateReceived.getPlateNumber() + " belongs to " + plateReceived.getOwnerName());
			} else if(args.length == 5) {
				//In this case, the owner name carries the server acknowledge/error message
				System.out.println(plateReceived.getOwnerName());
			} else {
				//TODO error. Add an exception
				return;
			}

			socket.close();
			
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout reached");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
