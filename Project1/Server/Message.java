package Project1.Server;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/* Generic message: <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> */

public class Message {
    private static final String genericErrorMsg = "Invalid message.";
    private static final int SM_START = 0;
    private static final int SM_CR1 = 1;
    private static final int SM_LF1 = 2;
    private static final int SM_CR2 = 3;
    private static final int SM_LF2 = 4;

    private String messageType; //8 characters
    private String version; // <n>.<m> -> 3 characters
    private String senderId;    // Not negative integer
    private String fileId;  // SHA256 ASCII string
    private String chunkNo; // Not negative integer not larger than 6 chars
    private String replicationDeg;  // Number in [1,9]
    private String header;
    private byte[] body;

    public Message(byte[] message) {
        byte[][] msgSplitted = splitHeaderFromBody(message);
        header = new String(msgSplitted[0]);
        body = msgSplitted[1];

        Pattern p = Pattern.compile("^\\s*(\\w+)\\s+(\\d.\\d)\\s+(\\d+)\\s+(\\w{64})(?:\\s+(\\d{1,6})(?:\\s+(\\d+))?)?\\s*$");
        Matcher m = p.matcher(header);

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

    public String getHeader() {
        return header;
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
}
