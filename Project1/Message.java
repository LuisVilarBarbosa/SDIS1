import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {
    String messageType;
    String version;
    String senderId;
    String fileId;
    String chunkNo;
    String replicationDeg;

    Message(String message) {
        Pattern p = Pattern.compile("\\s*(\\w+)\\s+(\\d+.\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*");   // implement "\r\n\r\n" verification
        Matcher m = p.matcher(message);

        if(!m.matches())
            throw new IllegalArgumentException("Invalid message.");

        messageType = m.group(1);
        version = m.group(2);
        senderId = m.group(3);
        fileId = m.group(4);
        chunkNo = m.group(5);
        replicationDeg = m.group(6);

        if(Integer.getInteger(senderId) < 0) throw new IllegalArgumentException("Invalid senderId (must be not negative).");
        if(Integer.getInteger(fileId) < 0) throw new IllegalArgumentException("Invalid fileId (must be not negative).");
        if(Integer.getInteger(chunkNo) < 0) throw new IllegalArgumentException("Invalid chunkNo (must be not negative).");
        int repDeg = Integer.getInteger(replicationDeg);
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
