import java.io.*;
import java.net.*;


class UDPClient {

    public static void main(String args[]) throws Exception {   

        // Variable initializations
        DatagramSocket client_socket = new DatagramSocket();
        InetAddress server_ip = InetAddress.getByName("localhost");
        int server_port = 9090;
        String http_request = "GET TestFile.html HTTP/1.0 "

        // Send packet to server
        byte[] data_out = http_request.getBytes();
        DatagramPacket packet_out = new DatagramPacket(
            data_out, data_out.length, server_ip, server_port
        );    
        clientSocket.send(packet_out);

        // receive packet from server
        byte[] data_in = new byte[256];
        DatagramPacket packet_in = new DatagramPacket(data_in, data_in.length);
        client_socket.receive(packet_in);

        client_socket.close();

    }

}