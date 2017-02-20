package SDIS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plate {
	public static final String platePatternExpression = "^([0-9A-Z]{2}-){2}([0-9A-Z]{2})\b";
	
	private String plateNumber;
	private String ownerName;
	
	public String getPlateNumber() {
		return plateNumber;
	}
	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public boolean validatePlate(String plateNumber)
	{
		Pattern platePattern = Pattern.compile(platePatternExpression);
		Matcher m = platePattern.matcher(plateNumber);
		
		return m.find();
	}
	
	public Plate(String plateNumber, String ownerName)
	{
		this.ownerName = ownerName;
		if(validatePlate(plateNumber))
			this.plateNumber = plateNumber;
		else
			this.plateNumber = "INVALID";
	}
}
