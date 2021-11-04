import java.util.Scanner;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class Application extends Node{
    static final int APPLICATION_PORT = 50000;
    static final int SERVICE_PORT = 51510;

    static final int TYPE = 0;
    static final int LENGTH = 1;
    static final int HEADER_LENGTH = 2;

    static final byte ENDPOINT_ONE = 0;
    static final byte ENDPOINT_TWO = 1;
    static final byte ERROR = 2; //for now
    static final byte ACK = 3;

    static final int ACKCODE = 4;
    static final byte ACKPACKET = 10;

    private InetSocketAddress forwardingService = new InetSocketAddress("forwarding-service", SERVICE_PORT);
    private String destination;
    private String message;

    Application(int srcPort) {
        try {
            socket = new DatagramSocket(srcPort);
            listener.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {
            byte[] data;
            data = packet.getData();
            message = sendAck(packet, data);
            switch(data[TYPE]) {
                case ACK:
                    System.out.println("Packet Received by Forwarding Service");
                    break;
                case ENDPOINT_ONE:
                    System.out.println("Endpoint One Says: " + message);
                    break;
                case ENDPOINT_TWO:
                    System.out.println("Endpoint Two Says: " + message);
                    break;
                default:
                    System.err.println("ERROR: Unexpected Packet Received");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        try {
            while(true) {
                byte[] data;
                DatagramPacket packet;
                System.out.println("Please enter destination of packet >");
                Scanner input = new Scanner(System.in);
                destination = input.next();
                System.out.println("Please enter message to send >");
                message = input.next();
                input.close();
                data = new byte[HEADER_LENGTH + message.length()];
                if(destination.equalsIgnoreCase("endpoint 1") || destination.equalsIgnoreCase("endpoint one")) {
                    data[TYPE] = ENDPOINT_ONE;
                }
                else if(destination.equalsIgnoreCase("endpoint 2") || destination.equalsIgnoreCase("endpoint two")) {
                    data[TYPE] = ENDPOINT_TWO;
                }
                else data[TYPE] = ERROR;
                data[LENGTH] = (byte) message.length();
                System.arraycopy(message.getBytes(), 0, data, HEADER_LENGTH, message.length());
                packet = new DatagramPacket(data, data.length);
                packet.setSocketAddress(forwardingService);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendAck(DatagramPacket packet, byte[] data) {
		try{
            String content;
            DatagramPacket response;
            byte[] buffer = new byte[data[LENGTH]];
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, LENGTH);
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

    public static void main(String[] args) {
        try {
            Application app = new Application(APPLICATION_PORT);
            app.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}