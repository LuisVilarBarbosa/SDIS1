package Project1.Server;

/* Generic received message: REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

public class ServerSpaceReclaiming {

    public static void updateStorageSpace(String protocolVersion, int serverId, Multicast mControlCh, long newStorageSpace) {

    }

    public static void monitorStorageSpaceChanges(String protocolVersion, int serverId, Multicast mControlCh) {
        while(true) {

        }
    }
}
