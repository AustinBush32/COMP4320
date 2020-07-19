
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        //initalize packetData and packetHeader
        packetData = new byte[PACKET_SIZE];
        packetHeader = new HashMap<>();
    }

    // method to reassemble packets in UDPClient.
    static byte[] reassemblePacket(ArrayList<Packet> packetList) {
    
        int size = 0;
        // find how large the total size is
        for(Packet packet : packetList) {
            size += packet.getPacketDataSize();
        }
        byte[] assembledPacket = new byte[size]; //byte array for assembled packet

        int counter = 0;
        // assemble the packets
        for (int i = 0; i < packetList.size(); i++) {
            for (Packet packet : packetList) {
                String segment = packet.getHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER);
                if (Integer.parseInt(segment) == i) {
                    for(int j = 0; k < packet.getPacketDataSize(); k++) {
                        assembledPacket[counter + k] = packet.GETPacketData(k);
                    }
                    counter += packet.getPacketDataSize();
                    break;
                }
            }
        }
        return assembledPacket;
    }

    // method to segment the file into 256 bit chunks
    static ArrayList<Packet> segmentation(byte[] file) {
        ArrayList<Packet> packet = new ArrayList<>();
        int len = file.length; //length of the file

        // check if the file is empty
        if (len == 0) {
            throw new IllegalArgumentException("File Empty");
        }
        int byteCounter = 0;
        int segmentNumber = 0;

        // segment the file into packets of size 252 bytes
        while (byteCounter < len) {
            Packet nextPacket = new Packet();
            byte[] data = new byte[PACKET_DATA_SIZE];
            int readInDataSize = PACKET_DATA_SIZE;
            if (len - byteCounter < PACKET_DATA_SIZE) {
                readInDataSize = len - byteCounter;
            }
            int j = byteCounter;
            for (int i = 0; i < readInDataSize; i++) {
                data[i] = file[j];
                j++;
            }
            nextPacket.setPacketData(data);
            packet.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, segmentNumber + "");
            String CheckSumPacket = String.valueOf(Packet.CheckSum(data));
            nextPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, CheckSumPacket);
            packet.add(nextPacket);
            segmentNumber++;
            byteCounter = byteCounter + readInDataSize;
        }
        return packet;
    }

    // method to create a new packet
    static packet createPacket(DatagramPacket packet) {
        Packet newPacket = new Packet(); // initalize a packet
        ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData()); // wrap into buffer

        // Set header segments
        newPacket.setHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER, bytebuffer.getShort() + "");
        newPacket.setHeaderValue(HEADER_ELEMENTS.CHECKSUM, bytebuffer.getShort() + "");

        byte[] data = packet.getData();
        byte[] remaining = new byte[data.length - bytebuffer.position()];
        System.arraycopy(data, bytebuffer.position(), remaining, 0, remaining.length); 
        newPacket.setPacketData(remaining);
        return newPacket;
    }

    // method to return the 16bit checksum value for the packet
    static short checkSum(byte[] packetBytes) {
        long sum = 0;
        int packetByteLength = packetBytes.length;
        int count = 0;
        while (packetByteLength > 1) { 
            sum += ((packetBytes[count]) << 8 & 0xFF00) | ((packetBytes[count + 1]) & 0x00FF);
            if ((sum & 0xFFFF0000) > 0) {
                sum = ((sum & 0xFFFF) + 1);
            }
            count += 2;
            packetByteLength -= 2;
        }
        if (packetByteLength > 0) {
            sum += (packetBytes[count] << 8 & 0xFF00);
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
                throw new IllegalArgumentException("error in getHeaderValue");
        }
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
                throw new IllegalArgumentException("Something is broken... bad broken");
        }
    }

    // gets the packet data at an index
    private byte GETPacketData(int index) {
        if (index >= 0 && index < packetData.length) {
            return packetData[index];
        }
        throw new IndexOutOfBoundsException("GET PACKET DATA INDEX OUT OF BOUNDS EXCEPTION: index = " + index);
    }

    // gets packet data
    byte[] GETPacketData() {
        return packetData;
    }

    // get packet data size
    int getPacketDataSize() {
        return packetData.length;
    }

    //Takes an array of bytes to be set as the data segment.
    //If the Packet contains data already, the data is overwritten.
    //Throws IllegalArgumentException if the size of toSet does not
    //conform with the size of the data segment in the packet.
    private void setPacketData(byte[] toSet) throws IllegalArgumentException {
        int argumentSize = toSet.length;
        if (argumentSize > 0) {
            packetData = new byte[argumentSize];
            System.arraycopy(toSet, 0, packetData, 0, packetData.length);
        } else {
            throw new IllegalArgumentException("ILLEGAL ARGUEMENT EXCEPTION-SET PACKET DATA: toSet.length = " + toSet.length);
        }
            
    }

     //returns packet as a datagram packet
    DatagramPacket getDatagramPacket(InetAddress i, int port) {
        byte[] setData = ByteBuffer.allocate(256)
                .putShort(Short.parseShort(packetHeader.get(HEADER_SEGMENT_NUM)))
                .putShort(Short.parseShort(packetHeader.get(HEADER_CHECK_SUM)))
                .put(packetData)
                .array();

        return new DatagramPacket(setData, setData.length, i, port);
    }
}