package SDIS;

import java.io.IOException;
import java.net.*;
import java.rmi.Remote;
import java.rmi.registry.Registry;

public class Client {

	public static void main(String[] args) {
		if(args.length != 4 && args.length != 5) {
			System.out.println("Usage: java Client <host_name> <remote_object_name> <oper> <platenumber> [ownername]");
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
		String remoteObjName = args[1];

		// Build majority of output response
		StringBuilder response = new StringBuilder();
		for(int i = 2; i < args.length; i++)
			response.append(args[i]).append(" ");
		response.append(": ");

		try {
			Registry r = java.rmi.registry.LocateRegistry.getRegistry();
			Remote serverObj= r.lookup(remoteObjName);
			((ServerObject)serverObj).lookup(plateNumber); //TODO
		} catch (SocketTimeoutException e) {
			response.append("ERROR");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(response);
	}

}
