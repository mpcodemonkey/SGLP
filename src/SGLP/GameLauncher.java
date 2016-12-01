package SGLP;

import SGLP.Command.CheckServerCommand;
import SGLP.Command.ExecutionCommand;
import SGLP.ExecutionManager.ClientExecutionManager;
import SGLP.ExecutionManager.ServerExecutionManager;
import SGLP.Network.Client;
import SGLP.Network.Server;
import SGLP.UI.ClientUIBuilder;
import SGLP.UI.ServerUIBuilder;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by ubufu on 11/1/2016.
 *
 * The game launcher launches an instance of either the client
 * UI or the server UI. Currently, this must be changed via the
 * boolean isClient flag, but this will be changed shortly to A
 * dialog box.
 */
public class GameLauncher {

    boolean isClient = false;
    ServerExecutionManager sem;
    ClientExecutionManager clem;
    ExecutionCommand ec;
    CheckServerCommand csc;
    HashMap<String, GameInfo> gameMapping;
    ClientUIBuilder uiBuilder;
    String serverAddress = "127.0.0.1"; //todo: read server address from file
    Server serverThread;
    public void start(){
        /**
         * todo: rewrite games to not require dialog
         */

        String path = File.separator + "Games";
        String name = "GameInfo.json";
        Gson infoAsJson = new Gson();
        ArrayList<GameInfo> AvailableGames = new ArrayList<>();

        File[] folders = new File(path).listFiles();

        for(File folder: folders){
            if(folder.isDirectory()){//todo: change try/catch to check if null
                try
                {
                    GameInfo newGame = infoAsJson.fromJson(new FileReader(path + File.separator + folder.getName() + File.separator + name), GameInfo.class);
                    newGame.setFolder(folder.getName());
                    AvailableGames.add(newGame);
                }
                catch(FileNotFoundException f){
                    System.out.println("No info found. the game in " + folder.getName() + " will be skipped");
                }
            }
        }

        //create game map
        gameMapping = createNameMap(AvailableGames);

        if(isClient){
            //todo: build client ui, add refresh button for checking game availability, unblock play button if game found
            //serverAddress = readServerAddrFromFile();
            csc = new CheckServerCommand(this);
            clem = new ClientExecutionManager();
            clem.setServerAddress(serverAddress);
            ec = new ExecutionCommand(clem);
            uiBuilder = new ClientUIBuilder(gameMapping, AvailableGames, ec, csc);
        }
        else{
            //todo: build server ui, add launch button for launching games, receive info from client for game request.
            sem = new ServerExecutionManager(new MutableProcessList());
            ec = new ExecutionCommand(sem);
            serverThread = new Server(sem.getProcessList());
            ServerUIBuilder serveruiBuilder = new ServerUIBuilder(gameMapping, AvailableGames, ec);
            Thread t = new Thread(serverThread);
            t.run();
        }

    }

    private HashMap<String, GameInfo> createNameMap(ArrayList<GameInfo> games) {
        HashMap<String, GameInfo> gameMap = new HashMap<>();

        for(GameInfo game: games){
            gameMap.put(game.getName(), game);
        }
        return gameMap;
    }

    public void checkGameServer(GameInfo activeGame) {
        Client c = new Client();
        int activePort = c.fireRequest(activeGame.getName(), serverAddress);

        //could not find a game
        if(activePort < 0){
            activeGame.getIPInfo().setServerPort("");
            uiBuilder.checkActivePlayButton();
            uiBuilder.displayGameNotFound();
        }
        else{
            activeGame.getIPInfo().setServerPort(activePort+"");
            uiBuilder.checkActivePlayButton();
        }
    }
}