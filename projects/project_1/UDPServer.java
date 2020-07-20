import java.io.*;
import java.net.*;


class UDPServer {   
    public static void main(String args[]) throws Exception {

        // Create socket at port 9090
        DatagramSocket server_socket = new DatagramSocket(9090);

        // Create byte arrays
        byte[] data_in = new byte[256];
        byte[] data_out  = new byte[256];
        byte[] data_terminate = "\0".getBytes();

        // Declare a FileInputStream object to convert the requested file to bytes
        FileInputStream file_bytes;

        while(true) {

            // Get packet sent from client
            DatagramPacket packet_in = new DatagramPacket(data_in, data_in.length);
            server_socket.receive(packet_in);

            // Get client IP address, port number, and HTTP request
            InetAddress client_ip = packet_in.getAddress();
            int client_port = packet_in.getPort();
            String client_get_request = new String(packet_in.getData());

            // Create the DatagramPacket to send data back to the client
            DatagramPacket packet_out = new DatagramPacket(
                data_out, data_out.length, client_ip, client_port
            );

            // Get the name of the file requested by the client
            String file_name = client_get_request.substring(
                client_get_request.indexOf(" ") + 1, client_get_request.lastIndexOf(" ")
            );

            // Define the FileInputStream object
            file_bytes = new FileInputStream(new File(file_name));

            // Send the requested data to the client
            while(file_bytes.read(data_out) != -1) {
                packet_out.setData(data_out);
                server_socket.send(packet_out);
            }

            // Notify the client that all data has been sent via a null character
            packet_out.setData(data_terminate);
            server_socket.send(packet_out);
        }

    }

}