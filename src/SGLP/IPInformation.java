package SGLP;

/**
 * Created by ubufu on 11/1/2016.
 */
public class IPInformation {

    private String clientAddress;
    private String ClientPort;
    private String ServerAddress;
    private String ServerPort;

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientPort() {
        return ClientPort;
    }

    public void setClientPort(String clientPort) {
        ClientPort = clientPort;
    }

    public String getServerAddress() {
        return ServerAddress;
    }

    public void setServerAddress(String serverAddress) {
        ServerAddress = serverAddress;
    }

    public String getServerPort() {
        return ServerPort;
    }

    public void setServerPort(String serverPort) {
        ServerPort = serverPort;
    }

}
