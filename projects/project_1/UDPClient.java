// Authors: Austin Bush, Cole Beck
// Class: COMP 4320

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;


class UDPClient {

    public static void main(String args[]) throws Exception {   

        // Variable initializations
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverIP = InetAddress.getByName("localhost");
        int serverPort = 9090;
        String httpRequest = "GET TestFile.html HTTP/1.0";

        // Send packet to server
        System.out.println("-------------------------Sending Data to Server-------------------------");
        byte[] dataOut = httpRequest.getBytes();
        DatagramPacket packetOut = new DatagramPacket(
            dataOut, dataOut.length, serverIP, serverPort
        );    
        clientSocket.send(packetOut);
        System.out.println("Complete");

        // Receive packet from server
        System.out.println("-----------------------Receiving Data from Server-----------------------");
        byte[] dataIn = new byte[256];
        DatagramPacket packetIn = new DatagramPacket(dataIn, dataIn.length);
        
        ArrayList<UDPPacket> received = new ArrayList<>();
        boolean finished = false;
        int packetNum = 0;

        while (!finished) {
            clientSocket.receive(packetIn);
            UDPPacket dataReceived = UDPPacket.makePacket(packetIn);
            packetNum++;
            if (dataReceived.getPacketData()[0] == '\0') {
                finished = true;
            } else {
                received.add(dataReceived);
                System.out.println("Received packetNum: " + packetNum);
            }
        }

        clientSocket.close();

        System.out.println("------------------------Running Gremlin Function------------------------");
        String gremlinProb = "0.0";
        if (args.length != 0) {
            gremlinProb = args[0];
        } else {
            System.out.println("There were no command line argumants for gremlin function");
        }

        for (UDPPacket packet : received) {
            gremlin(gremlinProb, packet);
        }

        errorDetection(received);

        byte[] reassembledFile = UDPPacket.reassemble(received);
        String reassembledFileString = new String(reassembledFile);
        System.out.println("\nfile recieved from server:\n" + reassembledFileString);
    }

    private static void errorDetection(ArrayList<UDPPacket> packetList) {
        for (UDPPacket packet : packetList) {
            String checksumHeaderValue = packet.getHeaderValue(UDPPacket.HEADER_VALUES.CHECKSUM);
            Short checkSum = Short.parseShort(checksumHeaderValue);
            byte[] data = packet.getPacketData();
            short calculatedCheckSum = UDPPacket.calculateChecksum(data);
            if (!checkSum.equals(calculatedCheckSum)) {
                String segmentHeaderValue = packet.getHeaderValue(UDPPacket.HEADER_VALUES.SEGMENT_NUM);
                System.out.println("Error detected in UDPPacket Number: " + segmentHeaderValue);
            }
        }
    }

    private static void gremlin(String probability, UDPPacket packet) {
        Random rand = new Random();
        double damageProb = rand.nextDouble();
        double flipProb = rand.nextDouble();
        int bytesToChange;

        if (flipProb <= 0.5) {
            bytesToChange = 1;
        } else if (flipProb <= 0.8) {
            bytesToChange = 2;
        } else {
            bytesToChange = 3;
        }

        if (Double.parseDouble(probability) >= damageProb) {
            for (int i = 0; i < bytesToChange; i++) {
                byte[] data = packet.getPacketData();
                int byteNum = rand.nextInt(packet.getPacketSize());
                data[byteNum] = (byte) ~data[byteNum];
            }
        }
    }
}