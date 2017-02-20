package SDIS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
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
		//Evaluates if the Regex expression is valid
		Pattern platePattern = Pattern.compile(platePatternExpression);
		Matcher m = platePattern.matcher(plateNumber);
		
		//Tries to find a match in the string given to the matcher
		return m.find();
	}
	
	public DatagramPacket toDatagramPacket(InetAddress address, int port) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
		objectStream.writeObject(this);
		objectStream.close();
		
		byte[] serializedPlate = byteStream.toByteArray();
		
		return new DatagramPacket(serializedPlate, serializedPlate.length, address, port);
		//TODO Substitute length for UDP_DATAGRAM_MAX_LENGTH??
	}
	
	public Plate(String plateNumber, String ownerName)
	{
		this.ownerName = ownerName;
		
		if(validatePlate(plateNumber))
			this.plateNumber = plateNumber;
		else
			this.plateNumber = "INVALID";
	}
	
	public Plate(DatagramPacket packet) throws IOException, ClassNotFoundException
	{
		ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
		Plate receivedPlate = (Plate) inputStream.readObject();
		inputStream.close();
		
		this.ownerName = receivedPlate.ownerName;
		this.plateNumber = receivedPlate.plateNumber;
		
	}
}
