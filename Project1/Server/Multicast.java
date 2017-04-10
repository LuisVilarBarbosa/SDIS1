package Project1.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;

public class Multicast {
    public static final int timeToLive = 1;
    public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
    private MulticastSocket socket;
    private String multicastAddress;
    private int multicastPort;

    public Multicast(String groupAddress, int groupPort) {
        try {
            socket = new MulticastSocket(groupPort);
            socket.joinGroup(InetAddress.getByName(groupAddress));
            socket.setTimeToLive(timeToLive);    //To avoid network congestion
            multicastAddress = groupAddress;
            multicastPort = groupPort;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] message) {
        try {
            if (message.length > UDP_DATAGRAM_MAX_LENGTH)
                throw new IllegalArgumentException("Message too big.");
            DatagramPacket msgToDiffuse = new DatagramPacket(message, message.length, InetAddress.getByName(multicastAddress), multicastPort);
            msgToDiffuse.setData(message);
            socket.send(msgToDiffuse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive() {
        byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
        try {
            DatagramPacket mcastReceive = new DatagramPacket(data, data.length);
            socket.receive(mcastReceive);
            data = Arrays.copyOfRange(mcastReceive.getData(), 0, mcastReceive.getLength());
            return data;
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] receive(int blockingTime) throws SocketException {
        socket.setSoTimeout(blockingTime);
        byte[] data = receive();
        socket.setSoTimeout(0);
        return data;
    }

    public void close() {
        socket.close();
    }

    public Multicast clone() {
        return new Multicast(multicastAddress, multicastPort);
    }
}
