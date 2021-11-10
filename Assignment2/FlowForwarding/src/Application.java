import java.util.Scanner;
import java.net.DatagramSocket;
import java.io.IOException;
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
    static final byte ERROR = 5; //for now
    static final byte ACK = 6;

    static final int ACKCODE = 1;
    static final byte ACKPACKET = 10;

    private InetSocketAddress forwardingService = new InetSocketAddress("ForwardingService", SERVICE_PORT);
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
            switch(data[TYPE]) {
                case ACK:
                    System.out.println("Packet Received by Forwarding Service");
                    break;
                case ENDPOINT_ONE:
                    message = sendAck(packet, data);
                    System.out.println("Endpoint Two Says: " + message);
                    start();
                    break;
                case ENDPOINT_TWO:
                    message = sendAck(packet, data);
                    System.out.println("Endpoint One Says: " + message);
                    start();
                    break;
                default:
                    message = sendAck(packet, data);
                    System.err.println("ERROR: Unexpected Packet Received");
                    start();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void start() {
        try {
            byte[] data;
            DatagramPacket packet;
            Scanner input = new Scanner(System.in);
            input.useDelimiter("\n");
            System.out.println("Please enter destination of packet >");
            destination = input.next();
            System.out.println("Please enter message to send >");
            message = input.next();
            data = new byte[HEADER_LENGTH + message.length()];
            if(destination.equalsIgnoreCase("endpoint1") || destination.equalsIgnoreCase("endpointone")) {
                data[TYPE] = ENDPOINT_TWO;
            }
            else if(destination.equalsIgnoreCase("endpoint2") || destination.equalsIgnoreCase("endpointtwo")) {
                data[TYPE] = ENDPOINT_ONE;
            }
            else {
                data[TYPE] = ERROR;
            }
            data[LENGTH] = (byte) message.length();
            System.arraycopy(message.getBytes(), 0, data, HEADER_LENGTH, message.length());
            packet = new DatagramPacket(data, data.length);
            packet.setSocketAddress(forwardingService);
            socket.send(packet);
            while(true) {
                this.wait();
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

    public static void main(String[] args) {
        try {
                Application app = new Application(APPLICATION_PORT);
                app.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}