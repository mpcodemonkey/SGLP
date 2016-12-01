package SGLP.Process;

import SGLP.MutableProcessList;

import java.io.IOException;

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
    private String name;

    public ServerProcess(MutableProcessList l, String c, String n){
        processMap = l;
        command = c;
        name = n;
    }

    @Override
    public void run() {
        Process sp = null;
        try {
            sp = Runtime.getRuntime().exec(command);
            try {
                sp.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        //remove entry from process map when server dies
        processMap.removeEntry(name);
        System.out.println(name + " has been removed from the active process list");

    }
}
