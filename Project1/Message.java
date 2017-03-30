import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.InvalidAttributesException;

/* Generic message: <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> */

public class Message {
    private static final String genericErrorMsg = "Invalid message.";
    private static final int SM_START = 0;
    private static final int SM_CR1 = 1;
    private static final int SM_LF1 = 2;
    private static final int SM_CR2 = 3;
    private static final int SM_LF2 = 4;
    
    public static final int CHUNK_MAX_SIZE = 64000;
    
    private String messageType; //8 characters
    private String version; // <n>.<m> -> 3 characters
    private String senderId;    // Not negative integer
    private String fileId;  // SHA256 ASCII string
    private String chunkNo; // Not negative integer not larger than 6 chars
    private String replicationDeg;  // Number in [1,9]
    private byte[] header;
    private byte[] body;
    private int header_size;
    private int max_body_size = 64000 - header_size;

    public Message(byte[] message) {
        byte[][] msgSplitted = splitHeaderFromBody(message);
        String msgStr = new String(msgSplitted[0]);
        body = msgSplitted[1];

        Pattern p = Pattern.compile("^\\s*(\\w+)\\s+(\\d.\\d)\\s+(\\d+)\\s+(\\w{64})(?:\\s+(\\d{1,6})(?:\\s+(\\d+))?)?\\s*$");
        Matcher m = p.matcher(msgStr);

        if (!m.matches())
            throw new IllegalArgumentException(genericErrorMsg);

        messageType = m.group(1);
        version = m.group(2);
        senderId = m.group(3);
        fileId = m.group(4);
        chunkNo = m.group(5);
        replicationDeg = m.group(6);

        if (Integer.parseInt(senderId) < 0)
            throw new IllegalArgumentException("Invalid senderId (must be not negative).");
        if (chunkNo != null && Integer.parseInt(chunkNo) < 0)
            throw new IllegalArgumentException("Invalid chunkNo (must be not negative).");
        if (replicationDeg != null) {
            int repDeg = Integer.parseInt(replicationDeg);
            if (repDeg < 1 || repDeg > 9)
                throw new IllegalArgumentException("Invalid replicationDeg (must be in [1,9]).");
        }
    }

    //Construct a PUTCHUNK message to send
    public Message(ServerObject serverInfo, String fileId, int chunkNumber, int replicationDegree){
    	this.messageType = "PUTCHUNK";
    	
    	setVersion(serverInfo.getProtocolVersion());
    	setSenderId(serverInfo.getServerId());
    	setRepDegree(replicationDegree);
    	setChunkNo(chunkNumber);
    	this.fileId = fileId;
    }
    
    public int setBodyData(byte[] data) {
    	if(data.length > this.max_body_size)
    		throw new ArrayStoreException("Invalid store: Data does not fit in the chunk body");
    	this.body = data;
    	
    	if(data == null)
    		return 0;
    	else
    		return data.length;
    }
    
    public void setMessageType(MESSAGE_TYPE mt) {
    	String type = messageTypeToString(mt);
    	if(!type.equals("error"))
    		messageType = type;
    }
    
    public String getMessageType() {
        return messageType;
    }

