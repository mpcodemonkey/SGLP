package SGLP.ExecutionManager;

import SGLP.GameInfo;

import java.io.File;
import java.io.IOException;

/**
 * Created by ubufu on 11/27/2016.
 */
public class ClientExecutionManager implements ExecutionManager{
    private String serverAddress;

    /*
    public void setServerAddress(String address){
        serverAddress = address;
    }*/

    @Override
    public void launchProcess(String command, GameInfo g ) {
        /**
         * since the client should only be able to play one game at a time,
         * instead of spawning a new non-blocking thread to run the game( how
         * the server handles execution ), we simply execute a new non-blocking
         * process, and wait for it to finish (block) before returning to the ui.
         */
        ProcessBuilder pb;
        //append switches for client run
        command += " " + "client" + " " + g.getIPInfo().getServerAddress() + " " + g.getIPInfo().getServerPort();
        pb = new ProcessBuilder(command.split(" "));
        pb.directory(new File(g.getFolder()));

        /**
         * the next few lines set up redirects for all input/output
         * from the child process to this thread. This allows for any
         * games that require command line interaction to still work
         * when launched from another process
         */
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        Process sp = null;
        try {
            sp = pb.start();
            sp.waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //edge case: join game button is pressed when server is down but connection was active
        //g.getIPInfo().setServerPort("");


    }
}
