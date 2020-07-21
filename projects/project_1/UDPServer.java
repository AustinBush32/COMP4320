import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


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
            // file_bytes = new FileInputStream(new File(file_name));

            Scanner file_in = new Scanner(new File(file_name));
            StringBuilder file_data_contents = new StringBuilder();

            while (file_in.hasNext()) {
                file_data_contents.append(file_in.nextLine());
            }

            System.out.println("File: " + file_data_contents);
            file_in.close();

            String HTTP_HeaderForm = "HTTP/1.0 TestFile.html Follows\r\n"
                    + "Content-Type: text/plain\r\n"
                    + "Content-Length: " + file_data_contents.length() + "\r\n"
                    + "\r\n" + file_data_contents;

            ArrayList<Packet> packet_list = Packet.segmentation(HTTP_HeaderForm.getBytes()); //segments file into packets
            System.out.println("List of segmented packets is " + packet_list.size() + " packets long");

            for(Packet packet : packet_list) {
                packet_out.setData(packet.GETPacketData()); 
                server_socket.send(packet_out);
            }

            // Send the requested data to the client
            // while(file_bytes.read(data_out) != -1) {
            //     packet_out.setData(data_out);
            //     server_socket.send(packet_out);
            // }

            // Notify the client that all data has been sent via a null character
            packet_out.setData(data_terminate);
            System.out.println(new String(packet_out.getData()));
            server_socket.send(packet_out);

        }

    }

}