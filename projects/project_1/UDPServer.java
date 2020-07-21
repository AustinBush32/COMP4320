import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


class UDPServer {   
    public static void main(String args[]) throws Exception {

        // Create socket at port 9090
        DatagramSocket serverSocket = new DatagramSocket(9090);

        // Create byte arrays
        byte[] dataIn = new byte[256];
        byte[] dataOut  = new byte[256];
        String nullByte = "\0";
        byte[] dataTerminate = "\0".getBytes();

        // Declare a FileInputStream object to convert the requested file to bytes
        FileInputStream fileBytes;


        while(true) {

            // Get packet sent from client
            DatagramPacket packetIn = new DatagramPacket(dataIn, dataIn.length);
            serverSocket.receive(packetIn);

            // Get client IP address, port number, and HTTP request
            InetAddress clientIP = packetIn.getAddress();
            int clientPort = packetIn.getPort();
            String clientGetRequest = new String(packetIn.getData());

            // Create the DatagramPacket to send data back to the client
            DatagramPacket packetOut = new DatagramPacket(
                dataOut, dataOut.length, clientIP, clientPort
            );

            // Get the name of the file requested by the client
            String fileName = clientGetRequest.substring(
                clientGetRequest.indexOf(" ") + 1, clientGetRequest.lastIndexOf(" ")
            );

            // Define the FileInputStream object
            // fileBytes = new FileInputStream(new File(fileName));

            Scanner fileIn = new Scanner(new File(fileName));
            StringBuilder fileDataContents = new StringBuilder();

            while (fileIn.hasNext()) {
                fileDataContents.append(fileIn.nextLine());
            }

            // System.out.println("File: " + fileDataContents);
            fileIn.close();

            String httpHeaderForm = "HTTP/1.0 TestFile.html Follows\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + fileDataContents.length() + "\r\n"
                    + "\r\n" + fileDataContents;

            ArrayList<Packet> packetList = Packet.segmentation(httpHeaderForm.getBytes()); //segments file into packets
            System.out.println("List of segmented packets is " + packetList.size() + " packets long");

            for(Packet packet : packetList) {
                DatagramPacket sendPacket = packet.getDatagramPacket(clientIP, clientPort);
                serverSocket.send(sendPacket);

                // packetOut.setData(packet.getPacketData()); 
                // serverSocket.send(packetOut);
            }

            // Send the requested data to the client
            // while(fileBytes.read(dataOut) != -1) {
            //     packetOut.setData(dataOut);
            //     serverSocket.send(packetOut);
            // }

            // Notify the client that all data has been sent via a null character
            System.out.println("Sending null character");
            ArrayList<Packet> nullPacket = Packet.segmentation(nullByte.getBytes());
            // packetOut.setData(dataTerminate);
            // packetOut.setData(nullPacket.get(0).getPacketData());
            DatagramPacket nullDatagram = nullPacket.get(0).getDatagramPacket(clientIP, clientPort);
            serverSocket.send(nullDatagram);
            System.out.print("Sent");
            // serverSocket.close();

        }

    }

}