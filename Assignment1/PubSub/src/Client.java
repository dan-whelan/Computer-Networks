/**
 * Adapted from sample client code given by Lecturer Stefan Weber
 */
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Random;

/**
 * Client class
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50002;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "broker";

	static final int HEADER_LENGTH = 2;
	static final int TYPE = 0;
	static final int MESSAGE_LENGTH = 1;

	static final byte ACKCODE = 1;
	static final byte ACKPACKET = 10;

	static final byte BROKER = 1;
	static final byte CLIENT = 2;
	static final byte SERVER = 3;
	static final byte ACK = 4;

	static final int UPPER_LIMIT = 1000;

	InetSocketAddress dstAddress;

	/**
	 * Constructor
	*/
	Client(String dstHost, int dstPort, int srcPort) {
		try {
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket packet) {
		try{
			byte[] data;
			String content;
			data = packet.getData();
			switch(data[TYPE]) {
				case ACK:
					System.out.println("Packet Received by Broker");
					break;
				case BROKER:
					content = sendAck(packet,data);
					System.out.println("Broker says: " + content);
					break;
				default:
					System.err.println("Error: Unexpected packet received");
					break;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void start(String content) {
		try {
			byte[] data = new byte[HEADER_LENGTH + content.length()];
			data[TYPE] = CLIENT;
			data[MESSAGE_LENGTH] = (byte) content.length();
			System.arraycopy(content.getBytes(), 0 , data, HEADER_LENGTH, content.length());
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
			this.wait();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	public String sendAck(DatagramPacket packet, byte[] data) {
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
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {
			Random generator = new Random();
			int measurement = generator.nextInt(UPPER_LIMIT);
			String content = String.valueOf(measurement);
			(new Client(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start(content);
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
