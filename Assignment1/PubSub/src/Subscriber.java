import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
/**
 * Subscriber class for receiving instruction from Server via Broker
 */
public class Subscriber extends Node {
    static final int DEFAULT_SRC_PORT = 50005;
    static final int DEFAULT_DST_PORT = 50001;
    static final String DEFAULT_DST_NODE = "broker";

    static final int HEADER_LENGTH = 3;
	static final int TYPE = 0;
	static final int SUB_TOPIC = 1;
	static final int MESSAGE_LENGTH = 2;

    static final int ACKCODE = 1;
    static final int ACKPACKET = 10;

    static final byte BROKER = 1;
    static final byte CLIENT = 2;
    static final byte SERVER = 3;
    static final byte ACK = 4;
    static final byte SUBSCRIBER = 5;

	static final byte READY = 0;
	static final byte POOL_ONE = 1;
	static final byte POOL_TWO = 2;
	static final byte POOL_THREE = 3;

    InetSocketAddress dstAddress;

    Subscriber(String dstHost, int dstPort, int srcPort) {
        try {
            dstAddress = new InetSocketAddress(dstHost, dstPort);
            socket = new DatagramSocket(srcPort);
            listener.go();
        } catch(Exception e) {
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
                case BROKER:
                    content = sendAck(packet,data);
                    doInstruction(content,data);
                    break;
                default:
                    System.err.println("Error: Unexpected Packet");
                    break;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String sendAck(DatagramPacket packet, byte[] data) {
        try {
            String content;
            DatagramPacket response;
            byte[] buffer = new byte[data[MESSAGE_LENGTH]];
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, data[MESSAGE_LENGTH]);
            content = new String(buffer);
            data = new byte[HEADER_LENGTH];
            data[TYPE] = ACK;
            data[ACKCODE] = ACKPACKET;
            response = new DatagramPacket(data, data.length);
            response.setSocketAddress(packet.getSocketAddress());
            return content;
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void doInstruction(String content, byte[] data) {
        switch(data[SUB_TOPIC]) {
            case POOL_ONE:
                if(content.equals("Continue as normal")) {
                    System.out.println("System continuing as normal as measurement between 5ppm and 15ppm in Pool One");
                }
                else {
                    System.out.println(content + "ppm in Pool One");
                }
                break;
            case POOL_TWO:
                if(content.equals("Continue as normal")) {
                    System.out.println("System continuing as normal as measurement between 5ppm and 15ppm in Pool Two");
                }
                else {
                    System.out.println(content + "ppm in Pool Two");
                }
                break;
            case POOL_THREE:
                if(content.equals("Continue as normal")) {
                    System.out.println("System continuing as normal as measurement between 5ppm and 15ppm in Pool Three");
                }
                else {
                    System.out.println(content + "ppm in Pool Three");
                }
                break;
            default:
                System.err.println("Error: Pool does not exist in this system");
                break;
        }
        String response = "Instructions carried out";
        try {
            data = new byte[HEADER_LENGTH + response.length()];
            data[TYPE] = SUBSCRIBER;
            data[SUB_TOPIC] = 1;
            data[MESSAGE_LENGTH] = (byte) response.length();
            System.arraycopy(response.getBytes(), 0, data, HEADER_LENGTH, response.length());
            DatagramPacket packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(dstAddress);
            socket.send(packet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        try {
            String message = "Ready to Receive";
			byte[] data = new byte[HEADER_LENGTH + message.length()];
			data[TYPE] = SUBSCRIBER;
			data[SUB_TOPIC] = READY;
			data[MESSAGE_LENGTH] = (byte) message.length();
			System.arraycopy(message.getBytes(), 0 , data, HEADER_LENGTH, message.length());
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
            System.out.println("Waiting for Contact...");
            this.wait();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            (new Subscriber(DEFAULT_DST_NODE,DEFAULT_DST_PORT,DEFAULT_SRC_PORT)).start(); 
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}