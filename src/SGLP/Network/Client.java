package SGLP.Network;

import java.io.IOException;
import java.net.*;

/**
 * Created by ubufu on 11/15/2016.
 *
 * This is a basic UDP client. It sends a single message to the game server, querying
 * if the currently selected game is available. It will either receive a null/empty string,
 * or a number indicating the port that the game is running on.
 */
public class Client{
    public int fireRequest(String processName, String serverAddr) {
        String hostName = "127.0.0.1";
        DatagramSocket clientSock = null;
        int gamePort = -1;
        byte[] receiveData = new byte[12];

        try {
            clientSock = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        InetAddress serverIP = null;

        try {
            serverIP = InetAddress.getByName(serverAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            clientSock.setSoTimeout(3000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {

            byte[] buffer = new byte[12];
            DatagramPacket request = new DatagramPacket(processName.getBytes(), processName.length(), serverIP, 8888 );
            clientSock.send(request);

            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            clientSock.receive(response);

            String port = new String(response.getData()).trim();
            if(port != null && !port.equals("")){
                gamePort = Integer.parseInt(port);
            }

        }
        catch (SocketTimeoutException ste)
        {
            System.out.println ("Timeout Occurred: Packet assumed lost");
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientSock.close();
        return gamePort;
    }
}
