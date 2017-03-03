package SDIS;

import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <port_number> <oper> <platenumber> [ownername]");
			return;
		}

		//Verify oper
		String oper = args[2];
		if((!oper.equalsIgnoreCase("register") && args.length == 5) ||
				(!oper.equalsIgnoreCase("lookup") && args.length == 4)) {
			System.out.println("Operation should be [register | lookup] with [5 | 4] arguments");
			return;
		}

		String hostName = args[0];
		int hostPort = Integer.parseInt(args[1]);

		// Build majority of output response
		StringBuilder response = new StringBuilder();
		for(int i = 2; i < args.length; i++)
			response.append(args[i]).append(" ");
		response.append(": ");

		try {
			Socket serverConnectionSocket = new Socket(hostName, hostPort);
			serverConnectionSocket.setSoTimeout(3000); //3 sec
			
			//Construct message
			StringBuilder sb = new StringBuilder(args[2]);
			for(int i = 3; i < args.length; i++)
				sb.append(" ").append(args[i]);

			OutputStream ostream = serverConnectionSocket.getOutputStream();
			PrintWriter prtWriter = new PrintWriter(ostream, true); //True for flushing the buffer
			prtWriter.println(sb.toString());

			InputStreamReader istreamReader = new InputStreamReader(serverConnectionSocket.getInputStream());
			BufferedReader reader = new BufferedReader(istreamReader);
			String msgRcvText = reader.readLine();
			response.append(msgRcvText);

			ostream.close();
			istreamReader.close();
			serverConnectionSocket.close();
		} catch (SocketTimeoutException e) {
			response.append("ERROR");
			System.out.println("Timeout");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(response);
	}

}
