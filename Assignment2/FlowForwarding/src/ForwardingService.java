import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class ForwardingService extends Node {
    static final int APPLICATION_PORT = 50000;
    static final int SERVICE_PORT = 51510;
    static final int ROUTER_PORT = 51510;

    static final int TYPE = 0;
    static final int LENGTH = 1;
    static final int HEADER_LENGTH = 2;

    static final byte ENDPOINT_ONE = 0;
    static final byte ENDPOINT_TWO = 1; 
    static final byte ROUTER_ONE = 1;
    static final byte ROUTER_TWO = 2;
    static final byte ROUTER_THREE = 3;
    static final byte ERROR = 5; //for now
    static final byte ACK = 6;

    static final int ACKCODE = 1;
    static final byte ACKPACKET = 10;

    private InetSocketAddress router;
    private InetSocketAddress application;

    ForwardingService(int port) {
        try {
            socket = new DatagramSocket(port);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data;
            String content;
            data = packet.getData();
            switch(data[TYPE]) {
                case ACK: 
                    System.out.println("Packet Received");
                    break;
                case ENDPOINT_ONE:
                    content = sendAck(packet, data);
                    router = new InetSocketAddress("RouterOne", ROUTER_PORT);
                    application = new InetSocketAddress("EndpointTwo", packet.getPort());
                    System.out.println("EndpointOne");
                    packet.setSocketAddress(router);
                    socket.send(packet);
                    break;
                case ENDPOINT_TWO: 
                    content = sendAck(packet, data);
                    router = new InetSocketAddress("RouterThree", ROUTER_PORT);
                    application = new InetSocketAddress("EndpointOne", packet.getPort());
                    System.out.println("EndpointTwo");
                    packet.setSocketAddress(router);
                    socket.send(packet);
                    break;
                case ROUTER_TWO:
                    content = sendAck(packet, data);
                    System.out.println("RouterOne");
                    sendPacket(ENDPOINT_TWO, content, application);
                    socket.send(packet);
                    break;
                case ROUTER_THREE:
                    content = sendAck(packet, data);
                    System.out.println("RouterThree");
                    sendPacket(ENDPOINT_ONE, content, application);
                    break;
                case ERROR:
                    content = sendAck(packet, data);
                    System.out.println("Unknown destination in topology, packet dropped");
                    break;
                default:
                    System.err.println("ERROR: Unexpected packet received");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        try {
            this.wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sendAck(DatagramPacket packet, byte[] data) {
        try{
            String content;
            DatagramPacket response;
            byte[] buffer = new byte[data[LENGTH]];
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, data[LENGTH]);
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

    private void sendPacket(byte type, String content, InetSocketAddress dstAddress){
        try {
            byte[] data;
            DatagramPacket packet;
            data = new byte[HEADER_LENGTH + content.length()];
            data[TYPE] = type;
            data[LENGTH] = (byte) content.length();
            System.arraycopy(content.getBytes(), 0, data, HEADER_LENGTH, content.length());
            packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(dstAddress);
            socket.send(packet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ForwardingService fs = new ForwardingService(SERVICE_PORT);
            fs.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}