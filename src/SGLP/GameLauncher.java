package SGLP;

import SGLP.Command.CheckServerCommand;
import SGLP.Command.ExecutionCommand;
import SGLP.Command.KillCommand;
import SGLP.ExecutionManager.ClientExecutionManager;
import SGLP.ExecutionManager.ServerExecutionManager;
import SGLP.Network.Client;
import SGLP.Network.Server;
import SGLP.UI.ClientUIBuilder;
import SGLP.UI.ServerUIBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by ubufu on 11/1/2016.
 *
 * The game launcher launches an instance of either the client
 * UI or the server UI. Currently, this must be changed via the
 * boolean isClient flag, but this will be changed shortly to A
 * dialog box.
 */
public class GameLauncher {

    private static String PATH = Paths.get(".", "resources").toString();
    private static String GAMEPATH = Paths.get(PATH + File.separator + "Games").toString();

    ServerExecutionManager sem;
    ClientExecutionManager clem;
    ExecutionCommand ec;
    KillCommand kc;
    CheckServerCommand csc;
    HashMap<String, GameInfo> gameMapping;
    HashMap<String, BufferedImage> gameImageMap;
    ClientUIBuilder uiBuilder;
    String serverAddress = "127.0.0.1";
    Server serverThread;
    public void start(boolean isClient){

        String name = "GameInfo.json";
        Gson infoAsJson = new Gson();
        ArrayList<GameInfo> AvailableGames = new ArrayList<>();
        gameImageMap = new HashMap<>();

        File[] folders = new File(GAMEPATH).listFiles();

        //no games, no reason to run
        if(folders == null || folders.length == 0){
            JOptionPane.showMessageDialog(null, "No games found. All games should be put in the resources\\Games folder");
            return;
        }

        Environment sageEnvironments = new Environment(PATH);

        HashMap<String, String> servers = null;
        try {
            servers = infoAsJson.fromJson(new FileReader(PATH + File.separator + "Servers.json"), new TypeToken<HashMap<String, String>>(){}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(File folder: folders){
            if(folder.isDirectory()){
                try
                {
                    GameInfo newGame = infoAsJson.fromJson(new FileReader(GAMEPATH + File.separator + folder.getName() + File.separator + name), GameInfo.class);
                    newGame.setFolder(GAMEPATH + File.separator + folder.getName());
                    newGame.getIPInfo().setServerAddress(servers.get(newGame.getName()));
                    AvailableGames.add(newGame);
                    System.out.println("added " + newGame.getName());
                }
                catch(FileNotFoundException f){
                    System.out.println("No info found. the game in " + folder.getName() + " will be skipped");
                }
            }
        }

        //create game map
        gameMapping = createNameMap(AvailableGames);

        if(isClient){
            for(GameInfo g : gameMapping.values()){
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new File(g.getFolder() + File.separator + g.getImageLocation()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                gameImageMap.put(g.getName(), image);
            }
            csc = new CheckServerCommand(this);
            clem = new ClientExecutionManager();
            ec = new ExecutionCommand(clem, sageEnvironments, "client");
            uiBuilder = new ClientUIBuilder(gameMapping, AvailableGames, ec, csc, gameImageMap);
        }
        else{
            sem = new ServerExecutionManager(new MutableProcessList());
            ec = new ExecutionCommand(sem, sageEnvironments, "server");
            kc = new KillCommand(sem);
            serverThread = new Server(sem.getProcessList());
            ServerUIBuilder serveruiBuilder = new ServerUIBuilder(gameMapping, AvailableGames, ec, kc);
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
        int activePort = c.fireRequest(activeGame.getName(), activeGame.getIPInfo().getServerAddress());

        //could not find a game
        if(activePort < 0){
            activeGame.getIPInfo().setServerPort("");
            uiBuilder.checkActivePlayButton();
            uiBuilder.displayGameNotFound();
        }
        else{
            System.out.println("client received port " + activePort + " as server port");
            activeGame.getIPInfo().setServerPort(activePort+"");
            //activeGame.getIPInfo().setServerAddress(serverAddress+"");
            uiBuilder.checkActivePlayButton();
        }
    }
}
