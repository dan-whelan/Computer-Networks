import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
public class Controller extends Node{
    static final int DEFAULT_PORT = 51510;

    static final int TYPE = 0;
    static final int LENGTH = 1;
    static final int HEADER_LENGTH = 2;

    static final byte ENDPOINT_ONE = 0;
    static final byte ENDPOINT_TWO = 1;
    static final byte ERROR = 7; //for now
    static final byte ACK = 6;
    static final byte CONTROLLER = 8;

    static final int NUMBER_OF_ROUTERS = 3;
    static final int NUMBER_OF_ENDPOINTS = 2;
    static final int INFO_TO_BE_STORED = 3;
    static final int DEST = 0;
    static final int IN = 1;
    static final int OUT = 2;
    static final int ROUTER_ONE = 0;
    static final int ROUTER_TWO = 1;
    static final int ROUTER_THREE = 2;

    static final int ACKCODE = 1;
    static final byte ACKPACKET = 10;
    private static InetSocketAddress[] addressTable;
    private static Object[][][] forwardingTable;

    private String content;
    private String address;
    Controller() {
        try {
            initialiseAddressTable();
            initialiseForwardingTable();
            socket = new DatagramSocket(DEFAULT_PORT);
            listener.go();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void initialiseForwardingTable() {
        forwardingTable = new Object[NUMBER_OF_ROUTERS][NUMBER_OF_ENDPOINTS][INFO_TO_BE_STORED];
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][IN] = "ForwardingService";
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][OUT] = "RouterTwo";
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][IN] = "RouterTwo";
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][OUT] = "ForwardingService";
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][IN] = "RouterOne";
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][OUT] = "RouterThree";
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][IN] = "RouterThree";
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][OUT] = "RouterOne";
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][IN] = "RouterTwo";
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][OUT] = "ForwardingService";
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][IN] = "ForwardingService";
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][OUT] = "RouterTwo";
    }

    private static void initialiseAddressTable() {
        addressTable = new InetSocketAddress[NUMBER_OF_ROUTERS];
        addressTable[ROUTER_ONE] = new InetSocketAddress("RouterOne", DEFAULT_PORT);
        addressTable[ROUTER_TWO] = new InetSocketAddress("RouterTwo", DEFAULT_PORT);
        addressTable[ROUTER_THREE] = new InetSocketAddress("RouterThree", DEFAULT_PORT);
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data;
            data = packet.getData();
            switch(data[TYPE]) {
                case ACK:
                    System.out.println("Packet Received");
                    break;
                case ROUTER_ONE:
                    content = sendAck(packet, data);
                    address = (String) forwardingTable[ROUTER_ONE][Integer.parseInt(content)][OUT];
                    sendPacket(CONTROLLER, address, addressTable[ROUTER_ONE]);
                    break;
                case ROUTER_TWO:
                    content = sendAck(packet, data);
                    address = (String) forwardingTable[ROUTER_TWO][Integer.parseInt(content)][OUT];
                    sendPacket(CONTROLLER, address, addressTable[ROUTER_TWO]);
                    break;
                case ROUTER_THREE:
                    content = sendAck(packet, data);
                    address = (String) forwardingTable[ROUTER_THREE][Integer.parseInt(content)][OUT];
                    sendPacket(CONTROLLER, address, addressTable[ROUTER_THREE]);
                    break;
                default:
                    System.out.println("Error: Unexpected Packet Receieved");
                    break;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String sendAck(DatagramPacket packet, byte[] data) {
        try{
            String message;
            DatagramPacket response;
            byte[] buffer = new byte[data[LENGTH]];
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, data[LENGTH]);
            message = new String(buffer);
            data = new byte[HEADER_LENGTH];
            data[TYPE] = ACK;
            data[ACKCODE] = ACKPACKET;
            response = new DatagramPacket(data, data.length);
            response.setSocketAddress(packet.getSocketAddress());
            socket.send(response);
            return message;
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

    
    public synchronized void start() {
        try {
            System.out.println("Starting Controller...");
            while(true) {
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Controller c = new Controller();
        c.start();
    }
}
