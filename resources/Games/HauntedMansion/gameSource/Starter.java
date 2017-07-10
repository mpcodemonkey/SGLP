package gameSource;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JOptionPane;

import networking.GameClient;
import networking.GameServer;
import sage.networking.IGameConnection.ProtocolType;

public class Starter {
	
	public static boolean fullScreen;
	private static final int localPort = 1550;
	private static int numPlayers;
	
	public static void main (String [] args) throws IOException
	{
		
		
		ScriptEngine engine = getScript("Init.js");
		
		fullScreen = ((Boolean)engine.get("fullScreen")).booleanValue();

		if(args.length > 0 && args[0].equalsIgnoreCase("server")){

		}
		else{
			boolean isSinglePlayer = JOptionPane.showOptionDialog(null,
					"Single player or multiplayer game?",
					"Haunted Mansion",
					0,
					3,
					null,
					new String[] { "1 Player", "Multiplayer" },
					"Client") == 0;
			if (isSinglePlayer) {
				HauntedMansion g = new HauntedMansion();
				g.start();
			}
		}
		int serverPort = -1;
		boolean isHost = false;
		if(args.length > 0){
			if(args[0].equalsIgnoreCase("client")){
				isHost = false;
				serverPort = Integer.parseInt(args[2]);
			}
			else{
				isHost = true;
				serverPort = Integer.parseInt(args[1]);
			}
		}
		else{
			isHost = JOptionPane.showOptionDialog(null,
					"Host or Join?",
					"Haunted Mansion",
					0,
					3,
					null,
					new String[] { "Host", "Join" },
					"Client") == 0;
		}
		if (isHost)
		{
			GameServer server = new GameServer(serverPort);
			server.getLocalInetAddress();
			String[] msgTokens = server.getLocalInetAddress().toString().split("/");
			HauntedMansion_Networked serverClient = new HauntedMansion_Networked(msgTokens[1], serverPort, server);
			server.setGame(serverClient);
			serverClient.start();
		}
		else
		{
			String serverIP = args[1];//JOptionPane.showInputDialog(null, "Server IP?");
			//if (serverIP == "")
			//	serverIP = "127.0.0.1";
			HauntedMansion_Networked client = new HauntedMansion_Networked(serverIP, serverPort, null);
			client.start();
		}
	}
	
	protected static ScriptEngine getScript(String scriptName)
	{
		List<Object> out = new ArrayList();
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine jsEngine = factory.getEngineByName("js");
		try
		{
			FileReader fr;
			jsEngine.eval(fr = new FileReader(scriptName));
			fr.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return jsEngine;
	}
}
