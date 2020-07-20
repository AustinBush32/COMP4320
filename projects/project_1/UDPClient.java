import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.*;


class UDPClient {

    public static void main(String args[]) throws Exception {   

        // Variable initializations
        DatagramSocket client_socket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");
        int server_port = 9090;
        String http_request = "GET TestFile.html HTTP/1.0";

        // Send packet to server
        byte[] data_out = http_request.getBytes();
        DatagramPacket packet_out = new DatagramPacket(
            data_out, data_out.length, server_ip, server_port
        );    
        client_socket.send(packet_out);

        // receive packet from server
        byte[] data_in = new byte[256];
        DatagramPacket packet_in = new DatagramPacket(data_in, data_in.length);
        while (true) {
            packet_in.setData(data_in);
            client_socket.receive(packet_in);
            
            System.out.println(new String(packet_in.getData()));

            if (new String(packet_in.getData()) == "\0") {
                System.out.println("received the \0");
                break;
            }
        }
        System.out.println("finish");
        client_socket.close();

    }

    // private static void gremlin(String probability, Packet packet) {
    //     Random rand = new Random();
    //     double damageProb = rand.nextDouble();
    //     double flipProb = rand.nextDouble();
    //     int bytesToChange;

    //     if (flipProb <= 0.5) {
    //         bytesToChange = 1;
    //     } else if (flipProb <= 0.8) {
    //         bytesToChange = 2;
    //     } else {
    //         bytesToChange = 3;
    //     }

    //     if (damageProb <= Double.parseDouble(probability)) {
    //         for (int i = 0; i < bytesToChange; i++) {
    //             byte[] data = packet.GETPacketData();
    //             int byteNum = rand.nextInt(packet.getPacketDataSize());
    //             data[byteNum] = (byte) ~data[byteNum];
    //         }
    //     }
    // }

    // private static void errorDetection(ArrayList<Packet> packetList) {
    //     for (Packet packet : packetList) {
    //         Short checkSum = Short.parseShort(packet.getHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM));
    //         byte[] data = packet.GETPacketData();
    //         short calculatedCheckSum = Packet.checkSum(data);
    //         if (!checkSum.equals(calculatedCheckSum)) {
    //             System.out.println("Error detected in Packet Number: " + packet.getHeaderValue(Packet.HEADER_ELEMENTS.SEGMENT_NUMBER));
    //         }
    //     }
    // }

}