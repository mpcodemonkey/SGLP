package SGLP.Process;

import SGLP.GameInfo;
import SGLP.MutableProcessList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ubufu on 11/3/2016.
 *
 * The ServerProcess class launches a new process for the game specified in
 * the Server UI. Only one instance of each game can be launched at a given time,
 * so for N games, there can be at most N processes launched and running concurrently.
 *
 * When A new game process is launched, it is added to a synchronous process map in the
 * ServerExecutionManager class. The run method in this class creates a new process for each
 * game, waits for it to finish/exit, and then removes that game from the process map.
 *
 * This class and the ServerExecutionManager class are based off of the Java Process
 * Compiler/Launcher developed by Catherine Kramer.
 */
public class ServerProcess implements Runnable {

    private MutableProcessList processMap;
    private String command;
    private GameInfo game;
    private HashMap<String, Process> killMap;

    public ServerProcess(MutableProcessList l, String c, GameInfo g, HashMap<String, Process> k){
        processMap = l;
        command = c;
        game = g;
        killMap = k;
    }

    @Override
    public void run() {
        ProcessBuilder pb;
        //append commands for server launch
        command += " " + "server" + " " + processMap.getEntry(game.getName());
        pb = new ProcessBuilder(command.split(" "));
        pb.directory(new File(game.getFolder()));

        /*
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
                killMap.put(game.getName(), sp);
                sp.waitFor();
        }
        catch (InterruptedException e) {
                e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //remove entry from process map when server dies
        processMap.removeEntry(game.getName());
        System.out.println(game.getName() + " has been removed from the active process list");

    }
}
