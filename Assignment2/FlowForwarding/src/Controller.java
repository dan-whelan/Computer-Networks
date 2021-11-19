import java.net.InetSocketAddress;
public class Controller {
    static final int SERVICE_PORT = 51510;
    static final int ROUTER_PORT = 51510;

    static final int TYPE = 0;
    static final int LENGTH = 1;
    static final int HEADER_LENGTH = 2;

    static final byte ENDPOINT_ONE = 0;
    static final byte ENDPOINT_TWO = 1;
    static final byte ERROR = 5; //for now
    static final byte ACK = 6;

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
    public static Object[][][] forwardingTable;
    Controller() {

    }

    private static void initialiseForwardingTable() {
        forwardingTable = new Object[NUMBER_OF_ROUTERS][NUMBER_OF_ENDPOINTS][INFO_TO_BE_STORED];
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][DEST] = ENDPOINT_TWO;
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][DEST] = ENDPOINT_ONE;
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][IN] = new InetSocketAddress("ForwardingService", SERVICE_PORT);
        forwardingTable[ROUTER_ONE][ENDPOINT_ONE][OUT] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][IN] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
        forwardingTable[ROUTER_ONE][ENDPOINT_TWO][OUT] = new InetSocketAddress("ForwardingService", SERVICE_PORT);
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][IN] = new InetSocketAddress("RouterOne", ROUTER_PORT);
        forwardingTable[ROUTER_TWO][ENDPOINT_ONE][OUT] = new InetSocketAddress("RouterThree", ROUTER_PORT);
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][IN] = new InetSocketAddress("RouterThree", ROUTER_PORT);
        forwardingTable[ROUTER_TWO][ENDPOINT_TWO][OUT] = new InetSocketAddress("RouterOne", SERVICE_PORT);
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][IN] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
        forwardingTable[ROUTER_THREE][ENDPOINT_ONE][OUT] = new InetSocketAddress("ForwardingService", SERVICE_PORT);
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][IN] = new InetSocketAddress("ForwardingService", SERVICE_PORT);
        forwardingTable[ROUTER_THREE][ENDPOINT_TWO][OUT] = new InetSocketAddress("RouterTwo", ROUTER_PORT);
        
    }
    
    public void run() {
        try {
            initialiseForwardingTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Controller c = new Controller();
        c.run();
    }
}
