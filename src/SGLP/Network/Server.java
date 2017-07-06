package SGLP.Network;

import SGLP.MutableProcessList;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by ubufu on 11/15/2016.
 *
 * A simple UDP server. Ther UDP server is launched on its own
 * thread, and simply waits to receive packets from any number
 * of clients. It receives the name of the game requested from
 * each client, and sends back either a port number if the game
 * is running, or a null/empty string.
 */
public class Server implements Runnable{

    MutableProcessList processList;
    public Server(MutableProcessList m){
        processList = m;
    }

    @Override
    public void run() {

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(8888);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        DatagramPacket request;


        for(;;){
            String data = null;

            try{

                System.out.println("serving up a port");
                byte[] buffer = new byte[18];
                request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                System.out.println("receiving message from client");
                data = new String(request.getData()).trim();

                String response = processList.getEntry(data) == null ? "" : processList.getEntry(data);
                DatagramPacket reply = new DatagramPacket(response.getBytes(), response.length(), request.getAddress(), request.getPort());
                socket.send(reply);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }


    }
}
