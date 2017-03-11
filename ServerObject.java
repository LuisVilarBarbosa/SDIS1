package SDIS;

import java.util.ArrayList;

public class ServerObject implements ServerRMI {
	
	private ArrayList<Plate> plateList = new ArrayList<>();
	
	public String register(String plateNumber, String ownerName) {
		String response;
		Plate p = new Plate(plateNumber, ownerName);
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

		StringBuilder sb = new StringBuilder("register ");
		sb.append(plateNumber).append(" ").append(ownerName).append(" : ").append(response);
		System.out.println(sb);

		return response;
	}

	public String lookup(String plateNumber) {
		String response = "NOT_FOUND";
		for(Plate p : plateList) {
			if(p.getPlateNumber().equals(plateNumber))
				response = p.getOwnerName();
		}

		StringBuilder sb = new StringBuilder("lookup ");
		sb.append(plateNumber).append(" : ").append(response);
		System.out.println(sb);

		return response;
	}
}
