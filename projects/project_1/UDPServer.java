import java.io.*;
import java.net.*;
import java.util.Scanner;


class UDPServer {   
    public static void main(String args[]) throws Exception {

        // Create socket at port 9090
        DatagramSocket server_socket = new DatagramSocket(9090);

        // Create byte arrays
        byte[] data_in = new byte[256];
        byte[] data_out  = new byte[256];

        // Create a scanner to 

        while(true) {

            // Get packet sent from client
            DatagramPacket packet_in = new DatagramPacket(data_in, data_in.length);
            server_socket.receive(packet_in);
            String sentence = new String(packet_in.getData());

            // Get client IP address and port number
            InetAddress client_ip = packet_in.getAddress();
            int client_port = packet_in.getPort();

            // Send the requested data to the client
            DatagramPacket packet_out = new DatagramPacket(
                packet_out, packet_out.length, client_ip, client_port
            );
            server_socket.send(packet_out);
        }
    }
}