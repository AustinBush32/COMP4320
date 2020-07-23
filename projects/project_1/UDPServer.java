// Authors: Austin Bush, Cole Beck
// Class: COMP 4320

import java.io.*;
import java.net.*;
import java.util.*;


class UDPServer {   
    public static void main(String args[]) throws Exception {

        // Create socket at port 9090
        DatagramSocket serverSocket = new DatagramSocket(9090);

        // Create byte arrays
        byte[] dataIn = new byte[256];
        String nullByte = "\0";

        while(true) {

            // Get packet sent from client
            DatagramPacket packetIn = new DatagramPacket(dataIn, dataIn.length);
            serverSocket.receive(packetIn);

            // Get client IP address, port number, and HTTP request
            InetAddress clientIP = packetIn.getAddress();
            int clientPort = packetIn.getPort();
            String clientGetRequest = new String(packetIn.getData());

            // Get the name of the file requested by the client
            String fileName = clientGetRequest.substring(
                clientGetRequest.indexOf(" ") + 1, clientGetRequest.lastIndexOf(" ")
            );
            BufferedReader fileIn = new BufferedReader(new FileReader(fileName));
            StringBuilder fileDataContents = new StringBuilder();

            String line = fileIn.readLine();
            System.out.println("line: " + line);
            while (line != null) {
                System.out.println(line);
                fileDataContents.append(line);
                line = fileIn.readLine();
            }
            fileIn.close();

            String httpHeader = "HTTP/1.0 TestFile.html Follows\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + fileDataContents.length() + "\r\n"
                    + "\r\n" + fileDataContents;

            ArrayList<UDPPacket> packetList = UDPPacket.segment(httpHeader.getBytes()); //segments file into packets
            System.out.println("List of segmented packets is " + packetList.size() + " packets long");

            for(UDPPacket packet : packetList) {
                DatagramPacket sendPacket = packet.getDatagramPacket(clientIP, clientPort);
                serverSocket.send(sendPacket);
            }

            // Notify the client that all data has been sent via a null character
            System.out.println("Sending null character");
            ArrayList<UDPPacket> nullPacket = UDPPacket.segment(nullByte.getBytes());
            DatagramPacket nullDatagram = nullPacket.get(0).getDatagramPacket(clientIP, clientPort);
            serverSocket.send(nullDatagram);
            System.out.print("Sent");
        }

    }

}