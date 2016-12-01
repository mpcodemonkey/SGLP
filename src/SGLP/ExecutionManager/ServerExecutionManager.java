package SGLP.ExecutionManager;

import SGLP.MutableProcessList;
import SGLP.Process.ServerProcess;

/**
 * Created by ubufu on 11/3/2016.
 *
 * The ServerExecutionManager class creates a new ServerProcess for each
 * game, and adds them to the synchronous current process map. This map
 * is used by the UDP Server to share with the client what games are
 * currently running.
 *
 * This class and the ServerProcess class are based off of the Java Process
 * Compiler/Launcher developed by Catherine Kramer.
 */
public class ServerExecutionManager implements ExecutionManager{

    private MutableProcessList processMap;
    private int currentPort;


    public ServerExecutionManager(MutableProcessList processList){

        processMap = processList;
        currentPort = 9001;

    }

    public MutableProcessList getProcessList(){
        return processMap;
    }

    @Override
    public void launchProcess(String command, String name){

        /**
         * if we've already launched an instance of the server for
         * a given game, there's no need to launch another, so we
         * exit immediately.
         */
        if(processMap.getEntry(name) != null) return;
        String port = currentPort+"";
        currentPort++;
        processMap.addEntry(name, port);
        ServerProcess sp = new ServerProcess(processMap, command, name);
        Thread st = new Thread(sp);
        st.start();
    }

}
