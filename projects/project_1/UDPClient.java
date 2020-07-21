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
        System.out.println("-------------------------Sending Data to Server-------------------------");

        // receive packet from server
        System.out.println("-----------------------Receiving Data from Server-----------------------");
        byte[] data_in = new byte[256];
        DatagramPacket packet_in = new DatagramPacket(data_in, data_in.length);
        
        ArrayList<Packet> received_packets = new ArrayList<>();
        boolean data_done_sending = false;
        int packet_num = 0;

        while (!data_done_sending) {
            client_socket.receive(packet_in);
            Packet data_received = Packet.createPacket(packet_in);
            packet_num++;
            System.out.println("Received packet_num: " + packet_num);
            if (data_received.GETPacketData()[0] == '\0') {
                data_done_sending = true;
            } else {
                received_packets.add(data_received);
            }
        }

        client_socket.close();

        System.out.println("------------------------Running Gremlin Function------------------------");
        String gremlin_prob = "0.0";
        if (args.length == 0) {
            System.out.println("There were no command line argumants for gremlin function");
        } else {
            gremlin_prob = args[0];
        }

        for (Packet packet : received_packets) {
            gremlin(gremlin_prob, packet);
        }

        errorDetection(received_packets);

        byte[] reassembled_file = Packet.reassemblePacket(received_packets);
        String reassembled_file_string = new String(reassembled_file);
        System.out.println("file recieved from server:\n" + reassembled_file_string);
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
                byte[] data = packet.GETPacketData();
                int byteNum = rand.nextInt(packet.getPacketDataSize());
                data[byteNum] = (byte) ~data[byteNum];
            }
        }
    }

    private static void errorDetection(ArrayList<Packet> packetList) {
        for (Packet packet : packetList) {
            Short checkSum = Short.parseShort(packet.getHeaderValue(Packet.HEADER_ELEMENTS.CHECKSUM));
            byte[] data = packet.GETPacketData();
            short calculatedCheckSum = Packet.checkSum(data);
            if (!checkSum.equals(calculatedCheckSum)) {
                System.out.println("Error detected in Packet Number: " + packet.getHeaderValue(Packet.HEADER_ELEMENTS.SEGMENT_NUMBER));
            }
        }
    }

}