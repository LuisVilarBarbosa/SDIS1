import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Generic message: <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> */

public class Message {
    String messageType;
    String version; // <n>.<m> -> 3 characters
    String senderId;    // Not negative integer
    String fileId;  // SHA256 ASCII string
    String chunkNo; // Not negative integer not larger than 6 chars
    String replicationDeg;  // Number in [1,9]
    // byte[] body; // the body of the message -> to implement

    public Message(String message) {
        Pattern p = Pattern.compile("^\\s*(\\w+)\\s+(\\d.\\d)\\s+(\\d+)\\s+(\\w{64})\\s+(\\d{1,6})\\s+(\\d+)\\s*$");   // implement "\r\n\r\n" verification
        Matcher m = p.matcher(message);

        if(!m.matches())
            throw new IllegalArgumentException("Invalid message.");

        messageType = m.group(1);
        version = m.group(2);
        senderId = m.group(3);
        fileId = m.group(4);
        chunkNo = m.group(5);
        replicationDeg = m.group(6);

        if(Integer.parseInt(senderId) < 0) throw new IllegalArgumentException("Invalid senderId (must be not negative).");
        if(Integer.parseInt(chunkNo) < 0) throw new IllegalArgumentException("Invalid chunkNo (must be not negative).");
        int repDeg = Integer.parseInt(replicationDeg);
        if(repDeg < 1 || repDeg > 9) throw new IllegalArgumentException("Invalid replicationDeg (must be in [1,9]).");
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
}
