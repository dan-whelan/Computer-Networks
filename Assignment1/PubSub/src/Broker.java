import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Broker Class for sending and receiving packets to relevant locations on network
 */

public class Broker extends Node {
    static final int BROKER_PORT = 50001;
    static final int SERVER_PORT = 50000;

    static final int HEADER_LENGTH = 2;
    static final int TYPE = 0;
    static final byte TYPE_UNKNOWN =0;
    static final int MESSAGE_LENGTH = 1;

    static final byte BROKER = 1;
    static final byte CLIENT = 2;
    static final byte SERVER = 3;
    static final byte ACK = 4;
    static final byte SUBSCRIBER = 5;

    static final byte ACKPACKET = 10;
    static final int ACKCODE = 1;

    private SocketAddress serverAddress;

    InetSocketAddress dstAddress;

    Broker(int port) {
        try {
            socket = new DatagramSocket(port);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 
     */
    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data;
            String content;
            data = packet.getData();
            switch(data[TYPE]) { 
                case ACK:
                    System.out.print("Packet recieved by Receiver");
                    break;
                case CLIENT:
                    content = sendAck(packet, data);
                    sendPacket(SERVER, content);
                    break;
                case SERVER:
                    content = sendAck(packet, data);
                    sendPacket(SUBSCRIBER, content);
                    break;
                case SUBSCRIBER:
                    content = sendAck(packet, data);
                    sendPacket(SERVER, content);
                    break;
                default:
                    System.err.println("Error: Unexpected packet received");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sending ACK to relevant sender of packet
     * @param packet packet sent
     * @param data data contained in packet
     * @return content of packet
     */
    private String sendAck(DatagramPacket packet, byte[] data) {
        try{
            String content;
            DatagramPacket response;
            byte[] buffer = new byte[data[MESSAGE_LENGTH]];
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, MESSAGE_LENGTH);
            content = new String(buffer);
            data = new byte[HEADER_LENGTH];
            data[TYPE] = ACK;
            data[ACKCODE] = ACKPACKET;
            response = new DatagramPacket(data, data.length);
            response.setSocketAddress(packet.getSocketAddress());
            socket.send(response);
            return content;
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }
     /**
      * sending packet to relevent location
      * @param type location packet is being sent to 
      * @param content content to be contained within the packet
      */
    private void sendPacket(int type, String content) {
        try {
            switch(type) {
                case SERVER:
                    byte[] data = new byte[HEADER_LENGTH + content.length()];
                    data[TYPE] = BROKER;
                    data[MESSAGE_LENGTH] = (byte) content.length();
                    System.arraycopy(content, 0, data, HEADER_LENGTH, content.length());
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    packet.setSocketAddress(serverAddress);
                    socket.send(packet);
                    break;
                case SUBSCRIBER:
                    break;
                default:
                    System.err.println("Error: invalid type");
                    break;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            System.out.println("Waiting for contact...");
            this.wait();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Broker broker = new Broker(BROKER_PORT);
            broker.start();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}