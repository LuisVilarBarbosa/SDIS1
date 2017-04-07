package Project1.Server;

/* Generic received message: REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */

import Project1.Database.ServerDatabase;

public class ServerSpaceReclaiming {

    public static void updateStorageSpace(ServerObject serverObject, long newStorageSpace) {

    }

    public static void monitorStorageSpaceChanges(String protocolVersion, int serverId, Multicast mControlCh, ServerDatabase db) {
        while(true) {

        }
    }
}
