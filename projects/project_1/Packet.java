
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
        byte[] assembledPacket = new byte[];
        // find how large the total size is
        for(Packet packet : packetList) {
            size += packet.getPacketDataSize();
        }
        byte[] assembledPacket = new byte[size];

        int counter = 0;
        // assemble the packets
        for(int i = 0; i < packetList.size(); i++) {
            for(Packet packet : packetList) {
                String segment = packet.getHeaderValue(HEADER_ELEMENTS.SEGMENT_NUMBER);
                if(Integer.parseInt(segment) == i) {
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

    }

    // method to create a new packet
    static packet createPacket(DatagramPacket packet) {

    }

    // method to return the 16bit checksum value for the packet
    static short checkSum(byte[] packetBytes) {

    }

    // method to get header element values
    String getHeaderValue(HEADER_ELEMENTS HeaderElements) {

    }

    // method to set header key/value pairs
    private void setHeaderValue(HEADER_ELEMENTS HeaderElements, String HeaderValue) {

    }

    // gets the packet data at an index
    private byte GETPacketData(int index) {

    }

    // gets packet data
    byte[] GETPacketData() {
        return PackageData;
    }

    // get packet data size
    int getPacketDataSize() {
        return PackageData.length;
    }

    //Takes an array of bytes to be set as the data segment.
    //If the Packet contains data already, the data is overwritten.
    //Throws IllegalArgumentException if the size of toSet does not
    //conform with the size of the data segment in the packet.
    private void setPacketData(byte[] toSet) throws IllegalArgumentException {

    }

     //returns packet as a datagram packet
    DatagramPacket getDatagramPacket(InetAddress i, int port) {

    }
}