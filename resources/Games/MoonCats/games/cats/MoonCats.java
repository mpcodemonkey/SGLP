//Stephen Ly
//CSC165

package games.cats;

import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.*;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.model.loader.ogreXML.*;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.renderer.IRenderer;
import sage.scene.Group;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import sage.scene.HUDString;
import sage.scene.SkyBox;
import sage.scene.SkyBox.Face;
import sage.scene.shape.Rectangle;
import sage.scene.state.TextureState;
import sage.scene.state.RenderState.RenderStateType;
import sage.terrain.AbstractHeightMap;
import sage.terrain.ImageBasedHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.Version;
import myGameEngine.*;

public class MoonCats extends BaseGame implements MouseListener, MouseMotionListener{
	
	private int p1score = 0;
	private int p2score = 0;
	private float maxTime = 80.0f;
	private float time = maxTime;
	
	private HUDString p1scoreString, p2scoreString;
	private HUDString timeString, gameOverString, whoWonString;
	private boolean gameStart = false;
	private boolean unhandledThrow = false;
	private boolean unhandledHit = false;
	private boolean unhandledReset = false;
	
	private IInputManager im;
	private IDisplaySystem display;
	private IEventManager eventMgr;
	private ICamera camera1;
	private IRenderer renderer;
	private TimedEventManager tm;
	private IAudioManager audioMgr;
	
	private OrbitCameraController camMeController, camTheyController;
	private Controller p1Controller;

	private Player player1, player2, currPlayer, otherPlayer;
	private IPlayerController enemy;
	private Ball dBall1, dBall2, dBall3;
	private Rectangle plane1, wallN, wallS, wallE, wallW;
	private SceneNode rootNode;
	private SkyBox skybox;
	private TerrainBlock imageTerrain;
	
	private Robot robot;
	private Point cCenter;
	private float prevMouseX, prevMouseY;
	private float curMouseX, curMouseY;
	private int mousePressed;
	private boolean isRecentering;

	private ScriptEngine engine;
	private String sysInitScript;
	private String nodeInitScript;
	
	private ServerSocket serverSock;
	private Socket clientSock;
	private int connType;
	private boolean spMode = false;
	
	private IPhysicsEngine physicsEngine;
	private IPhysicsObject terrain, ball1, ball2, ball3, p1, p2, wN, wS, wE, wW;
	
	private Sound bgm, walkSound1, throwSound1, hitSound1, walkSound2, throwSound2, hitSound2;

	//begin expo code
	private String runtType, serverIP, serverPort;
	public void startExpo(String [] cmdValues){
		runtType = cmdValues[0];
		//server
		if(cmdValues.length == 2){
			serverPort = cmdValues[1];
		}
		else{
			serverIP = cmdValues[1];
			serverPort = cmdValues[2];
		}
		start();
	}
	//end expo code
	protected void initSystem(){ 
		
		//create an Input Manager
		IInputManager inputManager = new InputManager();
		setInputManager(inputManager);
		
		//create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
		
		initScripts();
		initController();
		initNetwork();
		
		this.runScript(sysInitScript);
		
		IDisplaySystem mySys = (IDisplaySystem) engine.get("mySys");
		
		//call a local method to create a DisplaySystem object
		display = createDisplaySystem(mySys);
		setDisplaySystem(display);
		renderer = display.getRenderer();
		Canvas myCan = renderer.getCanvas();
		
		camera1 = new JOGLCamera(renderer);
		camera1.setPerspectiveFrustum(60, 16/9, 1, 2000);
		renderer.setCamera(camera1);

		initMouseMode();
		myCan.addMouseListener(this);
		myCan.addMouseMotionListener(this);
		eventMgr = EventManager.getInstance();
		im = this.getInputManager();

		initAudio();
	}
	
	private void initScripts(){
		ScriptEngineManager factory = new ScriptEngineManager();
		List<ScriptEngineFactory> list = factory.getEngineFactories();

		System.out.println("Script Engine Factories found:");
		for (ScriptEngineFactory f : list){ 
			System.out.println(" Name = " + f.getEngineName() + " language = " + f.getLanguageName() + " extensions = " + f.getExtensions());
		}
		// get the JavaScript engine
		engine = factory.getEngineByName("js");
		sysInitScript = "sysConfig.js";
		nodeInitScript = "initObjs.js";
	}
	
