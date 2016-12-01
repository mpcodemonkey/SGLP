package SGLP.ExecutionManager;

import java.io.IOException;

/**
 * Created by ubufu on 11/27/2016.
 */
public class ClientExecutionManager implements ExecutionManager{
    private String serverAddress;

    public void setServerAddress(String address){
        serverAddress = address;
    }

    @Override
    public void launchProcess(String command, String Name) {
        /**
         * since the client should only be able to play one game at a time,
         * instead of spawning a new non-blocking thread to run the game( how
         * the server handles execution ), we simply execute a new non-blocking
         * process, and wait for it to finish (block) before returning to the ui.
         */
        Process cp = null;
        try {
            cp = Runtime.getRuntime().exec(command);
            try {
                cp.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
}
