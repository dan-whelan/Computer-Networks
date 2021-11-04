import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Router extends Node {
    static final int SERVICE_PORT = 51510;
    static final int ROUTER_PORT = 51510;

    static final int TYPE = 0;
    static final int LENGTH = 1;
    static final int HEADER_LENGTH = 2;

    static final byte ENDPOINT_ONE = 0;
    static final byte ENDPOINT_TWO = 1;
    static final byte ERROR = 2; //for now
    static final byte ACK = 3;

    static final int NUMBER_OF_ENDPOINTS = 2;
    static final int INFO_TO_BE_STORED = 3;
    static final int DEST = 0;
    static final int IN = 1;
    static final int OUT = 2;
    static final int ROUTER_ONE = 1;
    static final int ROUTER_TWO = 2;
    static final int ROUTER_THREE = 3;

    static final int ACKCODE = 4;
    static final byte ACKPACKET = 10;

    private int routerNumber;
    private Object[][] forwardingTable;

    Router(int port, int designation) {
        try {
            this.routerNumber = designation;
            socket = new DatagramSocket(port);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data;
            data = packet.getData();
            switch(data[TYPE]) {
                case ENDPOINT_ONE:
                    if(routerNumber == ROUTER_THREE) {
                        String content = sendAck(packet, data);
                        sendPacket((byte)ROUTER_THREE, content, (InetSocketAddress) forwardingTable[ENDPOINT_ONE][OUT]);
                    }
                    else {
                        sendAck(packet, data);
                        packet.setSocketAddress((InetSocketAddress) forwardingTable[ENDPOINT_ONE][OUT]);
                        socket.send(packet);
                    }
                    break;
                case ENDPOINT_TWO:
                    if(routerNumber == ROUTER_ONE) {
                        String content = sendAck(packet, data);
                        sendPacket((byte) ROUTER_ONE, content, (InetSocketAddress) forwardingTable[ENDPOINT_TWO][OUT]);
                    }
                    else {
                        sendAck(packet, data);
                        packet.setSocketAddress((InetSocketAddress) forwardingTable[ENDPOINT_TWO][OUT]);
                        socket.send(packet);
                    }
                    break;
                case ERROR:
                    sendAck(packet, data);
                    System.err.println("Error: packet should have been dropped at forwarding service \ndropping packet now.");
                    break;
                default:
                    System.err.println("Error: Unexpected packet received");

                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        try {
            initialiseForwardingTable();
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

    private void initialiseForwardingTable() {
        forwardingTable = new Object[NUMBER_OF_ENDPOINTS][INFO_TO_BE_STORED];
        forwardingTable[ENDPOINT_ONE][DEST] = ENDPOINT_ONE;
        forwardingTable[ENDPOINT_TWO][DEST] = ENDPOINT_TWO;
        switch(routerNumber) {
            case ROUTER_ONE: 
                forwardingTable[ENDPOINT_ONE][IN] = new InetSocketAddress("forwarding-service", SERVICE_PORT);
                forwardingTable[ENDPOINT_ONE][OUT] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
                forwardingTable[ENDPOINT_TWO][IN] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
                forwardingTable[ENDPOINT_TWO][OUT] = new InetSocketAddress("forwarding-service", SERVICE_PORT);
                break;
            case ROUTER_TWO: 
                forwardingTable[ENDPOINT_ONE][IN] = new InetSocketAddress("RouterOne", ROUTER_PORT);
                forwardingTable[ENDPOINT_ONE][OUT] = new InetSocketAddress("RouterThree", ROUTER_PORT);
                forwardingTable[ENDPOINT_TWO][IN] = new InetSocketAddress("RouterThree", ROUTER_PORT);
                forwardingTable[ENDPOINT_TWO][OUT] = new InetSocketAddress("RouterOne", SERVICE_PORT);
                break; 
            case ROUTER_THREE: 
                forwardingTable[ENDPOINT_ONE][IN] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
                forwardingTable[ENDPOINT_ONE][OUT] = new InetSocketAddress("forwarding-service", SERVICE_PORT);
                forwardingTable[ENDPOINT_TWO][IN] = new InetSocketAddress("forwarding-service", SERVICE_PORT);
                forwardingTable[ENDPOINT_TWO][OUT] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
                break;
            default:
                break;
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Please enter router designation number >");
            Scanner input = new Scanner(System.in);
            int designation = input.nextInt();
            input.close();
            Router r = new Router(ROUTER_PORT, designation);
            r.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}