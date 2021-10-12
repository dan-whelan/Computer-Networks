/**
 * Adapted from sample server code given by lecturer Stefan Weber
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
/**
 * Server class
 */

public class Server extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;

	static final String BROKER_NAME = "broker";

	static final int HEADER_LENGTH = 2;
	static final int TYPE = 0;
	static final int MESSAGE_LENGTH = 1;
	
	static final int ACKCODE = 1;
	static final byte ACKPACKET = 10;

	static final byte BROKER = 1;
	static final byte CLIENT = 2;
	static final byte SERVER = 3;
	static final byte ACK = 4;
	static final byte BROKER_SUBSCRIBER = 6;

	InetSocketAddress dstAddress = new InetSocketAddress(BROKER_NAME, DEFAULT_DST_PORT);

	Server(int srcPort) {
		try {
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public void onReceipt(DatagramPacket packet) {
		try {
			byte[] data;
			String content;
			data = packet.getData();
			switch(data[TYPE]) {
				case ACK:
					System.out.println("Packet Received by Broker");
					break;
				case BROKER:
					content = sendAck(packet,data);
					sendResponse(content);
					break;
				case BROKER_SUBSCRIBER:
					content = sendAck(packet, data);
					System.out.println("Subscriber says: " + content);
					break;
				default:
					System.err.println("Error: Unexpected packet received");
					break;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String sendAck(DatagramPacket packet, byte[] data){
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

	public void sendResponse(String content){
		//int measurement = Integer.parseInt(content);
		String response = "Reduce Chlorine Levels";
		// if(measurement <= 200) {
		// 	response = "Continue as normal";
		// }
		// else {
		// 	response = "Reduce Chlorine Levels";
		// }
		try {
			byte[] data = new byte[HEADER_LENGTH + response.length()];
			data[TYPE] = SERVER;
			data[MESSAGE_LENGTH] = (byte) response.length();
			System.arraycopy(response.getBytes(), 0 , data, HEADER_LENGTH, response.length());
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void start() {
		try {
		System.out.println("Waiting for contact");
		this.wait();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			(new Server(DEFAULT_SRC_PORT)).start();
		} catch(java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}
