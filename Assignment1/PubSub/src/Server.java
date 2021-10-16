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

	static final int HEADER_LENGTH = 3;
	static final int TYPE = 0;
	static final int SUB_TOPIC = 1;
	static final int MESSAGE_LENGTH = 2;
	
	static final int ACKCODE = 1;
	static final byte ACKPACKET = 10;


	static final byte BROKER = 1;
	static final byte CLIENT = 2;
	static final byte SERVER = 3;
	static final byte ACK = 4;
	static final byte SUBSCRIBER = 5;

	static final byte READY = 0;
	static final byte POOL_ONE = 1;
	static final byte POOL_TWO = 2;
	static final byte POOL_THREE = 3;

	static final int UPPER_CHLORINE_LIMIT = 15;
	static final int LOWER_CHLORINE_LIMIT = 5;

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
					switch(data[SUB_TOPIC]) {
						case POOL_ONE:
							System.out.println("Chlorine measurement for Pool One is: " + content + "ppm");
							sendResponse(content, POOL_ONE);
							break;
						case POOL_TWO:
							System.out.println("Chlorine measurement for Pool Two is: " + content + "ppm");
							sendResponse(content, POOL_TWO);
							break;
						case POOL_THREE:
							System.out.println("Chlorine measurement for Pool Three is: " + content + "ppm");
							sendResponse(content, POOL_THREE);
							break;
						case SUBSCRIBER:
							System.out.println("Subscriber says: " + content);
							break;
						default:
							System.err.println("ERROR: invalid SubTopic");
					}
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
            System.arraycopy(data, HEADER_LENGTH, buffer, 0, data[MESSAGE_LENGTH]);
            content = new String(buffer);
            data = new byte[HEADER_LENGTH];
            data[TYPE] = ACK;
			data[SUB_TOPIC] = 0;
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

	public void sendResponse(String content, byte subTopic){
		int measurement = Integer.parseInt(content);
		String response = "";
		if(measurement <= UPPER_CHLORINE_LIMIT && measurement >= LOWER_CHLORINE_LIMIT) {
			response = "Continue as normal";
		}
		else if(measurement > UPPER_CHLORINE_LIMIT){
			response = "Reduce Chlorine Levels to lower than " + UPPER_CHLORINE_LIMIT;
		}
		else {
			response = "Increase Chlorine levels to higher than " + LOWER_CHLORINE_LIMIT;
		}
		try {
			byte[] data = new byte[HEADER_LENGTH + response.length()];
			data[TYPE] = SERVER;
			switch(subTopic) {
				case POOL_ONE:
					data[SUB_TOPIC] = POOL_ONE;
					break;
				case POOL_TWO:
					data[SUB_TOPIC] = POOL_TWO;
					break;
				case POOL_THREE:
					data[SUB_TOPIC] = POOL_THREE;
					break;
				default:
					System.err.println("ERROR: invalid subTopic");
			}
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
			String message = "Ready to Receive";
			byte[] data = new byte[HEADER_LENGTH + message.length()];
			data[TYPE] = SERVER;
			data[SUB_TOPIC] = READY;
			data[MESSAGE_LENGTH] = (byte) message.length();
			System.arraycopy(message.getBytes(), 0 , data, HEADER_LENGTH, message.length());
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
			System.out.println("Waiting for contact...");
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