	private void initController(){

/*
		System.out.println("JInput version: " + Version.getVersion());
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] cs = ce.getControllers();
		
		listControllers(cs);

		Scanner sc = new Scanner(System.in);

		System.out.print("Please select a keyboard/gamepad\n#");
		int choice = Integer.parseInt(sc.nextLine());


		p1Controller = cs[choice];
*/
		// quick demo of controller manager
		ArrayList<Controller> playerControllers = new ArrayList<Controller>();
		ControllerManager cm = new ControllerManager(playerControllers);
		while (cm.isShowing()) {
			// has to be done, java will kill the program otherwise
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		p1Controller = playerControllers.get(0);
	}
	
	private void initNetwork(){

		int sOrC = -1;
		if(runtType.equalsIgnoreCase("client")){
			sOrC = 2;
		}
		else{
			sOrC = 1;
		}
		/*
		System.out.println("Is this the server or client? (1/2)");
		Scanner sc = new Scanner(System.in);
		int sOrC = Integer.parseInt(sc.nextLine());
		if(sOrC > 2 || sOrC < 1){
			System.out.println("Networking mode undefined, defaulting to singleplayer mode");
			spMode = true;
			return;
		} else{
			
		}*/
		
		if(sOrC == 1){
			connType = 1;
			//System.out.println("What port is server listening on? ");
			int port = Integer.parseInt(serverPort);//Integer.parseInt(sc.nextLine());
			try {
				serverSock = new ServerSocket(port);
				System.out.println("Server listening for client on port " + port);
				clientSock = serverSock.accept();
				System.out.println("Client connected, starting client handling thread");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(sOrC == 2){
			connType = 2;
			//System.out.println("What is server's ip address? ");
			String ip = serverIP;//sc.nextLine();
			//System.out.println("What port is server listening on? ");
			int port = Integer.parseInt(serverPort);//Integer.parseInt(sc.nextLine());
			try {
				System.out.println("Connecting to server at " + ip + ":" + port);
				clientSock = new Socket(ip, port);
				System.out.println("Connected to server, Starting client thread");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isSP(){
		return spMode;
	}
	
	public void initGame(){
		
		System.out.println("Player using: "+p1Controller.getName());
		initGameObjects();
		tm = new TimedEventManager(this, dBall1, dBall2, dBall3, player1, player2);
		associateAllActions(camera1, currPlayer, p1Controller, currPlayer.getDir());
		
		updateAudio();
		setEarParameters();
		
		initPhysicsSystem();
		createPhysicsWorld();
		
		if(spMode){
			enemy = new NPCcontroller(player2, player1, dBall1, dBall2, dBall3, tm, eventMgr);
		}
		else{
			enemy = new NetworkController(connType, clientSock, currPlayer, this);
		}
		
		startGame();
		super.update(0.0f);
	}
	
	private void initAudio() {
		 AudioResource resource1, resource2, resource3, resource4;
		 audioMgr = AudioManagerFactory.createAudioManager(
		 "sage.audio.joal.JOALAudioManager");
		 
		 if(!audioMgr.initialize()){ 
			 System.out.println("Audio Manager failed to initialize!");
			 return;
		 }
		 
		 resource1 = audioMgr.createAudioResource("sound/walk.wav", AudioResourceType.AUDIO_SAMPLE);
		 resource2 = audioMgr.createAudioResource("sound/swing.wav", AudioResourceType.AUDIO_SAMPLE);
		 resource3 = audioMgr.createAudioResource("sound/hit.wav", AudioResourceType.AUDIO_SAMPLE);
		 resource4 = audioMgr.createAudioResource("sound/heatup.wav", AudioResourceType.AUDIO_STREAM);
		 
		 bgm = new Sound(resource4, SoundType.SOUND_EFFECT, 10, false);
		 bgm.initialize(audioMgr);
		 
		 walkSound1 = new Sound(resource1, SoundType.SOUND_EFFECT, 50, true);
		 throwSound1 = new Sound(resource2, SoundType.SOUND_EFFECT, 100, false);
		 hitSound1 = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		 
		 walkSound1.initialize(audioMgr);
		 throwSound1.initialize(audioMgr);
		 hitSound1.initialize(audioMgr);
		 
		 walkSound1.setMaxDistance(50.0f);
		 walkSound1.setMinDistance(3.0f);
		 walkSound1.setRollOff(1.0f);
		 
		 throwSound1.setMaxDistance(50.0f);
		 throwSound1.setMinDistance(3.0f);
		 throwSound1.setRollOff(1.0f);
		 
		 hitSound1.setMaxDistance(50.0f);
		 hitSound1.setMinDistance(3.0f);
		 hitSound1.setRollOff(1.0f);
		 
		 walkSound2 = new Sound(resource1, SoundType.SOUND_EFFECT, 50, true);
		 throwSound2 = new Sound(resource2, SoundType.SOUND_EFFECT, 100, false);
		 hitSound2 = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		 
		 walkSound2.initialize(audioMgr);
		 throwSound2.initialize(audioMgr);
		 hitSound2.initialize(audioMgr);
		 
		 walkSound2.setMaxDistance(50.0f);
		 walkSound2.setMinDistance(3.0f);
		 walkSound2.setRollOff(1.0f);
		 
		 throwSound2.setMaxDistance(50.0f);
		 throwSound2.setMinDistance(3.0f);
		 throwSound2.setRollOff(1.0f);
		 
		 hitSound2.setMaxDistance(50.0f);
		 hitSound2.setMinDistance(3.0f);
		 hitSound2.setRollOff(1.0f);
		 
	}

	private void setEarParameters() {
		Matrix3D avDir = (Matrix3D) (currPlayer.getWorldRotation().clone());
		float camAz = camMeController.getAzimuth();
		avDir.rotateY(180.0f-camAz);
		Vector3D camDir = new Vector3D(0,0,1);
		camDir = camDir.mult(avDir);
		
		audioMgr.getEar().setLocation(camMeController.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0,1,0));
	}
	
	private void updateAudio(){
		walkSound1.setLocation(new Point3D(player1.getWorldTranslation().getCol(3)));
		throwSound1.setLocation(new Point3D(player1.getWorldTranslation().getCol(3)));
		hitSound1.setLocation(new Point3D(player1.getWorldTranslation().getCol(3)));
		
		walkSound2.setLocation(new Point3D(player2.getWorldTranslation().getCol(3)));
		throwSound2.setLocation(new Point3D(player2.getWorldTranslation().getCol(3)));
		hitSound2.setLocation(new Point3D(player2.getWorldTranslation().getCol(3)));
	}
	
	public Sound getThrowSound(Player p){
		switch(p.getPlayerNum()){
		case 1:
			return throwSound1;
		case 2:
			return throwSound2;
		}
		return null;
	}
	
	public Sound getWalkSound(Player p){
		switch(p.getPlayerNum()){
		case 1:
			return walkSound1;
		case 2:
			return walkSound2;
		}
		return null;
	}
	
	private void startGame(){
		bgm.play();
		gameStart = true;
	}
	
	public boolean gameStarted(){
		return gameStart;
	}
	
	private void timeUp(){
		time = 0;
		timeString.setText("Time: " + (int)(time / 1));
		
		gameOverString = new HUDString("Game Over! Press Enter or X to start new game.");
		gameOverString.setLocation(0.32,0.56);
		gameOverString.setColor(Color.LIGHT_GRAY);
		gameOverString.scale(10, 10, 10);
		camera1.addToHUD(gameOverString);
		
		if(p1score > p2score){
			whoWonString = new HUDString("Player 1 Wins!");
			whoWonString.setLocation(0.44,0.66);
			whoWonString.setColor(Color.red);
		}else if(p2score > p1score){
			whoWonString = new HUDString("Player 2 Wins!");
			whoWonString.setLocation(0.44,0.66);
			whoWonString.setColor(Color.CYAN);
		} else{
			whoWonString = new HUDString("Tie Game!");
			whoWonString.setLocation(0.44,0.66);
			whoWonString.setColor(Color.magenta);
		}
		camera1.addToHUD(whoWonString);
		
		gameStart = false;
	}

	public void update(float elapsedTimeMS){

		Matrix3D mat;
		Vector3D translateVec;
		//For each object in the game world/
		for (SceneNode s : getGameWorld()){
			if(s.getPhysicsObject() != null && !(s instanceof TerrainBlock)){
				s.getPhysicsObject().setSleepThresholds(0, 0);
				mat = new Matrix3D(s.getPhysicsObject().getTransform());
				translateVec = mat.getCol(3);

				s.getLocalTranslation().setCol(3,translateVec);
				if(s instanceof Ball && gameStarted()){
					s.getLocalRotation().setCol(2, mat.getCol(2));
					s.getLocalRotation().setCol(1, mat.getCol(1));
					s.getLocalRotation().setCol(0, mat.getCol(0));

					if(((Ball) s).isHeld()){
						
						Vector3D move;
						
						if(currPlayer.getPlayerNum() != ((Ball) s).getOwner()){
							mat = new Matrix3D(otherPlayer.getLocalTranslation().getFloatValues());
							translateVec = (Vector3D) mat.getCol(3).clone();
						
							if(enemy instanceof NPCcontroller){
								move = (Vector3D) otherPlayer.getDir().clone();
							}
							else {
								move = camTheyController.getViewVector();
							}
						}
						else{
							mat = new Matrix3D(currPlayer.getLocalTranslation().getFloatValues());
							translateVec = (Vector3D) mat.getCol(3).clone();
	
							move = camMeController.getViewVector();
						}

						move = move.normalize();
						translateVec.setY(translateVec.getY() + 2*move.getY());
						translateVec.setX(translateVec.getX() + 2*move.getX());
						translateVec.setZ(translateVec.getZ() + 2*move.getZ());

						((Ball) s).getLocalTranslation().setCol(3, translateVec);
						((Ball) s).getPhysicsObject().setTransform(((Ball) s).getLocalTranslation().getValues());
						
					}
				}

				if(gameStarted() && (s.equals(currPlayer) || (enemy instanceof NPCcontroller && s instanceof Player))){
					s.updateWorldBound();
					if(time < maxTime - 1){
						if(s.getWorldBound().intersects(dBall1.getWorldBound())){
							if(!dBall1.isHeld() && dBall1.getOwner() == 0){
								CollisionEvent newCollision = new CollisionEvent((Player) s, dBall1);
								eventMgr.triggerEvent(newCollision);
							} else if(!dBall1.isHeld() && dBall1.getOwner() != ((Player)s).getPlayerNum()){
								ballHitPlayer(((Player)s));
							}
						}
						if(s.getWorldBound().intersects(dBall2.getWorldBound())){
							if(!dBall2.isHeld() && dBall2.getOwner() == 0){
								CollisionEvent newCollision = new CollisionEvent((Player) s, dBall2);
								eventMgr.triggerEvent(newCollision);
							} else if(!dBall2.isHeld() && dBall2.getOwner() != ((Player)s).getPlayerNum()){
								ballHitPlayer(((Player)s));
							}
						}
						if(s.getWorldBound().intersects(dBall3.getWorldBound())){
							if(!dBall3.isHeld() && dBall3.getOwner() == 0){
								CollisionEvent newCollision = new CollisionEvent((Player) s, dBall3);
								eventMgr.triggerEvent(newCollision);
							} else if(!dBall3.isHeld() && dBall3.getOwner() != ((Player)s).getPlayerNum()){
								ballHitPlayer(((Player)s));
							}
						}
					}
				}
			}
		}
		
		skybox.setLocalTranslation(currPlayer.getWorldTranslation());
		
		camMeController.update(elapsedTimeMS);
		camTheyController.update(elapsedTimeMS);
		
		p1scoreString.setText("Score: " + p1score);
		p2scoreString.setText("Score: " + p2score);
		
		player1.getModel().updateAnimation(elapsedTimeMS);
		player2.getModel().updateAnimation(elapsedTimeMS);

		enemy.updateGameState(elapsedTimeMS);
		processMessage(enemy.getGameState());
		
		if(gameStart){		
			time -= elapsedTimeMS/1000;
			
			if(time <= 0){
				timeUp();
			}
			else
				timeString.setText("Time: " + (int)(time / 1));

			
			physicsEngine.update(elapsedTimeMS);
		}
		tm.update(elapsedTimeMS);

		super.update(elapsedTimeMS);

		updateAudio();
		setEarParameters();
		
	}
	
	private void ballHitPlayer(Player p) {
		switch(p.getPlayerNum()){
		case 1:
			if(!tm.isInvinc(player1)){
				p2score++;
				player2.setScore(p2score);
				tm.startInvincibility(player1);
				hitSound1.play();
				if(currPlayer.getPlayerNum() == 1)
					unhandledHit = true;
			}
			break;
		case 2:
			if(!tm.isInvinc(player2)){
				p1score++;
				player1.setScore(p1score);
				tm.startInvincibility(player2);
				hitSound2.play();
				if(currPlayer.getPlayerNum() == 2)
					unhandledHit = true;
			}
			break;
		}
	}

	private IDisplaySystem createDisplaySystem(IDisplaySystem mySys){
		IDisplaySystem display1 = mySys;

		System.out.print("\nWaiting for display creation...");
		int count = 0;
		// wait until display creation completes or a timeout occurs
		while (!display1.isCreated()){
			try{ 
				Thread.sleep(10); 
			}catch(InterruptedException e){ 
				throw new RuntimeException("Display creation interrupted"); }
			count++;
			System.out.print("+");
			if (count % 80 == 0) { System.out.println(); }
			if (count > 2000){ // 20 seconds (approx.)
				throw new RuntimeException("Unable to create display");
			}
		}
		System.out.println();
		return display1;
	}
	
	private void runScript(String scriptFileName){
		try{ 
			FileReader fileReader = new FileReader(scriptFileName);
			engine.eval(fileReader); //execute the script statements in the file
			fileReader.close();
		}
		catch (FileNotFoundException e1){ 
			System.out.println(scriptFileName + " not found " + e1); 
		}
		catch (IOException e2){ 
			System.out.println("IO problem with " + scriptFileName + e2); 
		}
		catch (ScriptException e3){ 
			System.out.println("ScriptException in " + scriptFileName + e3); 
		}
		catch (NullPointerException e4){ 
			System.out.println ("Null ptr exception in " + scriptFileName + e4); 
		}
	}
	
	public void listControllers(Controller[] cs){
		for (int i=0; i < cs.length; i++){
			System.out.println("Controller #" + i + ": " + cs[i].getName() + " :: " + cs[i].getType());

		}
	}
	
	private void createPlayerHUDs(){
		
		HUDString player1ID = new HUDString("Player1");
		player1ID.setName("Player1ID");
		player1ID.setLocation(0.01, 0.95);
		player1ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player1ID.setColor(Color.red);
		player1ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		
		p1scoreString = new HUDString("Score: " + p1score);
		p1scoreString.setColor(Color.MAGENTA);
		p1scoreString.setLocation(0.01, 0.90);
		
		camera1.addToHUD(player1ID);
		camera1.addToHUD(p1scoreString);
		
		HUDString player2ID = new HUDString("Player2");
		player2ID.setName("Player2ID");
		player2ID.setLocation(0.90, 0.95);
		player2ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player2ID.setColor(Color.CYAN);
		player2ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		
		p2scoreString = new HUDString("Score: " + p2score);
		p2scoreString.setColor(Color.MAGENTA);
		p2scoreString.setLocation(0.90, 0.90);
		
		camera1.addToHUD(player2ID);
		camera1.addToHUD(p2scoreString);
		
		timeString = new HUDString("Time = " + time);
		timeString.setLocation(0.46,0.95);
		timeString.setColor(Color.LIGHT_GRAY);
		camera1.addToHUD(timeString);
	}

	private void initGameObjects(){
		
		skybox = new SkyBox("Default box", 200f, 200f, 200f);
		skybox.setTexture(Face.Up, TextureManager.loadTexture2D("textures/FullMoonUp2048.png"));
		skybox.setTexture(Face.Down, TextureManager.loadTexture2D("textures/FullMoonDown2048.png"));
		skybox.setTexture(Face.North, TextureManager.loadTexture2D("textures/FullMoonBack2048.png"));
		skybox.setTexture(Face.East, TextureManager.loadTexture2D("textures/FullMoonRight2048.png"));
		skybox.setTexture(Face.South, TextureManager.loadTexture2D("textures/FullMoonFront2048.png"));
		skybox.setTexture(Face.West, TextureManager.loadTexture2D("textures/FullMoonLeft2048.png"));
		this.addGameWorldObject(skybox);
		
		ImageBasedHeightMap myHeightMap = new ImageBasedHeightMap("textures/terrainmap.png");
		imageTerrain = createTerBlock(myHeightMap);
		
		TextureState grassState;
		Texture grassTexture = TextureManager.loadTexture2D("textures/moon.jpg");
		grassTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		grassState = (TextureState) display.getRenderer().createRenderState(RenderStateType.Texture);
		grassState.setTexture(grassTexture, 0);
		grassState.setEnabled(true);
		
		int sizeMod = 3;
		imageTerrain.setRenderState(grassState);
		imageTerrain.translate(-imageTerrain.getSize()/(10/sizeMod), 0, -imageTerrain.getSize()/(10/sizeMod));
		imageTerrain.scale(sizeMod, 10, sizeMod);
		
		this.addGameWorldObject(imageTerrain);
		
		this.runScript(nodeInitScript); 
		
		rootNode = (SceneNode) engine.get("rootNode");
		
		player1 = (Player) engine.get("player1");
		player2 = (Player) engine.get("player2");
		
		player1.setGame(this);
		player2.setGame(this);

		Group modelOne, modelTwo;
		Model3DTriMesh p1model = null, p2model = null;

		OgreXMLParser loader = new OgreXMLParser();
		
		try{ 
			modelOne = loader.loadModel("model/cat.mesh.xml",
					"model/cat.material",
					"model/cat.skeleton.xml");
			modelOne.updateGeometricState(0, true);
			java.util.Iterator<SceneNode> modelIterator = modelOne.iterator();
			p1model = (Model3DTriMesh) modelIterator.next();
			
			modelTwo = loader.loadModel("model/cat.mesh.xml",
					"model/cat.material",
					"model/cat.skeleton.xml");
			modelTwo.updateGeometricState(0, true);
			modelIterator = modelTwo.iterator();
			p2model = (Model3DTriMesh) modelIterator.next();
		}catch (Exception e){ 
			e.printStackTrace();
			System.exit(1);
		}
		
		player1.setModel(p1model);
		player2.setModel(p2model);
		
		player1.setSkin(0);
		player2.setSkin(0);
		
		if(connType == 2){
			currPlayer = player2;
			otherPlayer = player1;
			
			OrbitCameraController camController = new OrbitCameraController(null, otherPlayer, null, null);
			camTheyController = camController;
			otherPlayer.setCam(camTheyController);
		}
		else{
			currPlayer = player1;
			otherPlayer = player2;
			
			OrbitCameraController camController = new OrbitCameraController(null, otherPlayer, null, null);
			camTheyController = camController;
			otherPlayer.setCam(camTheyController);
		}
		
		dBall1 = (Ball) engine.get("dball1");
		this.addGameWorldObject(dBall1);
		
		dBall2 = (Ball) engine.get("dball2");
		this.addGameWorldObject(dBall2);
		
		dBall3 = (Ball) engine.get("dball3");
		this.addGameWorldObject(dBall3);
		
		Texture gTex = TextureManager.loadTexture2D("textures/check.png");
		gTex.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		plane1 = (Rectangle) engine.get("plane");
		plane1.setTexture(gTex);
		plane1.getWorldTranslation().translate(0, -0.01f, 0);
		
		wallN = (Rectangle) engine.get("wall1");
		wallS = (Rectangle) engine.get("wall2");
		wallE = (Rectangle) engine.get("wall3");
		wallW = (Rectangle) engine.get("wall4");
		
		this.addGameWorldObject(plane1);
//		this.addGameWorldObject(wallE);
//		this.addGameWorldObject(wallW);
//		this.addGameWorldObject(wallN);
//		this.addGameWorldObject(wallS);
		
		this.addGameWorldObject(player1);
		this.addGameWorldObject(player2);
		
		player1.getWorldTranslation().translate(0, 1, 0);
		player2.getWorldTranslation().translate(0, 1, 0);
		this.addGameWorldObject(rootNode);

		createPlayerHUDs();
		
		eventMgr.addListener(player1, CollisionEvent.class);
		eventMgr.addListener(player1, ThrowEvent.class);
		
		eventMgr.addListener(player2, CollisionEvent.class);
		eventMgr.addListener(player2, ThrowEvent.class);

	}

	private TerrainBlock createTerBlock(AbstractHeightMap heightMap){ 
		float heightScale = .005f;
		Vector3D terrainScale = new Vector3D(.2, heightScale, .2);
		// use the size of the height map as the size of the terrain
		int terrainSize = heightMap.getSize();
		// specify terrain origin so heightmap (0,0) is at world origin
		float cornerHeight =
				heightMap.getTrueHeightAtPoint(0, 0) * heightScale;
		Point3D terrainOrigin = new Point3D(0, -cornerHeight, 0);
		// create a terrain block using the height map
		String name = "Terrain:" + heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale,
				heightMap.getHeightData(), terrainOrigin);
		return tb;
	}

	protected void initPhysicsSystem(){ 
		String engine = "sage.physics.JBullet.JBulletPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		float[] gravity = {0, -9.8f, 0};
		physicsEngine.setGravity(gravity);
	}
	
	private void createPhysicsWorld(){
		float ballMass = 1.0f;
		float bounciness = 0.7f;
		float angularDamping = 0.7f;
		float linearDamping = 0.3f;
		float friction = 0.7f;
		
		ball1 = physicsEngine.addSphereObject(physicsEngine.nextUID(), ballMass, dBall1.getWorldTransform().getValues(), 1.0f);
		ball1.setBounciness(bounciness);
		ball1.setDamping(linearDamping, angularDamping);
		ball1.setFriction(friction);
		dBall1.setPhysicsObject(ball1);
		
		ball2 = physicsEngine.addSphereObject(physicsEngine.nextUID(), ballMass, dBall2.getWorldTransform().getValues(), 1.0f);
		ball2.setBounciness(bounciness);
		ball2.setDamping(linearDamping, angularDamping);
		ball2.setFriction(friction);
		dBall2.setPhysicsObject(ball2);
	
		ball3 = physicsEngine.addSphereObject(physicsEngine.nextUID(), ballMass, dBall3.getWorldTransform().getValues(), 1.0f);
		ball3.setBounciness(bounciness);
		ball3.setDamping(linearDamping, angularDamping);
		ball3.setFriction(friction);
		dBall3.setPhysicsObject(ball3);
		
		float hEx[] = {1, 2, 2};
		
		p1 = physicsEngine.addCylinderObject(physicsEngine.nextUID(), 100.0f, player1.getWorldTransform().getValues(), hEx);
		p1.setBounciness(0.2f);
		p1.setDamping(0.9f, 1f);
		player1.setPhysicsObject(p1);
		
		p2 = physicsEngine.addCylinderObject(physicsEngine.nextUID(), 100.0f, player2.getWorldTransform().getValues(), hEx);
		p2.setBounciness(0.2f);
		p2.setDamping(0.9f, 1f);
		player2.setPhysicsObject(p2);

		float up[] = {0, 1, 0};
		terrain = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), plane1.getWorldTransform().getValues(), up, 0.0f);
		terrain.setBounciness(bounciness);
		terrain.setDamping(linearDamping, angularDamping);
		terrain.setFriction(friction);
		plane1.setPhysicsObject(terrain);
		
		plane1.translate(0, 1, 0);
		plane1.getPhysicsObject().setTransform(plane1.getLocalTranslation().getValues());
		
		float inE[] = {0, 0, -1};
		wE = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), wallE.getWorldTransform().getValues(), inE, 0.0f);
		wE.setBounciness(bounciness);
		wE.setDamping(linearDamping, angularDamping);
		wE.setFriction(friction);
		wallE.setPhysicsObject(wE);
		
		wallE.getWorldTranslation().translate(0, 0, 25);
		wallE.getPhysicsObject().setTransform(wallE.getLocalTranslation().getValues());
		
		float inW[] = {0, 0, 1};
		wW = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), wallW.getWorldTransform().getValues(), inW, 0.0f);
		wW.setBounciness(bounciness);
		wW.setDamping(linearDamping, angularDamping);
		wW.setFriction(friction);
		wallW.setPhysicsObject(wW);
		
		wallW.getWorldTranslation().translate(0, 0, -25);
		wallW.getPhysicsObject().setTransform(wallW.getLocalTranslation().getValues());
		
		float inN[] = {-1, 0, 0};
		wN = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), wallN.getWorldTransform().getValues(), inN, 0.0f);
		wN.setBounciness(bounciness);
		wN.setDamping(linearDamping, angularDamping);
		wN.setFriction(friction);
		wallN.setPhysicsObject(wN);
		
		wallN.getWorldTranslation().translate(25, 0, 0);
		wallN.getPhysicsObject().setTransform(wallN.getLocalTranslation().getValues());
		
		float inS[] = {1, 0, 0};
		wS = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), wallS.getWorldTransform().getValues(), inS, 0.0f);
		wS.setBounciness(bounciness);
		wS.setDamping(linearDamping, angularDamping);
		wS.setFriction(friction);
		wallS.setPhysicsObject(wS);
		
		wallS.getWorldTranslation().translate(-25, 0, 0);
		wallS.getPhysicsObject().setTransform(wallS.getLocalTranslation().getValues());
	}

	private void associateAllActions(ICamera camera, Player player, Controller pCont, Vector3D dir){
		IAction quitGame = new QuitGameAction(this);
		
		IAction moveForward = new MoveForwardAction(player, dir, tm);
		IAction moveBackward = new MoveBackwardAction(player, dir, tm);
		IAction turnLeft = new TurnLeftAction(player, dir, tm);
		IAction turnRight = new TurnRightAction(player, dir, tm);
		
		IAction moveFB = new MoveFBAction(player, dir, tm);
		IAction turnLR = new TurnLRAction(player, dir, tm);
		
		IAction gpThrow = new ThrowAction();
		IAction reset = new resetAction();
		
		IAction toggleSkinLeft = new ToggleSkinAction(player, -1);
		IAction toggleSkinRight = new ToggleSkinAction(player, 1);
		
		OrbitCameraController camController = new OrbitCameraController(camera, player, im, pCont);
		camMeController = camController;
		player.setCam(camMeController);
		
		if(pCont.getType().equals(Controller.Type.KEYBOARD)){
			im.associateAction (
					pCont, net.java.games.input.Component.Identifier.Key.ESCAPE,
					quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(pCont, Component.Identifier.Key.W, moveForward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(pCont, Component.Identifier.Key.S, moveBackward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(pCont, Component.Identifier.Key.A, turnLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(pCont, Component.Identifier.Key.D, turnRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			im.associateAction(pCont, Component.Identifier.Key.RETURN, reset, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			im.associateAction(pCont, Component.Identifier.Key.LEFT, toggleSkinLeft, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(pCont, Component.Identifier.Key.RIGHT, toggleSkinRight, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}
		
		if(pCont.getType().equals(Controller.Type.GAMEPAD)){
			im.associateAction(pCont, Component.Identifier.Axis.Y, moveFB, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(pCont, Component.Identifier.Axis.X, turnLR, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			im.associateAction(pCont, Component.Identifier.Button._5, gpThrow, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(pCont, Component.Identifier.Button._9, quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(pCont, Component.Identifier.Button._1, reset, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			im.associateAction(pCont, Component.Identifier.Axis.POV, toggleSkinLeft, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);		
		}
	}

	private void processMessage(String message) {
		
		if(message != null){
			switch(connType){
			case 1:{
				//message syntax: xpos,ypos,zpos,azi,elev,ballheld,throwing?,throw,colliding?,moving?,reset,skin#
				String[] msg = message.split(",");

				//Player 2 position
				float x = Float.parseFloat(msg[0]);
				float y = Float.parseFloat(msg[1]);
				float z = Float.parseFloat(msg[2]);
				//Player 2 orientation
				float azi = Float.parseFloat(msg[3]);
				float ele = Float.parseFloat(msg[4]);
				//Which ball being held
				int ballHeld = Integer.parseInt(msg[5]);
				//If throwing ball, velocity of ball
				boolean throwing = Boolean.parseBoolean(msg[6]);
				float velx = Float.parseFloat(msg[7]);
				float vely = Float.parseFloat(msg[8]);
				float velz = Float.parseFloat(msg[9]);
				//Hit by ball?
				boolean gotHit = Boolean.parseBoolean(msg[10]);
				//Moving?
				boolean moving = Boolean.parseBoolean(msg[11]);
				//reset
				int start = Integer.parseInt(msg[12]);
				//skin
				int skin = Integer.parseInt(msg[13]);

				Vector3D p2pos = new Vector3D(x, y, z);
				Matrix3D pos = (Matrix3D) otherPlayer.getLocalTranslation().clone();
				pos.setCol(3, p2pos);
				otherPlayer.getPhysicsObject().setTransform(pos.getValues());

				float angle = azi;
				if(gameStarted()){
				Matrix3D rotation = new Matrix3D();
				rotation.rotate(angle + 180, new Vector3D(0, 1, 0));
				otherPlayer.setLocalRotation(rotation);
				}
				
				otherPlayer.getCam().setAziEle(azi, ele);
				
				if(ballHeld > 0 && !otherPlayer.isHoldingBall()){
					Ball collBall;
					switch(ballHeld){
					case 1:
						collBall = dBall1;
						break;
					case 2:
						collBall = dBall2;
						break;
					case 3:
						collBall = dBall3;
						break;
					default:
						collBall = null;
					}
					CollisionEvent newCollision = new CollisionEvent(otherPlayer, collBall);
					eventMgr.triggerEvent(newCollision);
				}
				
				if(throwing && otherPlayer.isHoldingBall()){
					ThrowEvent newThrowEvent = new ThrowEvent(otherPlayer, otherPlayer.getCam());
					eventMgr.triggerEvent(newThrowEvent);
					tm.startBallTimer(otherPlayer.getHeldBall());
					
					float vec[] = {velx, vely, velz};
					otherPlayer.getHeldBall().getPhysicsObject().setLinearVelocity(vec);
				}
				
				if(gotHit && !tm.isInvinc(otherPlayer)){
					ballHitPlayer(otherPlayer);
				}
				
				if(moving){
					if(!otherPlayer.getModel().isAnimating()){
						otherPlayer.getModel().startAnimation("Walk");
						getWalkSound(otherPlayer).play();
					}
					tm.startAnimTimer(otherPlayer);
				}
				
				if(start == 1){
					startGame();
				} else if(start == 2){
					resetGame();
				}
				
				if(otherPlayer.getSkin() != skin)
					otherPlayer.setSkin(skin);
				
				break;}
			case 2:{
				//message syntax: xpos,ypos,zpos,azi,elev,ballheld,throwing?,throw,colliding?,moving?,reset,skin#
				String[] msg = message.split(",");

				//Player 2 position
				float x = Float.parseFloat(msg[0]);
				float y = Float.parseFloat(msg[1]);
				float z = Float.parseFloat(msg[2]);
				//Player 2 orientation
				float azi = Float.parseFloat(msg[3]);
				float ele = Float.parseFloat(msg[4]);
				//Which ball being held
				int ballHeld = Integer.parseInt(msg[5]);
				//If throwing ball, velocity of ball
				boolean throwing = Boolean.parseBoolean(msg[6]);
				float velx = Float.parseFloat(msg[7]);
				float vely = Float.parseFloat(msg[8]);
				float velz = Float.parseFloat(msg[9]);
				//Hit by ball?
				boolean gotHit = Boolean.parseBoolean(msg[10]);
				//Moving?
				boolean moving = Boolean.parseBoolean(msg[11]);
				//reset
				int start = Integer.parseInt(msg[12]);
				//skin
				int skin = Integer.parseInt(msg[13]);

				Vector3D p2pos = new Vector3D(x, y, z);
				Matrix3D pos = (Matrix3D) otherPlayer.getLocalTranslation().clone();
				pos.setCol(3, p2pos);
				otherPlayer.getPhysicsObject().setTransform(pos.getValues());

				float angle = azi;
				if(gameStarted()){
				Matrix3D rotation = new Matrix3D();
				rotation.rotate(angle + 180, new Vector3D(0, 1, 0));
				otherPlayer.setLocalRotation(rotation);
				}
				otherPlayer.getCam().setAziEle(azi, ele);
				
				if(ballHeld > 0 && !otherPlayer.isHoldingBall()){
					Ball collBall;
					switch(ballHeld){
					case 1:
						collBall = dBall1;
						break;
					case 2:
						collBall = dBall2;
						break;
					case 3:
						collBall = dBall3;
						break;
					default:
						collBall = null;
					}
					CollisionEvent newCollision = new CollisionEvent(otherPlayer, collBall);
					eventMgr.triggerEvent(newCollision);
				}
				
				if(throwing && otherPlayer.isHoldingBall()){
					ThrowEvent newThrowEvent = new ThrowEvent(otherPlayer, otherPlayer.getCam());
					eventMgr.triggerEvent(newThrowEvent);
					tm.startBallTimer(otherPlayer.getHeldBall());
					
					float vec[] = {velx, vely, velz};
					otherPlayer.getHeldBall().getPhysicsObject().setLinearVelocity(vec);
				}
				
				if(gotHit && !tm.isInvinc(otherPlayer)){
					ballHitPlayer(otherPlayer);
				}

				if(moving){
					if(!otherPlayer.getModel().isAnimating()){
						otherPlayer.getModel().startAnimation("Walk");
						getWalkSound(otherPlayer).play();
					}
					tm.startAnimTimer(otherPlayer);
				}
				
				if(start == 1){
					startGame();
				} else if(start == 2){
					resetGame();
				}
				
				if(otherPlayer.getSkin() != skin)
					otherPlayer.setSkin(skin);
			}
			}
		}
	}
	
	public String createMessage(){
		switch(currPlayer.getPlayerNum()){
		case 1:{
			String message1 = new String();
			Vector3D pos1 = currPlayer.getLocalTranslation().getCol(3);
			message1 += pos1.getX() + ",";
			message1 += pos1.getY() + ",";
			message1 += pos1.getZ() + ",";
			
			message1 += camMeController.getAzimuth() + ",";
			message1 += camMeController.getElevation() + ",";
			
			if(currPlayer.isHoldingBall() && gameStarted())
				message1 += currPlayer.getHeldBall().getID() + ",";
			else
				message1 += "0,";
			
			if(unhandledThrow){
				message1 += "true,";
				float vec[] = currPlayer.getHeldBall().getPhysicsObject().getLinearVelocity();
				message1 += vec[0] + ",";
				message1 += vec[1] + ",";
				message1 += vec[2] + ",";
				unhandledThrow = false;
			} else
				message1 += "false,0,0,0,";
			
			if(unhandledHit){
				message1 += "true,";
				unhandledHit = false;
			}
			else
				message1 += "false,";
			
			if(tm.isMoving(currPlayer)){
				message1 += "true,";
			}
			else
				message1 += "false,";
			
			if(unhandledReset){
				message1 += "2,";
				unhandledReset = false;
			}
			else
				message1 += "0,";
			
			message1 += currPlayer.getSkin() + ",\r\n";

			return message1;}
		case 2:{
			String message1 = new String();
			Vector3D pos1 = currPlayer.getLocalTranslation().getCol(3);
			message1 += pos1.getX() + ",";
			message1 += pos1.getY() + ",";
			message1 += pos1.getZ() + ",";
			
			message1 += camMeController.getAzimuth() + ",";
			message1 += camMeController.getElevation() + ",";
			
			if(currPlayer.isHoldingBall() && gameStarted())
				message1 += currPlayer.getHeldBall().getID() + ",";
			else
				message1 += "0,";
			
			if(unhandledThrow){
				message1 += "true,";
				float vec[] = currPlayer.getHeldBall().getPhysicsObject().getLinearVelocity();
				message1 += vec[0] + ",";
				message1 += vec[1] + ",";
				message1 += vec[2] + ",";
				unhandledThrow = false;
			} else
				message1 += "false,0,0,0,";
			
			if(unhandledHit){
				message1 += "true,";
				unhandledHit = false;
			}
			else
				message1 += "false,";
			
			if(tm.isMoving(currPlayer)){
				message1 += "true,";
			}
			else
				message1 += "false,";
			
			if(unhandledReset){
				message1 += "2,";
				unhandledReset = false;
			}
			else
				message1 += "0,";
			
			message1 += currPlayer.getSkin() + ",\r\n";
			
			return message1;}
		}
		return null;
	}
	
	private void initMouseMode(){
		Dimension dim = getRenderer().getCanvas().getSize();
		cCenter = new Point(dim.width/2, dim.height/2);
		isRecentering = false;
		
		try{ 
			robot = new Robot();
		} 
		catch(AWTException ex){ 
			throw new RuntimeException("Couldn't create Robot!"); 
		}
		
		recenterMouse();
		
		prevMouseX = cCenter.x; // 'prevMouse' defines the initial
		prevMouseY = cCenter.y; // mouse position
		
		// also demonstrates changing the cursor
		getRenderer().getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	private void recenterMouse(){
		isRecentering = true;
		Point p = new Point(cCenter.x, cCenter.y);
		Canvas canvas = getRenderer().getCanvas();
		SwingUtilities.convertPointToScreen(p, canvas);
		robot.mouseMove(p.x, p.y);	
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if(mousePressed >= 1){
			mouseMoved(arg0);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(mousePressed >= 1){
		if (isRecentering && cCenter.x == e.getX() && cCenter.y == e.getY()){
			// mouse has been recentered, recentering is complete
			isRecentering = false;
		} else{ // event was due to a user mouse-move, and so it must be processed
			curMouseX = e.getX();
			curMouseY = e.getY();
			float mouseDeltaX = prevMouseX - curMouseX;
			float mouseDeltaY = prevMouseY - curMouseY;
			camMeController.yaw(mouseDeltaX);
			camMeController.pitch(mouseDeltaY);
			
			if(gameStarted()){
				float angle = camMeController.getAzimuth();
				Matrix3D rotation = new Matrix3D();
				rotation.rotate(angle + 180, new Vector3D(0, 1, 0));
				currPlayer.setLocalRotation(rotation);
			}
			prevMouseX = curMouseX;
			prevMouseY = curMouseY;
			// tell robot to put the cursor back to the
			recenterMouse();
			prevMouseX = cCenter.x;
			prevMouseY = cCenter.y;
		}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		getRenderer().getCanvas().setCursor(getRenderer().getCanvas().getToolkit().createCustomCursor(
	            new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
	            "null"));
		if(gameStart){
			if(arg0.getButton() == MouseEvent.BUTTON1 && currPlayer.isHoldingBall()){
				ThrowEvent newThrowEvent = new ThrowEvent(currPlayer, camMeController);
				eventMgr.triggerEvent(newThrowEvent);
				tm.startBallTimer(currPlayer.getHeldBall());
				unhandledThrow = true;
			}
		}
		mousePressed++;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		mousePressed--;
		if(mousePressed == 0)
			getRenderer().getCanvas().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	protected void render(){
		super.render();
	}
	
	public void resetGame(){
		gameStart = false;
		
		p1score = 0;
		p2score = 0;
		time = maxTime;
		unhandledThrow = false;
		unhandledHit = false;
		
		Matrix3D ident = new Matrix3D();
		player1.setWorldTranslation((Matrix3D) ident.clone());
		player1.setWorldRotation((Matrix3D) ident.clone());
		player1.getWorldTranslation().translate(0, 3f, 20);
		player1.setDir(new Vector3D(0, 0, 1));
		player1.rotate(180, new Vector3D(0, 1, 0));
		
		player2.setWorldTranslation((Matrix3D) ident.clone());
		player2.setWorldRotation((Matrix3D) ident.clone());
		player2.getWorldTranslation().translate(0, 3f, -20);
		player2.setDir(new Vector3D(0, 0, 1));
		player2.rotate(0, new Vector3D(0, 1, 0));
		
		player1.getPhysicsObject().setTransform(player1.getWorldTransform().getValues());
		player2.getPhysicsObject().setTransform(player2.getWorldTransform().getValues());

		if(player1.getHeldBall() != null)
			player1.getHeldBall().changeHoldState(0, false);
		player1.unHold();

		if(player2.getHeldBall() != null)
			player2.getHeldBall().changeHoldState(0, false);
		player2.unHold();
		
		player1.getCam().setAziEle(0, 20);
		player2.getCam().setAziEle(180, 20);

		dBall1.setWorldTranslation((Matrix3D) ident.clone());
		dBall2.setWorldTranslation((Matrix3D) ident.clone());
		dBall3.setWorldTranslation((Matrix3D) ident.clone());
		
		dBall1.getWorldTranslation().translate(0, 2, 0);
		dBall2.getWorldTranslation().translate(10, 2, 0);
		dBall3.getWorldTranslation().translate(-10, 2, 0);
		
		dBall1.getPhysicsObject().setTransform(dBall1.getWorldTransform().getValues());
		dBall2.getPhysicsObject().setTransform(dBall2.getWorldTransform().getValues());
		dBall3.getPhysicsObject().setTransform(dBall3.getWorldTransform().getValues());

		float vec[] = {0, 0, 0};
		dBall1.getPhysicsObject().setLinearVelocity(vec);
		dBall2.getPhysicsObject().setLinearVelocity(vec);
		dBall3.getPhysicsObject().setLinearVelocity(vec);
		
		dBall1.getPhysicsObject().setAngularVelocity(vec);
		dBall2.getPhysicsObject().setAngularVelocity(vec);
		dBall3.getPhysicsObject().setAngularVelocity(vec);
		
		p1scoreString.setText("Score: " + p1score);
		p2scoreString.setText("Score: " + p2score);
		timeString.setText("Time: " + (int)(time / 1));
		
		camera1.removeFromHUD(gameOverString);
		camera1.removeFromHUD(whoWonString);

		startGame();
	}
	
	private class ThrowAction extends AbstractInputAction{

		@Override
		public void performAction(float arg0, Event arg1) {
			if(currPlayer.isHoldingBall()){
				ThrowEvent newThrowEvent = new ThrowEvent(currPlayer, camMeController);
				eventMgr.triggerEvent(newThrowEvent);
				tm.startBallTimer(currPlayer.getHeldBall());
				unhandledThrow = true;
			}
		}
		
	}
	
	private class resetAction extends AbstractInputAction{

		@Override
		public void performAction(float arg0, Event arg1) {
			if(!gameStarted()){
				unhandledReset = true;
				resetGame();
			}
		}
		
	}
	
}

