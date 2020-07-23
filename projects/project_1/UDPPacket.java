// Authors: Austin Bush, Cole Beck
// Class: COMP 4320

import java.net.*;
import java.nio.*;
import java.util.*;

/*
 * Includes methods to create packets, getting packet data, getting and setting the packet header,
 * getting and setting the packet segmant number, segmentation, re-assembly, and the checksum
 * funtion.
*/

public class UDPPacket {
    private static String HEADER_CHECK_SUM = "CheckSum"; //key for check sum
    private static String HEADER_SEGMENT_NUM = "SegmentNumber"; //key for segment number
    private static final int HEADER_LINES = 4; //lines before data as stated in pdf
    private static int PACKET_SIZE = 256; //packet size allowed by the network as stated in pdf
    private static final int PACKET_DATA_SIZE = PACKET_SIZE - HEADER_LINES; //actual size of data
    private Map<String, String> packetHeader; //map for packet header info
    private byte[] packetData; //byte array for packet data

    // enum for key/value pairs
    public enum HEADER_VALUES {
        SEGMENT_NUM,
        CHECKSUM
    }

    //Constructor
    public UDPPacket() {
        packetHeader = new HashMap<>();
        packetData = new byte[PACKET_SIZE];
    }

    // method to create a new packet
    static UDPPacket makePacket(DatagramPacket packet) {

        UDPPacket newPacket = new UDPPacket(); // initalize a packet
        ByteBuffer bytebuffer = ByteBuffer.wrap(packet.getData()); // wrap into buffer
        byte[] data = packet.getData();
        byte[] remainder;

        // Set header segments
        newPacket.setHeaderValue(HEADER_VALUES.SEGMENT_NUM, Short.toString(bytebuffer.getShort()));
        newPacket.setHeaderValue(HEADER_VALUES.CHECKSUM, Short.toString(bytebuffer.getShort()));

        remainder = new byte[data.length - bytebuffer.position()];
        System.arraycopy(data, bytebuffer.position(), remainder, 0, remainder.length); 
        newPacket.setPacketData(remainder);
        return newPacket;
    }

    // method to segment the file into 256 bit chunks
    static ArrayList<UDPPacket> segment(byte[] file) {

        ArrayList<UDPPacket> packet = new ArrayList<>();
        int len = file.length; //length of the file
        int segmentCounter = 0;
        int byteCounter = 0;
        
        // check if the file is empty
        if (len == 0) {
            throw new IllegalArgumentException("Empty File");
        }

        // segment the file into packets of size 252 bytes
        while (byteCounter < len) {
            UDPPacket upcomingPacket = new UDPPacket();
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
            upcomingPacket.setHeaderValue(HEADER_VALUES.SEGMENT_NUM, Integer.toString(segmentCounter));
            String checkSumValue = String.valueOf(UDPPacket.calculateChecksum(data));
            upcomingPacket.setHeaderValue(HEADER_VALUES.CHECKSUM, checkSumValue);
            packet.add(upcomingPacket);
            segmentCounter++;
            byteCounter += dataSize;
        }
        return packet;
    }

    // method to reassemble packets in UDPClient.
    static byte[] reassemble(ArrayList<UDPPacket> packetList) {
    
        int counter = 0;
        int size = 0;
        byte[] assembledPacket;

        // find how large the total size is
        for(UDPPacket packet : packetList) {
            size += packet.getPacketSize();
        }
        assembledPacket = new byte[size]; //byte array for assembled packet

        // assemble the packets
        for (int i = 0; i < packetList.size(); i++) {
            for (UDPPacket packet : packetList) {
                String segment = packet.getHeaderValue(HEADER_VALUES.SEGMENT_NUM);
                if (Integer.parseInt(segment) == i) {
                    for(int j = 0; j < packet.getPacketSize(); j++) {
                        assembledPacket[counter + j] = packet.getPacketData(j);
                    }
                    counter += packet.getPacketSize();
                    break;
                }
            }
        }
        return assembledPacket;
    }

    // method to return the 16bit checksum value for the packet
    static short calculateChecksum(byte[] packet) {

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
    String getHeaderValue(HEADER_VALUES headerValue) {
        switch (headerValue) {
            case SEGMENT_NUM:
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
    int getPacketSize() {
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
    private void setHeaderValue(HEADER_VALUES headerValue, String value) {
        switch (headerValue) {
            case SEGMENT_NUM:
                packetHeader.put(HEADER_SEGMENT_NUM, value);
                break;
            case CHECKSUM:
                packetHeader.put(HEADER_CHECK_SUM, value);
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