    public String getVersion() {
        return version;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getChunkNo() {
        return chunkNo;
    }

    public String getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

    private byte[][] splitHeaderFromBody(byte[] message) {
        byte[] header, body;
        int pos = 0, state = SM_START;

        while (state != SM_LF2 && pos < message.length) {
            byte b = message[pos];
            switch (state) {
                case SM_START:
                    if (b == '\r') state = SM_CR1;
                    //else state = SM_START;
                    break;
                case SM_CR1:
                    if (b == '\n') state = SM_LF1;
                    else state = SM_START;
                    break;
                case 2:
                    if (b == '\r') state = SM_CR2;
                    else state = SM_START;
                    break;
                case 3:
                    if (b == '\n') state = SM_LF2;
                    else state = SM_START;
                    break;
            }
            pos++;
        }

        if (state == SM_LF2) {
            header = Arrays.copyOfRange(message, 0, pos - SM_LF2);
            body = Arrays.copyOfRange(message, pos, message.length);
        } else if (state == SM_LF1) {
            header = Arrays.copyOfRange(message, 0, pos - SM_LF1);
            body = new byte[0];
        } else
            throw new IllegalArgumentException(genericErrorMsg);

        return new byte[][]{header, body};
    }
    
    public int generateHeader() throws InvalidAttributesException{
    	checkAttributes();
    	
    	StringBuilder messageBuilder = new StringBuilder();
    	messageBuilder.append(messageType).append(' ')
    	.append(version).append(' ')
    	.append(senderId).append(' ')
    	.append(fileId).append(' ')
    	.append(chunkNo).append(' ')
    	.append(replicationDeg).append(' ')
    	.append("/n/n");
    	
    	String header = messageBuilder.toString();
    	this.header = header.getBytes();
    	this.header_size = this.header.length;
    	this.max_body_size = CHUNK_MAX_SIZE - this.header_size;
    	
    	return this.max_body_size;
    }
    
    private void checkAttributes() throws InvalidAttributesException{
    	//TODO improve this verification. Before generating, the fields may be altered and become invalid
    	if(this.messageType == null)
    		throw new InvalidAttributesException("Invalid message type: " + this.messageType);
    	else if(this.version == null)
    		throw new InvalidAttributesException("Invalid protocol version: " + this.version);
    	else if(this.senderId == null)
    		throw new InvalidAttributesException("Invalid senderId: " + this.senderId);
    	else if(this.fileId == null)
    		throw new InvalidAttributesException("Invalid fileId: " + this.fileId);
    	else if(this.chunkNo == null)
    		throw new InvalidAttributesException("Invalid chunk number: " + this.chunkNo);
    	else if(this.replicationDeg == null)
    		throw new InvalidAttributesException("Invalid replication degree: " + this.replicationDeg);
    }
    public String messageTypeToString(MESSAGE_TYPE mt) {
    	switch(mt){
    	case PUTCHUNK:
    		return "PUTCHUNK";
    	case CHUNK:
    		return "CHUNK";
    	case DELETE:
    		return "DELETE";
    	case GETCHUNK:
    		return "GETCHUNK";
    	case REMOVED:
    		return "REMOVED";
    	case STORED:
    		return "STORED";
    	default:
    		return "error"; //TODO throw exception??
    	}
    }
    
    private boolean lessThan6Chars(int number){
    	if((number / 1000000) != 0)
    		return false;
    	else
    		return true;
    }
    
    private void setVersion(String version) {
    	if(version.length() != 3)
    		throw new IllegalArgumentException("Invalid protocol version string length (must be <digit>.<digit>)");
    	else if(!Character.isDigit(version.charAt(0)) || !Character.isDigit(version.charAt(2)) || version.charAt(1) != '.')
    		throw new IllegalArgumentException("Invalid protocol version (must be <digit>.<digit>)");
    	else
    		this.version = version;
    }
    private void setSenderId(int senderId) {    	
    	if(senderId < 0)
            throw new IllegalArgumentException("Invalid senderId (must be not negative).");
    	else
    		this.senderId = Integer.toString(senderId);
    }
    
    private void setRepDegree(int replicationDegree){
    	if(replicationDegree < 1 || replicationDegree > 9)
    		throw new IllegalArgumentException("Invalid replicationDeg (must be in [1,9]).");
    	else
    		this.replicationDeg = Integer.toString(replicationDegree);
    }
    
    private void setChunkNo(int chunkNumber){
    	if(chunkNumber < 0)
    		throw new IllegalArgumentException("Invalid chunkNo (must be not negative).");
    	else if(!lessThan6Chars(chunkNumber))
    		throw new IllegalArgumentException("Invalid chunkNo (must have 6 or less digits).");
    	else
    		this.chunkNo = Integer.toString(chunkNumber);
    }
    
    public byte[] generateByteArray(){
    	byte[] c = 	new byte[header.length + body.length];
    	System.arraycopy(header, 0, c, 0, header.length);
    	System.arraycopy(body, 0, c, header.length, body.length);
    	
    	return c;
    }
}
