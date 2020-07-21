import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/*
 * Includes methods to create packets, getting packet data, getting and setting the packet header,
 * getting and setting the packet segmant number, segmentation, re-assembly, and the checksum
 * funtion.
*/

public class Packet {
    private static String HEADER_SEGMENT_NUM = "SegmentNumber"; //key for segment number
    private static String HEADER_CHECK_SUM = "CheckSum"; //key for check sum
    private static int PACKET_SIZE = 256; //packet size allowed by the network as stated in pdf
    private static final int HEADER_LINES = 4; //lines before data as stated in pdf
    private static final int PACKET_DATA_SIZE = PACKET_SIZE - HEADER_LINES; //actual size of data
    private byte[] packetData; //byte array for packet data
    private Map<String, String> packetHeader; //map for packet header info

    // enum for key/value pairs
    public enum HEADER_ELEMENTS {
        SEGMENT_NUMBER,
        CHECKSUM
    }

    //Constructor
    public Packet() {
        packetHeader = new HashMap<>();
        packetData = new byte[PACKET_SIZE];
    }

    // method to create a new packet
    static Packet createPacket(DatagramPacket packet) {

        Packet newPacket = new Packet(); // initalize a packet
        ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData()); // wrap into buffer
        byte[] data = packet.getData();
        byte[] remainder;

        // Set header segments
        newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, Short.toString(bytebuffer.getShort()));
        newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, Short.toString(bytebuffer.getShort()));

        remainder = new byte[data.length - bytebuffer.position()];
        System.arraycopy(data, bytebuffer.position(), remainder, 0, remainder.length); 
        newPacket.setPacketData(remainder);
        return newPacket;
    }

    // method to segment the file into 256 bit chunks
    static ArrayList<Packet> segmentation(byte[] file) {

        ArrayList<Packet> packet = new ArrayList<>();
        int len = file.length; //length of the file
        int byteCounter = 0;
        int segmentCounter = 0;

        // check if the file is empty
        if (len == 0) {
            throw new IllegalArgumentException("Empty File");
        }

        // segment the file into packets of size 252 bytes
        while (byteCounter < len) {
            Packet upcomingPacket = new Packet();
            byte[] data = new byte[PACKET_DATA_SIZE];
            int dataSize = PACKET_DATA_SIZE;
            if (len - byteCounter < PACKET_DATA_SIZE) {
                dataSize = len - byteCounter;
            }
            int j = byteCounter;
            for (int i = 0; i < dataSize; i++) {
                data[i] = file[j];
                j++;
            }
            upcomingPacket.setPacketData(data);
            upcomingPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, Integer.toString(segmentCounter));
            String checkSumValue = String.valueOf(Packet.checkSum(data));
            upcomingPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, checkSumValue);
            packet.add(upcomingPacket);
            segmentCounter++;
            byteCounter += dataSize;
        }
        return packet;
    }

    // method to reassemble packets in UDPClient.
    static byte[] reassemblePacket(ArrayList<Packet> packetList) {
    
        int size = 0;
        int counter = 0;
        byte[] assembledPacket;

        // find how large the total size is
        for(Packet packet : packetList) {
            size += packet.getPacketDataSize();
        }
        assembledPacket = new byte[size]; //byte array for assembled packet

        // assemble the packets
        for (int i = 0; i < packetList.size(); i++) {
            for (Packet packet : packetList) {
                String segment = packet.getHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER);
                if (Integer.parseInt(segment) == i) {
                    for(int j = 0; j < packet.getPacketDataSize(); j++) {
                        assembledPacket[counter + j] = packet.getPacketData(j);
                    }
                    counter += packet.getPacketDataSize();
                    break;
                }
            }
        }
        return assembledPacket;
    }

    // method to return the 16bit checksum value for the packet
    static short checkSum(byte[] packet) {

        long sum = 0;
        int byteLength = packet.length;
        int count = 0;

        while (byteLength > 1) { 
            sum += ((packet[count]) << 8 & 0xFF00) | ((packet[count + 1]) & 0x00FF);
            if ((sum & 0xFFFF0000) > 0) {
                sum = ((sum & 0xFFFF) + 1);
            }
            count += 2;
            byteLength -= 2;
        }
        if (byteLength > 0) {
            sum += (packet[count] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) { 
                sum = ((sum & 0xFFFF) + 1);
            }
        }
        return (short) (~sum & 0xFFFF);
    }


    // method to get header element values
    String getHeaderValue(HEADER_ELEMENTS HeaderElements) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                return packetHeader.get(HEADER_SEGMENT_NUM);
            case CHECKSUM:
                return packetHeader.get(HEADER_CHECK_SUM);
            default:
                throw new IllegalArgumentException("Error in getHeaderValue");
        }
    }

    // gets the packet data at an index
    private byte getPacketData(int index) {
        if (index >= 0 && index < packetData.length) {
            return packetData[index];
        }
        throw new IndexOutOfBoundsException("getPacketData out of bound exception at index " + index);
    }

    // gets packet data
    byte[] getPacketData() {
        return packetData;
    }

    // get packet data size
    int getPacketDataSize() {
        return packetData.length;
    }

     // returns packet as a datagram packet
     DatagramPacket getDatagramPacket(InetAddress ip, int port) {
        byte[] setData = ByteBuffer.allocate(256)
                .putShort(Short.parseShort(packetHeader.get(HEADER_SEGMENT_NUM)))
                .putShort(Short.parseShort(packetHeader.get(HEADER_CHECK_SUM)))
                .put(packetData)
                .array();

        return new DatagramPacket(setData, setData.length, ip, port);
    }

    // method to set header key/value pairs
    private void setHeaderValue(HEADER_ELEMENTS HeaderElements, String HeaderValue) {
        switch (HeaderElements) {
            case SEGMENT_NUMBER:
                packetHeader.put(HEADER_SEGMENT_NUM, HeaderValue);
                break;
            case CHECKSUM:
                packetHeader.put(HEADER_CHECK_SUM, HeaderValue);
                break;
            default:
                throw new IllegalArgumentException("Error in setHeaderValue");
        }
    }

    // Sets the packet to an array of bytes
    private void setPacketData(byte[] toSet) throws IllegalArgumentException {
        int toSetLen = toSet.length;

        if (toSetLen > 0) {
            packetData = new byte[toSetLen];
            System.arraycopy(toSet, 0, packetData, 0, packetData.length);
        } else {
            throw new IllegalArgumentException("Illegal argument exception in setPacketData: toSet.length = " + toSet.length);
        }
            
    }   
}