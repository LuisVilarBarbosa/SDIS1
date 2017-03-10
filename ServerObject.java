package SDIS;

import java.util.ArrayList;

public class ServerObject {
	
	private ArrayList<Plate> plateList = new ArrayList<>();
	
	public String register(String ownerName, String plateNumber) {
		String response;
		Plate p = new Plate(plateNumber, ownerName.toString());
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
		System.out.println(response);
		return response;
	}

	public String lookup(String plateNumber) {
		String response = "NOT_FOUND";
		for(Plate p : plateList) {
			if(p.getPlateNumber().equals(plateNumber))
				response = p.getOwnerName();
		}
		System.out.println(response);
		return response;
	}
}
