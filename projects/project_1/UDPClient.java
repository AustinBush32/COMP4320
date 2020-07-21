import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;


class UDPClient {

    public static void main(String args[]) throws Exception {   

        // Variable initializations
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverIP = InetAddress.getByName("192.168.0.65");
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
        
        ArrayList<Packet> receivedPackets = new ArrayList<>();
        boolean dataDoneSending = false;
        int packetNum = 0;

        while (!dataDoneSending) {
            clientSocket.receive(packetIn);
            Packet dataReceived = Packet.createPacket(packetIn);
            packetNum++;
            if (dataReceived.getPacketData()[0] == '\0') {
                dataDoneSending = true;
            } else {
                receivedPackets.add(dataReceived);
                System.out.println("Received packetNum: " + packetNum);
            }
        }

        clientSocket.close();

        System.out.println("------------------------Running Gremlin Function------------------------");
        String gremlinProb = "0.0";
        if (args.length == 0) {
            System.out.println("There were no command line argumants for gremlin function");
        } else {
            gremlinProb = args[0];
        }

        for (Packet packet : receivedPackets) {
            gremlin(gremlinProb, packet);
        }

        errorDetection(receivedPackets);

        byte[] reassembledFile = Packet.reassemblePacket(receivedPackets);
        String reassembledFileString = new String(reassembledFile);
        System.out.println("\nfile recieved from server:\n" + reassembledFileString);
    }

    private static void gremlin(String probability, Packet packet) {
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

        if (damageProb <= Double.parseDouble(probability)) {
            for (int i = 0; i < bytesToChange; i++) {
                byte[] data = packet.getPacketData();
                int byteNum = rand.nextInt(packet.getPacketDataSize());
                data[byteNum] = (byte) ~data[byteNum];
            }
        }
    }

    private static void errorDetection(ArrayList<Packet> packetList) {
        for (Packet packet : packetList) {
            Short checkSum = Short.parseShort(packet.getHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM));
            byte[] data = packet.getPacketData();
            short calculatedCheckSum = Packet.checkSum(data);
            if (!checkSum.equals(calculatedCheckSum)) {
                System.out.println("Error detected in Packet Number: " + packet.getHeaderValue(Packet.HEADER_ELEMENTS.SEGMENT_NUMBER));
            }
        }
    }

}