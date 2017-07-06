package SGLP.ExecutionManager;

import SGLP.GameInfo;
import SGLP.MutableProcessList;
import SGLP.Process.ServerProcess;

import java.util.HashMap;
import org.jvnet.winp.*;

/**
 * Created by ubufu on 11/3/2016.
 *
 * The ServerExecutionManager class creates a new ServerProcess for each
 * game, and adds them to the synchronous current process map. This map
 * is used by the UDP Server to share with the client what games are
 * currently running.
 *
 * This class and the ServerProcess class are based off of the Java Process
 * Compiler/Launcher developed by Catherine Kramer. Thanks Cat :)
 */
public class ServerExecutionManager implements ExecutionManager{

    private MutableProcessList processMap;
    private HashMap<String, Process> killMap;
    private int currentPort;


    public ServerExecutionManager(MutableProcessList processList){

        processMap = processList;
        currentPort = 9001;
        killMap = new HashMap<>();

    }

    public MutableProcessList getProcessList(){
        return processMap;
    }

    @Override
    public void launchProcess(String command, GameInfo game){

        /**
         * if we've already launched an instance of the server for
         * a given game, there's no need to launch another, so we
         * exit immediately.
         */
        if(processMap.getEntry(game.getName()) != null) return;
        String port = currentPort+"";
        currentPort++;
        processMap.addEntry(game.getName(), port);
        ServerProcess sp = new ServerProcess(processMap, command, game, killMap);
        Thread st = new Thread(sp);
        st.setDaemon(true);
        st.start();
    }
    public void destroyProcess(String killTarget){
        Process p = killMap.get(killTarget);
        if(p != null){
            WinProcess winProcess = new WinProcess(p);
            winProcess.killRecursively();
            killMap.remove(killTarget);
            //p.destroyForcibly();
        }
        else{
            System.out.println("no server running for " + killTarget);
        }
    }

}
