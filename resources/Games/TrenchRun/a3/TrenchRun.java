package a3;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.ImageIcon;

import a3.objects.*;
import client.MyClient;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import gameEngine.Grid;
import gameEngine.actions.*;
import gameEngine.camera.Camera3Pkeyboard;
import gameEngine.controllers.LaserController;
import gameEngine.controllers.MoveTrenchController;
import gameEngine.controllers.SmokeController;
import gameEngine.display.MyDisplaySystem;
import net.java.games.input.Controller;
import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.networking.IGameConnection.ProtocolType;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.renderer.IRenderer;
import sage.scene.Group;
import sage.scene.HUDString;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import sage.scene.SceneNode.CULL_MODE;
import sage.scene.SceneNode.RENDER_MODE;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.shape.Line;
import sage.scene.shape.Rectangle;
import sage.scene.state.BlendState;
import sage.scene.state.RenderState;
import sage.scene.state.RenderState.RenderStateType;
import sage.scene.state.TextureState;
import sage.terrain.ImageBasedHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;
import sage.texture.Texture.ApplyMode;

public class TrenchRun extends BaseGame{

	private final static int MAXPLAYERS = 4;
	private final static String VERSION = "1.2";
	private static final int GRIDWIDTH = 4;
	private static final int GRIDHEIGHT = 4;
	
	//Ship models and textures
	private static String SHIPMODELICARUSLOCATION = "./objs/ShipIcarus.obj";
	private static String SHIPTEXTUREICARUSLOCATION = "./textures/IcarusGreen.png";
	private static String SHIPMODELANDARLOCATION = "./objs/ShipAndar.obj";
	private static String SHIPTEXTUREANDARLOCATION = "./textures/ShipTextureAndar.jpg";
	private static String BRIDGEMODELLOCATION = "./objs/BridgeObstacle.obj";
	private static String BRIDGETEXTURELOCATION = "./textures/BridgeObstacleTexture.jpg";
	private static String TOWERMODELLOCATION = "./objs/TowerObstacle.obj";
	private static String TOWERTEXTURELOCATION = "./textures/TowerObstacleTexture.jpg";
	private static String TURRETMODELLOCATION = "./objs/Turret.obj";
	private static String TURRETTEXTURELOCATION = "./textures/TurretTexture.jpg";
	private Texture icarusTexture = TextureManager.loadTexture2D(SHIPTEXTUREICARUSLOCATION);
	private Texture andarTexture = TextureManager.loadTexture2D(SHIPTEXTUREANDARLOCATION);
	private Texture bridgeObstacleTexture = TextureManager.loadTexture2D(BRIDGETEXTURELOCATION);
	private Texture towerObstacleTexture = TextureManager.loadTexture2D(TOWERTEXTURELOCATION);
	private Texture turretObstacleTexture = TextureManager.loadTexture2D(TURRETTEXTURELOCATION);
	
	// Physics Engine and Objects
	private Rectangle groundPlane;
	
	private IPhysicsEngine physicsEngine;
	private IPhysicsObject groundPlaneP;
	private IPhysicsObject shipP;
	
	//Sound locations
	private static String SOUNDRESOURCEONELOCATION = "./sounds/laser1.wav";
	private static String SOUNDRESOURCETWOLOCATION = "./sounds/meep_merp.wav";
	private static String SOUNDRESOURCETHREELOCATION = "./sounds/CodyLaser.wav";
	private static String SOUNDRESOURCEFOURLOCATION = "./sounds/alarm.wav";
	private static String SOUNDRESOURCEFIVELOCATION = "./sounds/fizzle.wav";
	private static String SOUNDRESOURCESIXLOCATION = "./sounds/Power_Failure.wav";
	private static String SOUNDRESOURCESEVENLOCATION = "./sounds/Explosion.wav";
	private static String SOUNDRESOURCEEIGHTLOCATION = "./sounds/Bone_Crush.wav";
	private static String SOUNDRESOURCENINELOCATION = "./sounds/Andar.wav";
	private static String SOUNDRESOURCETENLOCATION = "./sounds/Icarus.wav";
	private static String SOUNDRESOURCEELEVENLOCATION = "./sounds/Ding.wav";
	private static String MUSICRESOURCEONELOCATION = "./sounds/Masters.wav";
	private static String MUSICRESOURCETWOLOCATION = "./sounds/DangerZone.wav";
	
	private static Color GRIDCOLOR = Color.blue;
	private static double LASERLENGTH = 6.0;
	
	private Player[] playersInGame = new Player[MAXPLAYERS+1];//Allow for 4 players AND an initial playerNum=0
	private Color[] playerColors = new Color[] {Color.white,Color.red,Color.green,Color.cyan,Color.magenta};
	private int currentTrenchNumber = 1;
	private int deleteTrenchNumber = 1;
	private TerrainBlock[] terrainList = new TerrainBlock[10];
	private TextureState[] terrainTexture = new TextureState[10];
	private Group rootGroup;
	private TriMesh bridgeObstacle, towerObstacle, turretObstacle;
	private Group laserGroup;
	private Group smokeGroup;
	private Group lobbyLineGroup;
	private Group lobbyGroup, lobbyTowerGroup;
	private MoveTrenchController moveTrench;
	private SmokeController moveSmoke;
	private Grid gameGrid;
	private Ship testShip;
	private double lastHealth=1000;//Some arbitrary large number
	private double lastShields=1000;
	private Player user=new Player(0);
	private float time = 0; //for use as timer
	private float scoreboardTimer=500;//Refresh scoreboard every half of a second
	private Point3D cameraLoc;
	private Point3D cameraTarget;
	private int gridXScale = 3;
	private int gridYScale = 4;
	private Camera3Pkeyboard charController;
	private IDisplaySystem display;
	private IRenderer renderer;
	private BlendState btransp;
	private ICamera camera;
	private IEventManager eventMgr;
	private Controller gamepadOne = null;
	private OBJLoader loader = new OBJLoader();
	private SkyBox skybox;
	private boolean gameOver=false;
	private boolean showScoreboard=false;
	private boolean gameJustBegan=true;
	private Collection<Line> laserCollection;
	private Collection<Rectangle> cloudCollection;
	private Collection<Turret> turretCollection;
	
	private ScriptEngineManager scriptFactory;
	private String scriptFileName;
	private File scriptFile;
	private ScriptEngine jsEngine;
	private long fileLastModifiedTime;
	
	//private GameServerTCP ourServer;
	private MyClient ourClient;
	private boolean isConnected = false;//Set, but never read... Is it needed for any reason?
	private Point3D position = new Point3D(0,0,0);
	private int trenchGenerator = 1;
	
	//Controllers
	LaserController lc1 = new LaserController();
	
	//Scoreboard
	private HUDString[] scoreboard = new HUDString[MAXPLAYERS+1];//MUST BE FIXED!!!!!! Methods called against this variable will not handle 4 players!-CM
	private HUDString[] finalScoreboard = new HUDString[MAXPLAYERS+1];
	private static DecimalFormat myFormatter = new DecimalFormat("000");
	
	//Lobby
	private int lobbySelector=0;
	private HUDString gameTitle;
	private HUDString startDirections, objectives, gameDirections;
	private TriMesh[] lobbyModels;
	private TriMesh andarLobbyModel;
	private TriMesh icarusLobbyModel;
	private String[] konamiCode = new String[] {"UP","UP","DOWN","DOWN","LEFT","RIGHT","LEFT","RIGHT","B","A"};
	private int konamiCounter=0;
	
	//Health and Shields
	private Rectangle health;
	private Rectangle shields;
	
	// Lobby switch in update
	private boolean lobby = true;
	private boolean transition = false;
	private Point3D beginCamLoc = new Point3D();
	private Point3D beginCamTar = new Point3D();
	private HUDString respawnDirections;
		
	//Sound variables
	IAudioManager audioMgr;
	AudioResource soundResource1;//soundResource1
	AudioResource soundResource2;
	AudioResource soundResource3;
	AudioResource soundResource4;
	AudioResource soundResource5;
	AudioResource soundResource6;
	AudioResource soundResource7;
	AudioResource soundResource8;
	AudioResource soundResource9;
	AudioResource soundResource10;
	AudioResource soundResource11;
	AudioResource musicResource1;
	AudioResource musicResource2;
	Sound laserSound;//laserSound
	Sound failedMove;
	Sound codyLaser;
	Sound alarm;
	Sound shieldHit;
	Sound shieldFail;
	Sound explosion;
	Sound healthHit;
	Sound andarDeclaration;
	Sound icarusDeclaration;
	Sound ding;
	Sound musicMasters;
	Sound musicDangerZone;
	Sound backgroundMusic;
	private boolean dangerZonePlaying=false;
	private boolean gameEnd = false;
	
	Group towerAnim1, towerAnim2;
	private boolean fullScreen;
	private String ipAddress;

	//expo variables
	private int port;
	
	public TrenchRun(String fs, String ip, int serverPort) {
		if(fs.equalsIgnoreCase("yes")){
			fullScreen = true;
		} else {
			fullScreen = false;
		}
		ipAddress = ip;
		port = serverPort;
	}
	protected void initGame(){
		beginClient();
		InitScripts();
		this.executeScript(jsEngine, scriptFileName);
		beginCamLoc = (Point3D) jsEngine.get("cameraLoc");
		beginCamTar = (Point3D) jsEngine.get("cameraTarget");
		eventMgr = EventManager.getInstance();
		renderer = display.getRenderer();
		initRenderStates();
		initGameObjects();
		clearShipCommands();
		initAudio();
		initTerrainBlocks();
		rootGroup = new Group("rootGroup");
		laserGroup = new Group("laserGroup");
		smokeGroup = new Group("smokeGroup");
		lobbyLineGroup = new Group("lobbyLineGroup");
		lobbyGroup = new Group("lobbyGroup");
		lobbyTowerGroup = new Group("lobbyTowerGroup");
		lobbyModels = new TriMesh[2];
		createTrench("1", "111110000000");
		createTrench("2", "111110000000");
		createTrench("3", "111110000000");
		createTrench("4", "111110000000");
		createTrench("5", "111110000000");
		createTrench("6", "111110000000");
		createTrench("7", "111110000000");
		createTrench("8", "111110000000");
		addGameWorldObject(rootGroup);
		//addGameWorldObject(laserGroup);
		
		moveTrench = new MoveTrenchController();
		moveTrench.addControlledNode(rootGroup);
		rootGroup.addController(moveTrench);
		moveSmoke = new SmokeController();
		moveSmoke.addControlledNode(smokeGroup);
		smokeGroup.addController(moveSmoke);
		addGameWorldObject(smokeGroup);
		
		// Create "ground plane"
		groundPlane = new Rectangle();
		groundPlane.rotate(90, new Vector3D(1,0,0));
		groundPlane.rotate(0, new Vector3D(0,0,1));
		groundPlane.scale(20, 20, 20);
		groundPlane.translate(0, -4.9f, 0);
		groundPlane.setCullMode(CULL_MODE.ALWAYS);
		groundPlane.updateLocalBound();
		addGameWorldObject(groundPlane);
		
		// Physics
		initPhysicsSystem();
		createSagePhysicsWorld();
		initLobbyHud();
		super.update(0.0f);
	}
	public void beginGame(){
		camera.removeFromHUD(startDirections);
		camera.removeFromHUD(gameTitle);
		camera.removeFromHUD(objectives);
		camera.removeFromHUD(gameDirections);		
		removeGameWorldObject(lobbyTowerGroup);
		gameGrid.removeShip(user.getPlayerNumber());
		removeGameWorldObject(user.getShip().getShipModel());
		switch(lobbySelector){
		case 0:
			initShip("ANDAR");
			andarDeclaration.setLocation(camera.getLocation());
			andarDeclaration.play(30, false);
			break;
		case 1:
			initShip("ICARUS");
			icarusDeclaration.setLocation(camera.getLocation());
			icarusDeclaration.play(30, false);
			break;
		default :
			initShip("ANDAR");
			System.out.println("ERROR: Unkown ship model variant (see beginGame method)");
				break;
		}
		
		transition = true;
		if(user.getPlayerNumber() == 1){
			gameGrid.initShip(0, 0, testShip);
		} else if(user.getPlayerNumber() == 2){
			gameGrid.initShip(3, 0, testShip);
		} else if(user.getPlayerNumber() == 3){
			gameGrid.initShip(3, 3, testShip);
		} else if(user.getPlayerNumber() == 4){
			gameGrid.initShip(0, 3, testShip);
		}
		sendDetailsForMessage();//makes a call to client
		setEarParameters();
		
	}
	
	private void sendDetailsForMessage() {
		ourClient.sendDetailsForMessage(getPlayerInfo());
	}
	private void initRenderStates() {
		btransp = (BlendState) renderer.createRenderState(RenderStateType.Blend);
		btransp.setBlendEnabled(true);
		btransp.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		btransp.setDestinationFunction(BlendState.DestinationFunction.DestinationAlpha);
		btransp.setTestEnabled(true);
		btransp.setTestFunction(BlendState.TestFunction.GreaterThan);
		btransp.setEnabled(true);
		//btransp.setBlendEquationAlpha(BlendState.BlendEquation.Subtract);
	}

	/**
	 * Initializes the physics system for the game.
	 */
	private void initPhysicsSystem() {
		String engine = "sage.physics.JBullet.JBulletPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		float[] gravity = {0, -100f, 0};
		physicsEngine.setGravity(gravity);
	}
	
	/**
	 * Creates corresponding sage physics objects
	 * for the game world.
	 */
	private void createSagePhysicsWorld() {
		// add the ship physics
		
		// add the ground plane physics
		float up[] = {0, 1, 0}; // {0,1,0} is flat
		groundPlaneP =
				physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(),
				groundPlane.getWorldTransform().getValues(), up, 0.0f);
		groundPlaneP.setBounciness(0.5f);
		groundPlane.setPhysicsObject(groundPlaneP);		
		// should also set damping, friction, etc.
	}
	
	private void initAudio() {
		audioMgr = AudioManagerFactory.createAudioManager(
				"sage.audio.joal.JOALAudioManager");
		if(!audioMgr.initialize())
		{
			return;
		}
		soundResource1 = audioMgr.createAudioResource(SOUNDRESOURCEONELOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		laserSound = new Sound(soundResource1, SoundType.SOUND_EFFECT, 10, false);
		laserSound.initialize(audioMgr);
		laserSound.setMaxDistance(50.0f);
		laserSound.setMinDistance(3.0f);
		laserSound.setRollOff(5.0f);

		soundResource2 = audioMgr.createAudioResource(SOUNDRESOURCETWOLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		failedMove = new Sound(soundResource2, SoundType.SOUND_EFFECT, 10, false);
		failedMove.initialize(audioMgr);
		failedMove.setMaxDistance(50.0f);
		failedMove.setMinDistance(3.0f);
		failedMove.setRollOff(5.0f);
		
		soundResource3 = audioMgr.createAudioResource(SOUNDRESOURCETHREELOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		codyLaser = new Sound(soundResource3, SoundType.SOUND_EFFECT, 10, false);
		codyLaser.initialize(audioMgr);
		codyLaser.setMaxDistance(50.0f);
		codyLaser.setMinDistance(3.0f);
		codyLaser.setRollOff(5.0f);
		
		soundResource4 = audioMgr.createAudioResource(SOUNDRESOURCEFOURLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		alarm = new Sound(soundResource4, SoundType.SOUND_EFFECT, 10, false);
		alarm.initialize(audioMgr);
		alarm.setMaxDistance(50.0f);
		alarm.setMinDistance(3.0f);
		alarm.setRollOff(5.0f);
		
		soundResource5 = audioMgr.createAudioResource(SOUNDRESOURCEFIVELOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		shieldHit = new Sound(soundResource5, SoundType.SOUND_EFFECT, 10, false);
		shieldHit.initialize(audioMgr);
		shieldHit.setMaxDistance(50.0f);
		shieldHit.setMinDistance(3.0f);
		shieldHit.setRollOff(5.0f);
		
		soundResource6 = audioMgr.createAudioResource(SOUNDRESOURCESIXLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		shieldFail = new Sound(soundResource6, SoundType.SOUND_EFFECT, 10, false);
		shieldFail.initialize(audioMgr);
		shieldFail.setMaxDistance(50.0f);
		shieldFail.setMinDistance(3.0f);
		shieldFail.setRollOff(5.0f);
		
		soundResource7 = audioMgr.createAudioResource(SOUNDRESOURCESEVENLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		explosion = new Sound(soundResource7, SoundType.SOUND_EFFECT, 10, false);
		explosion.initialize(audioMgr);
		explosion.setMaxDistance(50.0f);
		explosion.setMinDistance(3.0f);
		explosion.setRollOff(5.0f);
		
		soundResource8 = audioMgr.createAudioResource(SOUNDRESOURCEEIGHTLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		healthHit = new Sound(soundResource8, SoundType.SOUND_EFFECT, 10, false);
		healthHit.initialize(audioMgr);
		healthHit.setMaxDistance(50.0f);
		healthHit.setMinDistance(3.0f);
		healthHit.setRollOff(5.0f);
		
		soundResource9 = audioMgr.createAudioResource(SOUNDRESOURCENINELOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		andarDeclaration = new Sound(soundResource9, SoundType.SOUND_EFFECT, 10, false);
		andarDeclaration.initialize(audioMgr);
		andarDeclaration.setMaxDistance(50.0f);
		andarDeclaration.setMinDistance(3.0f);
		andarDeclaration.setRollOff(5.0f);
		
		soundResource10 = audioMgr.createAudioResource(SOUNDRESOURCETENLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		icarusDeclaration = new Sound(soundResource10, SoundType.SOUND_EFFECT, 10, false);
		icarusDeclaration.initialize(audioMgr);
		icarusDeclaration.setMaxDistance(50.0f);
		icarusDeclaration.setMinDistance(3.0f);
		icarusDeclaration.setRollOff(5.0f);
		
		soundResource11 = audioMgr.createAudioResource(SOUNDRESOURCEELEVENLOCATION,
				AudioResourceType.AUDIO_SAMPLE);
		//The int in the Sound constructor below does NOTHING!
		ding = new Sound(soundResource11, SoundType.SOUND_EFFECT, 10, false);
		ding.initialize(audioMgr);
		ding.setMaxDistance(50.0f);
		ding.setMinDistance(3.0f);
		ding.setRollOff(5.0f);
		
		musicResource1 = audioMgr.createAudioResource(MUSICRESOURCEONELOCATION, AudioResourceType.AUDIO_SAMPLE);
		musicMasters = new Sound(musicResource1, SoundType.SOUND_MUSIC, 8, true);
		musicMasters.initialize(audioMgr);
		musicMasters.setMaxDistance(50.0f);
		musicMasters.setMinDistance(3.0f);
		musicMasters.setRollOff(5.0f);
		
		
		musicResource2 = audioMgr.createAudioResource(MUSICRESOURCETWOLOCATION, AudioResourceType.AUDIO_SAMPLE);
		musicDangerZone = new Sound(musicResource2, SoundType.SOUND_MUSIC, 8, true);
		musicDangerZone.initialize(audioMgr);
		musicDangerZone.setMaxDistance(50.0f);
		musicDangerZone.setMinDistance(3.0f);
		musicDangerZone.setRollOff(5.0f);
		
		backgroundMusic=musicMasters;
	}
	public void setEarParameters()
	{
		Matrix3D avDir = (Matrix3D) (user.getShip().getShipModel().getWorldRotation().clone());
		float camAz = (float)charController.getAzimuth();
		avDir.rotateY(180.0f-camAz);
		Vector3D camDir = new Vector3D(0,0,1);
		camDir = camDir.mult(avDir);
		audioMgr.getEar().setLocation(charController.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0,1,0));
	}

	private void InitScripts(){
		scriptFactory = new ScriptEngineManager();
		scriptFileName = "./testScript.js";
		scriptFile = new File("./testScript.js");
		List<ScriptEngineFactory> scriptList = scriptFactory.getEngineFactories();
		System.out.println("Script Engine Factories Found: ");
		for( ScriptEngineFactory f : scriptList){
			System.out.println(" Name= " + f.getEngineName()
				+ " Language = " + f.getLanguageName()
				+ " extensions = " + f.getExtensions());
		}
		jsEngine = scriptFactory.getEngineByName("js");
	}
	private void executeScript(ScriptEngine engine, String scriptFileName){
		try{
			FileReader fr = new FileReader(scriptFileName);
			engine.eval(fr);
			fr.close();
		} catch (FileNotFoundException noFile){
			System.out.println(scriptFileName + " not found. " + noFile);
		} catch (IOException ioE){
			System.out.println("IO problem with " + scriptFileName + ioE);
		} catch (ScriptException scriptE){
			System.out.println("ScriptException in " + scriptFileName + scriptE);
		} catch (NullPointerException nullE){
			System.out.println("Null Pointer Exception in " + scriptFileName + nullE);
		}
	}
	
	private void initShip(String shipType) {
		switch(shipType){
		case "ANDAR":{
			TriMesh andar = loader.loadModel(SHIPMODELANDARLOCATION);
			andarTexture.setApplyMode(Texture.ApplyMode.Replace);
			andar.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			andar.setLocalRotation(rot);
			andar.setLocalScale(scale);
			testShip = new Ship(100, 50, 0, andar);
			testShip.setTypeString("ANDAR");
		}
			break;
		case "ICARUS":{
			TriMesh icarus = loader.loadModel(SHIPMODELICARUSLOCATION);
			icarusTexture.setApplyMode(Texture.ApplyMode.Replace);
			icarus.setTexture(icarusTexture);
			Matrix3D icarusRot = new Matrix3D();
			icarusRot.rotate(90.0,0.0,0.0);
			icarus.setLocalRotation(icarusRot);
			testShip = new Ship(100, 50, 0, icarus);
			testShip.setTypeString("ICARUS");
		}
			break;
		default :{
			TriMesh andar = loader.loadModel(SHIPMODELANDARLOCATION);
			andarTexture.setApplyMode(Texture.ApplyMode.Replace);
			andar.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			andar.setLocalRotation(rot);
			andar.setLocalScale(scale);
			testShip = new Ship(100, 50, 0, andar);
			testShip.setTypeString("ANDAR");
			System.out.println("ERROR: Unkown Ship variant (see initShip)");
		}
			break;
		}
		user.setShip(testShip);
		lastShields=user.getShip().getShields();//Set the shields value, otherwise update does it, and plays a sound
		lastHealth=user.getShip().getHealth();//Same here
		user.setUUID(ourClient.getClientID());
		addGameWorldObject(user.getShip().getShipModel());
	}
	
	private void initGameObjects(){
		this.executeScript(jsEngine, scriptFileName);
		cameraLoc = (Point3D) jsEngine.get("cameraLoc");
		cameraLoc.setY(cameraLoc.getY()+120);
		cameraLoc.setZ(cameraLoc.getZ()-20);
		cameraTarget = (Point3D) jsEngine.get("cameraTarget");
		cameraTarget.setY(cameraTarget.getY()+200);
		cameraTarget.setZ(cameraTarget.getZ()-20);
		camera = new JOGLCamera(renderer);
		camera.setPerspectiveFrustum(45, 1, 0.01, 8000);
		camera.setLocation(cameraLoc);
		
		laserCollection = new ArrayList<Line>();
		cloudCollection = new ArrayList<Rectangle>();
		turretCollection = new ArrayList<Turret>();
		
		skybox = new SkyBox("skybox",1500.0f, 2000.0f, 1500.0f);
		Texture skyboxTextureDown = TextureManager.loadTexture2D("./textures/star_skybox_down.jpg");
		Texture skyboxTextureUp = TextureManager.loadTexture2D("./textures/star_skybox_up.jpg");
		Texture skyboxTextureNorth = TextureManager.loadTexture2D("./textures/star_skybox_up.jpg");
		Texture skyboxTextureSouth = TextureManager.loadTexture2D("./textures/star_skybox_south.jpg");
		Texture skyboxTextureWest = TextureManager.loadTexture2D("./textures/star_skybox_west.jpg");
		Texture skyboxTextureEast = TextureManager.loadTexture2D("./textures/star_skybox_east.jpg");
		skybox.setTexture(SkyBox.Face.Down, skyboxTextureDown);
		skybox.setTexture(SkyBox.Face.Up, skyboxTextureUp);
		skybox.setTexture(SkyBox.Face.North, skyboxTextureNorth);
		skybox.setTexture(SkyBox.Face.South, skyboxTextureSouth);
		skybox.setTexture(SkyBox.Face.West, skyboxTextureWest);
		skybox.setTexture(SkyBox.Face.East, skyboxTextureEast);
		skybox.updateRenderStates();
		addGameWorldObject(skybox);
		
		initShip("ANDAR");
		
		gameGrid = new Grid(this, GRIDWIDTH, GRIDHEIGHT);
		gameGrid.setXScale(gridXScale);
		gameGrid.setYScale(gridYScale);
		
		bridgeObstacle = loader.loadModel(BRIDGEMODELLOCATION);
		bridgeObstacleTexture.setApplyMode(Texture.ApplyMode.Replace);
		bridgeObstacle.setTexture(bridgeObstacleTexture);
		
		towerObstacle = loader.loadModel(TOWERMODELLOCATION);
		towerObstacleTexture.setApplyMode(Texture.ApplyMode.Replace);
		towerObstacle.setTexture(towerObstacleTexture);
		
		turretObstacle = loader.loadModel(TURRETMODELLOCATION);
		turretObstacleTexture.setApplyMode(Texture.ApplyMode.Replace);
		turretObstacle.setTexture(turretObstacleTexture);
		
		for( int x = 0; x < GRIDWIDTH+1; x++){
			Point3D xBottom = new Point3D(x*gridXScale, 0, 0);
			Point3D xTop = new Point3D(x*gridXScale, 4*gridYScale, 0);
			Line xLine = new Line (xBottom, xTop, GRIDCOLOR, 2);
			addGameWorldObject(xLine);
		}
		for( int y = 0; y < GRIDHEIGHT+1; y++){
			Point3D yLeft = new Point3D(0, y*gridYScale, 0);
			Point3D yRight = new Point3D(4*gridXScale, y*gridYScale, 0);
			Line yLine = new Line (yLeft, yRight, GRIDCOLOR, 2);
			addGameWorldObject(yLine);
		}		
		
		respawnDirections = new HUDString("To respawn, press SPACEBAR or START");
		respawnDirections.setCullMode(CULL_MODE.NEVER);
		respawnDirections.setRenderMode(RENDER_MODE.ORTHO);
		respawnDirections.setColor(Color.red);
		respawnDirections.setLocation(0.41, 0.9);
	}
	
	private void initHUD() {
		
		shields = new Rectangle(0.5f, 0.1f);
		shields.setCullMode(SceneNode.CULL_MODE.NEVER);
		shields.setRenderMode(RENDER_MODE.ORTHO);
		Matrix3D shieldTran = new Matrix3D();
		shieldTran.translate(0.7, -0.7, -0.1);
		Matrix3D shieldScale = new Matrix3D();
		shieldScale.scale(0.52, 0.75, 1.0);
		shields.setWorldTranslation(shieldTran);
		shields.setWorldScale(shieldScale);
		shields.setColor(Color.cyan);
		camera.addToHUD(shields);
		
		health = new Rectangle(0.5f,0.1f);
		health.setCullMode(SceneNode.CULL_MODE.NEVER);
		health.setRenderMode(RENDER_MODE.ORTHO);
		Matrix3D healthTran = new Matrix3D();
		healthTran.translate(0.7, -0.7, 0.0);
		Matrix3D healthScale = new Matrix3D();
		healthScale.scale(0.5, 0.5, 1.0);
		health.setWorldScale(healthScale);
		health.setWorldTranslation(healthTran);
		health.setColor(Color.red);
		camera.addToHUD(health);
		
		
	}

	public void createTrench(String sectionNumber, String seed){
		currentTrenchNumber = Integer.parseInt(sectionNumber);
		currentTrenchNumber++;
		TrenchSection initTrench = new TrenchSection(gameGrid, sectionNumber, seed, terrainList, terrainTexture, bridgeObstacle, towerObstacle, turretObstacle);
		Group trenchSection = new Group("section" + sectionNumber);
		rootGroup.addChild(trenchSection);
		trenchSection.addChild(initTrench.getLeftTop());
		trenchSection.addChild(initTrench.getLeftSide());
		trenchSection.addChild(initTrench.getRightTop());
		trenchSection.addChild(initTrench.getRightSide());
		trenchSection.addChild(initTrench.getGround());	
		if(initTrench.getObstacle() != null){
			trenchSection.addChild(initTrench.getObstacle());
		}
		if(initTrench.getTurretLeftTop() != null){
			Turret turret = new Turret(initTrench.getTurretLeftTop(), Integer.parseInt(sectionNumber), seed.charAt(8));
			turretCollection.add(turret);
			trenchSection.addChild(initTrench.getTurretLeftTop());
		}
		if(initTrench.getTurretLeftBot() != null){
			Turret turret = new Turret(initTrench.getTurretLeftBot(), Integer.parseInt(sectionNumber), seed.charAt(9));
			turretCollection.add(turret);
			trenchSection.addChild(initTrench.getTurretLeftBot());
		}
		if(initTrench.getTurretRightBot() != null){
			Turret turret = new Turret(initTrench.getTurretRightBot(), Integer.parseInt(sectionNumber), seed.charAt(10));
			turretCollection.add(turret);
			trenchSection.addChild(initTrench.getTurretRightBot());
		}
		if(initTrench.getTurretRightTop() != null){
			Turret turret = new Turret(initTrench.getTurretRightTop(), Integer.parseInt(sectionNumber), seed.charAt(11));
			turretCollection.add(turret);
			trenchSection.addChild(initTrench.getTurretRightTop());
		}
		Group tempGroup = null;
		if(deleteTrenchNumber == currentTrenchNumber - 9){
			for (Iterator<SceneNode> gameIterator = rootGroup.getChildren(); gameIterator.hasNext();){
				Object o = gameIterator.next();
				if( o == rootGroup.getChild("section" + deleteTrenchNumber)){
					tempGroup = (Group) o;
				}
			}
			rootGroup.removeChild(tempGroup);
			removeGameWorldObject(tempGroup);
			deleteTrenchNumber++;
		}
		
	}
	
	private void updateTurretCollection(){
		Iterator<Turret> i = turretCollection.iterator();
		ArrayList<Turret> toRemove = new ArrayList<Turret>();
		while(i.hasNext()){
			Object temp = i.next();
			if(temp instanceof Turret){
				if(((Turret)temp).getHealth()<=0 || ((Turret)temp).getTrenchNum() == deleteTrenchNumber){
					toRemove.add(((Turret)temp));
				}
			}
		}
		for(Turret t: toRemove){
			turretCollection.remove(t);
		}
	}
	
	private void initTerrainBlocks(){
		terrainList[0] = createTerBlock("./textures/TopHM1.jpg");
		terrainTexture[0] = createTerTex("./textures/TopTexture1.jpg");
		
		terrainList[1] = createTerBlock("./textures/TopHM2.jpg");
		terrainTexture[1] = createTerTex("./textures/TopTexture2.jpg");
		
		terrainList[2] = createTerBlock("./textures/SideHM1.jpg");
		terrainTexture[2] = createTerTex("./textures/ShadowedTexture1.jpg");
		
		terrainList[3] = createTerBlock("./textures/SideHM2.jpg");
		terrainTexture[3] = createTerTex("./textures/ShadowedTexture2.jpg");
		
		terrainList[4] = createTerBlock("./textures/SideHM1.jpg");
		terrainTexture[4] = createTerTex("./textures/RightTexture1.jpg");
		
		terrainList[5] = createTerBlock("./textures/SideHM2.jpg");
		terrainTexture[5] = createTerTex("./textures/RightTexture2.jpg");
	}
	
	private TextureState createTerTex(String textureImage){
		TextureState floorState;
		Texture floorTexture = TextureManager.loadTexture2D(textureImage);
		floorTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		//floorTexture.setMinificationFilter(MinificationFilter.BilinearNearestMipMap);
		floorState = (TextureState)display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		floorState.setTexture(floorTexture, 0);
		floorState.setEnabled(true);
		
		return floorState;
	}

	private TerrainBlock createTerBlock(String heightMap){
		ImageBasedHeightMap myHeightMap = new ImageBasedHeightMap(heightMap);
		float heightScale = .022f;
		Vector3D terrainScale = new Vector3D(.2, heightScale, 1);
		int terrainSize = myHeightMap.getSize();
		 
		float cornerHeight = myHeightMap.getTrueHeightAtPoint(0, 0) * heightScale;
		Point3D terrainOrigin = new Point3D(0, -cornerHeight, 0);
		
		String name = "Terrain: " + heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale, myHeightMap.getHeightData(), terrainOrigin);
		return tb;
	}

	/**
	 * Constructs a new input manager that does not include ship commands.
	 * Sets the BaseGame input manager to the input manager.
	 */
	private void clearShipCommands() {
		IInputManager im = new InputManager();

		// General Actions
		ToggleScoreboardAction toggleScore = new ToggleScoreboardAction(this);
		QuitAction quit = new QuitAction(this);
		BeginGameAction begin = new BeginGameAction(this);
		LobbySelectRightAction lobbySelectRight = new LobbySelectRightAction(this);
		LobbySelectLeftAction lobbySelectLeft = new LobbySelectLeftAction(this);
		LobbySelectUpAction lobbySelectUp = new LobbySelectUpAction(this);
		LobbySelectDownAction lobbySelectDown = new LobbySelectDownAction(this);
		DPadLobbySelectAction dPadLobby = new DPadLobbySelectAction(this);
		
		
		// Keyboard
		Controller kbName = im.getKeyboardController(1);
		
		// Camera Controller
		charController = new Camera3Pkeyboard(camera, cameraTarget, im, kbName, .05f, .05f);

		// Exclusive Keyboard Actions

		// Keyboard Commands
		im.associateAction(
				kbName, net.java.games.input.Component.Identifier.Key.ESCAPE,
				quit, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		if(lobby){
			im.associateAction(
				kbName, net.java.games.input.Component.Identifier.Key.SPACE,
				begin, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
				kbName, net.java.games.input.Component.Identifier.Key.D,
				lobbySelectRight, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.A,
					lobbySelectLeft, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.W,
					lobbySelectUp, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.S,
					lobbySelectDown, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.B,
					lobbySelectRight, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.RIGHT,
					lobbySelectRight, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.LEFT,
					lobbySelectLeft, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.UP,
					lobbySelectUp, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					kbName, net.java.games.input.Component.Identifier.Key.DOWN,
					lobbySelectDown, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
		} else if( !gameEnd ){
			im.associateAction(
				kbName, net.java.games.input.Component.Identifier.Key.TAB,
				toggleScore, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}

		// Gamepad
		String gpName = im.getFirstGamepadName();

		if (gpName != null) {

			// Exclusive Gamepad Actions

			// Gamepad Commands
			if(lobby){
				im.associateAction(
					gpName, net.java.games.input.Component.Identifier.Button._7,
					begin, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				im.associateAction(
						gpName, net.java.games.input.Component.Identifier.Button._0,
						lobbySelectLeft, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				im.associateAction(
						gpName, net.java.games.input.Component.Identifier.Button._1,
						lobbySelectRight, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				im.associateAction(
						gpName, net.java.games.input.Component.Identifier.Axis.POV,
						dPadLobby, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			} else if ( !gameEnd ){
				im.associateAction(
					gpName, net.java.games.input.Component.Identifier.Button._6,
					toggleScore, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			}
		}

		// Set new input manager
		setInputManager(im);
	}

	/**
	 * Adds commands to the new input manager with the commands needed
	 * to maneuver the player's ship.
	 */
	private void resetShipCommands() {
		IInputManager im = getInputManager();

		// General Actions

		// Keyboard
		Controller kbName = im.getKeyboardController(1);

		// Exclusive Keyboard Actions
		MoveUpAction mvForward = new MoveUpAction(this, testShip, gameGrid, GRIDWIDTH, GRIDHEIGHT);
		MoveDownAction mvBackward = new MoveDownAction(this, testShip, gameGrid, GRIDWIDTH, GRIDHEIGHT);
		MoveLeftAction mvLeft = new MoveLeftAction(this, testShip, gameGrid, GRIDWIDTH, GRIDHEIGHT);
		MoveRightAction mvRight = new MoveRightAction(this, testShip, gameGrid, GRIDWIDTH, GRIDHEIGHT);
		DPadMoveAction stickVert = new DPadMoveAction(this, testShip, gameGrid, GRIDWIDTH, GRIDHEIGHT);
		ControllerLaserAction controllerFireMahLazors = new ControllerLaserAction(user.getShip().getShipModel(), this);
		FireLaserAction fireMahLazors = new FireLaserAction(user.getShip().getShipModel(), this);
		//TODO Demonstration for physics
		KillShipAction killShip = new KillShipAction(user.getShip());

		// Keyboard Commands
		im.associateAction(kbName,
				net.java.games.input.Component.Identifier.Key.W,
				mvForward,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName,
				net.java.games.input.Component.Identifier.Key.S,
				mvBackward,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName,
				net.java.games.input.Component.Identifier.Key.A,
				mvLeft,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName,
				net.java.games.input.Component.Identifier.Key.D,
				mvRight,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.L,
				fireMahLazors, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		//TODO Demonstration for physics
		im.associateAction(
				kbName, net.java.games.input.Component.Identifier.Key.K,
				killShip, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		

		// Gamepad
		String gpName = im.getFirstGamepadName();

		if (gpName != null) {
			// Gamepad Commands
			im.associateAction(
					gpName, net.java.games.input.Component.Identifier.Axis.POV,
					stickVert, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			im.associateAction(
					gpName, net.java.games.input.Component.Identifier.Axis.Z,
					controllerFireMahLazors, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}

		// Set updated input manager
		setInputManager(im);
	}
	private void enableRespawnCommands(){
		IInputManager im = getInputManager();

		// General Actions
		RespawnAction respawn = new RespawnAction(this);
		
		// Keyboard
		Controller kbName = im.getKeyboardController(1);
		
		// Camera Controller
		charController = new Camera3Pkeyboard(camera, cameraTarget, im, kbName, .05f, .05f);

		// Exclusive Keyboard Actions

		// Keyboard Commands
		im.associateAction(
			kbName, net.java.games.input.Component.Identifier.Key.SPACE,
			respawn, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	

		// Gamepad
		String gpName = im.getFirstGamepadName();

		if (gpName != null) {
			// Exclusive Gamepad Actions

			// Gamepad Commands
			im.associateAction(
					gpName, net.java.games.input.Component.Identifier.Button._7,
					respawn, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}

		// Set new input manager
		setInputManager(im);
	}
	
	public void update(float elapsedTimeMS){
		if(lobby){
			if(transition){
				cameraLoc.setY(cameraLoc.getY()-(0.018 * elapsedTimeMS));
				cameraLoc.setZ(cameraLoc.getZ()+(0.003 * elapsedTimeMS));
				camera.setLocation(cameraLoc);
				cameraTarget.setY(cameraTarget.getY()-(0.03 * elapsedTimeMS));
				cameraTarget.setZ(cameraTarget.getZ()+(0.003 * elapsedTimeMS));
				charController.newTarget(cameraTarget);
				charController.update(elapsedTimeMS);
				if(cameraLoc.getY() <= beginCamLoc.getY()){
					lobby = false;
					clearShipCommands();
					resetShipCommands();
					initHUD();
					//Remove lobby screen items (HUDStrings already removed.
					removeGameWorldObject(lobbyGroup);
				}
				if(!andarDeclaration.getIsPlaying() && !icarusDeclaration.getIsPlaying() && !alarm.getIsPlaying()){
					alarm.play(10, false);
				}
			}//end of transition
			if (ourClient != null) {
				ourClient.processPackets();
			}
			Iterator<SceneNode> itr = towerAnim1.getChildren();
			while( itr.hasNext()){
				Model3DTriMesh towerMesh = ((Model3DTriMesh) itr.next());
				towerMesh.updateAnimation(elapsedTimeMS);
			}
			itr = towerAnim2.getChildren();
			while( itr.hasNext()){
				Model3DTriMesh towerMesh = ((Model3DTriMesh) itr.next());
				towerMesh.updateAnimation(elapsedTimeMS);
			}
		} else {
			time=time+elapsedTimeMS;
			scoreboardTimer=scoreboardTimer-elapsedTimeMS;
			if(scoreboardTimer<0 && !gameEnd){
				scoreboardTimer=500;
				updateScoreboard();
				ourClient.sendScoreMessage(Integer.toString(user.getScore()));
			}
			if(gameJustBegan){
				gameJustBegan=false;
				alarm.stop();
				backgroundMusic.play(10, true);
			}
			
			if(ourClient!=null){ ourClient.processPackets(); }
			if( !gameEnd ){
				long modTime = scriptFile.lastModified();
				if(modTime > fileLastModifiedTime){
					fileLastModifiedTime = modTime;
					this.executeScript(jsEngine, scriptFileName);
					cameraLoc = (Point3D) jsEngine.get("cameraLoc");
					cameraTarget = (Point3D) jsEngine.get("cameraTarget");
					camera.setLocation(cameraLoc);
					moveTrench.setTrenchSpeed((double) jsEngine.get("trenchSpeed"));
					moveSmoke.setSmokeSpeed((double) jsEngine.get("trenchSpeed"));
					charController.newTarget(cameraTarget);
				}
			} else {
				moveTrench.setTrenchSpeed(0.0f);
			}
			Point3D camLoc = camera.getLocation();
			Matrix3D camTrans = new Matrix3D();
			camTrans.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
			skybox.setLocalTranslation(camTrans);		
			
			if( !gameEnd ){	
				if( moveTrench.getZ() < (-198.6*deleteTrenchNumber)-100){
					if( user.getPlayerNumber() == trenchGenerator){
						ourClient.generateTrenchSeed(currentTrenchNumber);
					}
				}
			}
			charController.update(elapsedTimeMS);
			rootGroup.updateGeometricState(elapsedTimeMS, true);
			
			
			//Move the laser sound to the player's location and set the "Ear Parameters".
			laserSound.setLocation(position);
			shieldHit.setLocation(position);
			shieldFail.setLocation(position);
			healthHit.setLocation(position);
			setEarParameters();
			
			gameGrid.updateDangerZones(moveTrench.getZ());
			
			/*  Check if the player's ship is destroyed (has 0 health)
			 *	If it has then the ship should be removed and
			 *	the input manager should be cleared
			 */
			if(user.getShip().isDead()) {
				if(!user.getShip().hasExploded()) {
					killShip(user.getShip());
					camera.addToHUD(respawnDirections);
					sendDeathMessage();
					clearShipCommands();
					enableRespawnCommands();
				}
			}
			
			/* 
			 * Updates and runs physics
			 */
			Matrix3D mat;
			Vector3D translateVec;
			physicsEngine.update(elapsedTimeMS);
			SceneNode tempNode = null;
			ArrayList<Point3D> smokePoints = new ArrayList<Point3D>();
			for (SceneNode s : getGameWorld()) {
				if (s.getPhysicsObject() != null) {
					mat = new Matrix3D(s.getPhysicsObject().getTransform());
					translateVec = mat.getCol(3);
					s.getLocalTranslation().setCol(3,translateVec);
					// should also get and apply rotation
					float[] velocity = new float[3];
					velocity = s.getPhysicsObject().getLinearVelocity();
					if( velocity[2] <= 22){
						velocity[2] = -130;
						s.getPhysicsObject().setLinearVelocity(velocity);
					}
					if(s.getLocalTranslation().getCol(3).getZ() <= -50){
						tempNode = s;
					}

					if (s instanceof TriMesh) {
						// Save a point for each object with a physics object (dead ships)
						Point3D spot = new Point3D(s.getLocalTranslation().getCol(3).getX(),
								s.getLocalTranslation().getCol(3).getY(),
								s.getLocalTranslation().getCol(3).getZ());
						smokePoints.add(spot);
					}
				} 
			}
			if(tempNode != null){
				removeGameWorldObject(tempNode);
			}
			// Generate smoke based on points given
			for (Point3D spot : smokePoints) {
				createSmoke(spot);
			}
			smokePoints.clear();
			
			/*
			 * Increase the score for the player
			 */
			if (time > 1000) {
				if (!user.getShip().isDead()) {
					user.addToScore(1);
				}
				time = time - 1000;
			}
			

			user.getShip().updateShields(elapsedTimeMS);
			updateHealthShieldsHUD();
			updateLasersLife();
			updateSmokeLife();
			updateTurretCollection();
			turretsFire();
		}
		super.update(elapsedTimeMS);
	}
	
	private void initLobbyHud() {
		gameTitle = new HUDString("Trench Run");
		gameTitle.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		gameTitle.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		gameTitle.setColor(Color.red);
		gameTitle.setLocation(0.47, 0.7);
		camera.addToHUD(gameTitle);
		
		startDirections = new HUDString("When all players are ready, have one player press the SPACEBAR / START.");
		startDirections.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		startDirections.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		startDirections.setColor(Color.red);
		startDirections.setLocation(0.34, 0.30);
		camera.addToHUD(startDirections);
		
		objectives = new HUDString("When the game begins, keep clear of the Danger Zones and shoot the Turrets!");
		objectives.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		objectives.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		objectives.setColor(Color.red);
		objectives.setLocation(0.33, 0.27);
		camera.addToHUD(objectives);
		
		gameDirections = new HUDString("Use WASD / D-PAD to move in the trench and L / RIGHT TRIGGER to fire lasers!");		
		gameDirections.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);		
		gameDirections.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);		
		gameDirections.setColor(Color.red);		
		gameDirections.setLocation(0.32, 0.24);		
		camera.addToHUD(gameDirections);
		
		//Load in available Models and set their textures
		andarLobbyModel=loader.loadModel(SHIPMODELANDARLOCATION);
		andarTexture.setApplyMode(Texture.ApplyMode.Replace);
		andarLobbyModel.setTexture(andarTexture);
		icarusLobbyModel=loader.loadModel(SHIPMODELICARUSLOCATION);
		icarusTexture.setApplyMode(ApplyMode.Replace);
		icarusLobbyModel.setTexture(icarusTexture);
		
		//Add available models to lobbyModel array
		//(Used to translate the selector window)
		lobbyModels[0]=andarLobbyModel;
		lobbyModels[1]=icarusLobbyModel;
		
		//Transformations for Andar Model
		Matrix3D andarTrans = new Matrix3D();
		Matrix3D andarRotOne = new Matrix3D();
		Matrix3D andarScale = new Matrix3D();
		andarScale.scale(0.5, 0.5, 0.3);
		andarRotOne.rotate(0.0,-90.0,20.0);
		andarTrans.translate(-3, 0, 0);
		andarLobbyModel.setLocalTranslation(andarTrans);
		andarLobbyModel.setLocalRotation(andarRotOne);
		andarLobbyModel.setLocalScale(andarScale);
		
		//Transformations for Icarus Model
		Matrix3D icarusTrans = new Matrix3D();
		Matrix3D icarusRot = new Matrix3D();
		icarusRot.rotate(-110.0, 0.0, 0.0);
		icarusTrans.translate(3, 0, 0);
		icarusLobbyModel.setLocalTranslation(icarusTrans);
		icarusLobbyModel.setLocalRotation(icarusRot);
		
		//Create the selector "Window"
		Point3D bottomLeft = new Point3D(0,0,0);
		Point3D bottomRight = new Point3D(4,0,0);
		Point3D topRight = new Point3D(4,4,0);
		Point3D topLeft = new Point3D(0,4,0);
		Line selectorBoxLine1 = new Line(bottomLeft, bottomRight, Color.red, 4);
		Line selectorBoxLine2 = new Line(bottomRight, topRight, Color.red, 4);
		Line selectorBoxLine3 = new Line(topRight, topLeft, Color.red, 4);
		Line selectorBoxLine4 = new Line(topLeft, bottomLeft, Color.red, 4);
		
		//add the lines to a group to allow for uniform modification
		lobbyLineGroup.addChild(selectorBoxLine1);
		lobbyLineGroup.addChild(selectorBoxLine2);
		lobbyLineGroup.addChild(selectorBoxLine3);
		lobbyLineGroup.addChild(selectorBoxLine4);
		
		//Transforms for the selector window
		Matrix3D boxTrans = new Matrix3D();
		boxTrans.translate(lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getX()-2, lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getY()-3, 0);
		Matrix3D boxRot = new Matrix3D();
		boxRot.rotate(charController.getTilt(), new Vector3D(1.0,0.0,0.0));
		lobbyLineGroup.setLocalTranslation(boxTrans);
		lobbyLineGroup.setLocalRotation(boxRot);
		
		OgreXMLParser ogreLoader = new OgreXMLParser();
		towerAnim1 = null;
		towerAnim2 = null;
		try{
			towerAnim1 = ogreLoader.loadModel(
					"./animatedObjects/Tower.mesh.xml",
					"./animatedObjects/Material.001.material",
					"./animatedObjects/Tower.skeleton.xml");
		} catch (Exception e){
			e.printStackTrace();
		}
		try{
			towerAnim2 = ogreLoader.loadModel(
					"./animatedObjects/Tower.mesh.xml",
					"./animatedObjects/Material.001.material",
					"./animatedObjects/Tower.skeleton.xml");
		} catch (Exception e){
			e.printStackTrace();
		}
		Matrix3D tower1Trans = new Matrix3D();
		Matrix3D tower1Rot = new Matrix3D();
		Matrix3D tower1Scale = new Matrix3D();
		tower1Scale.scale(0.65, 1.35, 0.65);
		tower1Rot.rotate(0.0, 90.0, 20.0);
		tower1Trans.translate(-6.4, -12, 0);
		towerAnim1.setLocalTranslation(tower1Trans);
		towerAnim1.setLocalRotation(tower1Rot);
		towerAnim1.setLocalScale(tower1Scale);
		
		Matrix3D tower2Trans = new Matrix3D();
		Matrix3D tower2Rot = new Matrix3D();
		Matrix3D tower2Scale = new Matrix3D();
		tower2Scale.scale(0.65, 1.35, 0.65);
		tower2Rot.rotate(0.0, 90.0, 20.0);
		tower2Trans.translate(7, -12, 0);
		towerAnim2.setLocalTranslation(tower2Trans);
		towerAnim2.setLocalRotation(tower2Rot);
		towerAnim2.setLocalScale(tower2Scale);
		
		//Make a group of all items in the lobby screen to allow for uniform modification
		lobbyGroup.addChild(lobbyLineGroup);
		lobbyGroup.addChild(andarLobbyModel);
		lobbyGroup.addChild(icarusLobbyModel);
		lobbyTowerGroup.addChild(towerAnim1);
		lobbyTowerGroup.addChild(towerAnim2);
		addGameWorldObject(lobbyGroup);
		addGameWorldObject(lobbyTowerGroup);
		
		//Translate and rotate all items in the lobby screen
		Matrix3D groupTranslate = new Matrix3D();
		Matrix3D groupRot = new Matrix3D();
		groupRot.rotate(45.0, camera.getRightAxis());
		groupTranslate.translate(6, 152.5, -44);
		lobbyGroup.setLocalTranslation(groupTranslate);
		lobbyGroup.setLocalRotation(groupRot);
		lobbyTowerGroup.setLocalTranslation(groupTranslate);
		lobbyTowerGroup.setLocalRotation(groupRot);
		
		Iterator<SceneNode> itr = towerAnim1.getChildren();
		while( itr.hasNext()){
			Model3DTriMesh towerMesh = ((Model3DTriMesh) itr.next());
			towerMesh.startAnimation("DishRotate");
		}
		itr = towerAnim2.getChildren();
		while( itr.hasNext()){
			Model3DTriMesh towerMesh = ((Model3DTriMesh) itr.next());
			towerMesh.startAnimation("AntennaRotate");
		}
	}
	private void updateSmokeLife() {
		ArrayList<Cloud> toRemove = new ArrayList<Cloud>();
		for (SceneNode c : smokeGroup) {
			((Cloud) c).reduceLifeSpan(5.0);
			if (((Cloud) c).getLifeSpan() <= 0) {
				toRemove.add((Cloud) c);
			}
		}
		
		for (Cloud c : toRemove) {
			smokeGroup.removeChild(c);
		}
	}
	

	private void createSmoke(Point3D p) {
		Rectangle cloud = new Cloud(p);
		cloud.setRenderState(btransp);
		cloud.updateRenderStates();
		cloud.setRenderMode(RENDER_MODE.TRANSPARENT);
		
		Matrix3D moveCloud = new Matrix3D();
		moveCloud.translate(p.getX(), p.getY(), -moveSmoke.getZ()+p.getZ());
		cloud.setLocalTranslation(moveCloud);
		
		smokeGroup.addChild(cloud);
		cloudCollection.add(cloud);
	}

	private void updateLasersLife() {
		Iterator<Line> i = laserCollection.iterator();
		Laser toRemove = null;
		while(i.hasNext()){
			Object temp = i.next();
			if(temp instanceof Laser){
				((Laser)temp).reduceLifeSpan(10.0);
				if(((Laser)temp).getLifeSpan()<0){
					toRemove=((Laser)temp);
				}
			}
		}
		if(toRemove!=null){
			removeGameWorldObject(toRemove);
			laserCollection.remove(toRemove);//casted for safety
			laserGroup.removeChild(toRemove);
		}
	}

	private void updateHealthShieldsHUD() {
		if(!user.getShip().isDead()){
			Ship userShip = user.getShip();
			if(userShip.getHealth()<lastHealth){
				healthHit.play(30, false);
				Double percentHealth=0.0;
				percentHealth = (double)userShip.getHealth()/(double)userShip.getMaxHealth();
				camera.removeFromHUD(health);
				health=new Rectangle((float)(percentHealth*0.5),0.1f);
				health.setCullMode(SceneNode.CULL_MODE.NEVER);
				health.setRenderMode(RENDER_MODE.ORTHO);
				Matrix3D healthTran = new Matrix3D();
				healthTran.translate(0.7, -0.7, 0.0);
				Matrix3D healthScale = new Matrix3D();
				healthScale.scale(0.5, 0.5, 1.0);
				health.setWorldScale(healthScale);
				health.setWorldTranslation(healthTran);
				health.setColor(Color.red);
				camera.addToHUD(health);
				lastHealth=userShip.getHealth();
			}
			if(userShip.getShields()!=lastShields){
				if(userShip.getShields()<lastShields){
					shieldHit.play(30, false);
					if(lastShields<=0){
						shieldHit.stop();
						shieldFail.play(30, false);
					}
				}
				Double percentShields = 0.0;
				percentShields = (double)userShip.getShields()/(double)userShip.getMaxShields();
				camera.removeFromHUD(shields);
				shields = new Rectangle((float)(percentShields*0.5), 0.1f);
				shields.setCullMode(SceneNode.CULL_MODE.NEVER);
				shields.setRenderMode(RENDER_MODE.ORTHO);
				Matrix3D shieldTran = new Matrix3D();
				shieldTran.translate(0.7, -0.7, -0.1);
				Matrix3D shieldScale = new Matrix3D();
				shieldScale.scale(0.52, 0.75, 1.0);
				shields.setWorldTranslation(shieldTran);
				shields.setWorldScale(shieldScale);
				shields.setColor(Color.cyan);
				camera.addToHUD(shields);
				lastShields=userShip.getShields();
			}
		}else{
			camera.removeFromHUD(health);
			camera.removeFromHUD(shields);
		}
	}

	protected void initSystem() {
		createDisplay();
		setDisplaySystem(display);
		
		IInputManager im = new InputManager();
		setInputManager(im);
		
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}

	protected void createDisplay() {
		java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		MyDisplaySystem myDisplay = new MyDisplaySystem(width, height, 32, 60, fullScreen, "sage.renderer.jogl.JOGLRenderer");
		System.out.println("Waiting for display creation\n");
		int count = 0;
		
		while (!myDisplay.isCreated()){
			try{
				Thread.sleep(10);
			} catch (InterruptedException e){
				throw new RuntimeException("Display creation interrupted");
			}
			count++;
			System.out.print("+");
			if (count % 80 == 0){
				System.out.println();
			}
			if (count > 500){
				throw new RuntimeException("Unable to create display");
			}
		}
		Image reticleImage =
				new ImageIcon("./textures/reticleVOne.png").getImage();
		Cursor aimingReticle =
				Toolkit.getDefaultToolkit().
				createCustomCursor(reticleImage, new java.awt.Point(0,0),"Reticle");
		this.getRenderer().getCanvas().setCursor(aimingReticle);
		display = myDisplay;
	}

	protected void shutdown() {
		display.close();
		ourClient.sendByeMessage();
		this.setGameOver(true);
		super.shutdown();
		
	}

	protected void render() {
		renderer.setCamera(camera);
		renderer.clearRenderQueue();
		renderer.addToRenderQueue(skybox);
		renderer.processRenderQueue();
		
		super.render();
	}
	
	public void beginClient() {
		try {
			ourClient = new MyClient(InetAddress.getByName(ipAddress), port, ProtocolType.TCP, this);
			ourClient.sendJoinMessage();
		}
		catch (UnknownHostException e) 
		{ 
			e.printStackTrace(); 
		}
		catch (IOException e) 
		{ 
			e.printStackTrace(); 
		}
	}

	public void setIsConnected(boolean b, int num) {
		isConnected=b;
		user.setPlayerNumber(num);
		playersInGame[num]=user;
		generateNewScoreboardString(num);
		
		if(user.getPlayerNumber() == 1){
			gameGrid.initShip(0, 0, testShip);
		} else if(user.getPlayerNumber() == 2){
			gameGrid.initShip(3, 0, testShip);
		} else if(user.getPlayerNumber() == 3){
			gameGrid.initShip(3, 3, testShip);
		} else if(user.getPlayerNumber() == 4){
			gameGrid.initShip(0, 3, testShip);
		}
		
		
	}

	public Point3D getPlayerPosition(){
		return user.getShip().getLocation();
	}

	public Ship getPlayerShip() {
		return user.getShip();
	}

	public void createGhostAvatar(UUID ghostID, int x, int y, String shipModel, int ghostPlayerNumber){
		TriMesh modelType;
		switch(shipModel){
		case "ICARUS":{
			modelType=loader.loadModel(SHIPMODELICARUSLOCATION);
			modelType.setTexture(icarusTexture);
			Matrix3D rot = new Matrix3D();
			rot.rotate(90.0, 0.0, 0.0);
			modelType.setLocalRotation(rot);
		}
		break;
		case "ANDAR":{
			modelType=loader.loadModel(SHIPMODELANDARLOCATION);
			modelType.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			modelType.setLocalRotation(rot);
			modelType.setLocalScale(scale);
		}
		break;
		default:
			modelType=loader.loadModel(SHIPMODELANDARLOCATION);
			modelType.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			modelType.setLocalRotation(rot);
			modelType.setLocalScale(scale);
			break;
		}
		
		// Constructor places the ghost on the grid
		Ghost ghost = new Ghost(
			ghostID, gameGrid, x, y, ghostPlayerNumber,
			new Ship(100, 50, ghostPlayerNumber, modelType)
		);
		addGameWorldObject(ghost.getShip().getShipModel());
		
		playersInGame[ghostPlayerNumber] = ghost;
		
		generateNewScoreboardString(ghostPlayerNumber);
		//scoreboard[ghostPlayerNumber].setColor(playerColors[ghostPlayerNumber]);
	}

	public void updateGhostPosition(UUID ghostID, int x, int y){
		for(int i = 0; i<playersInGame.length; i++) {
			if(playersInGame[i] != null && playersInGame[i].getID().equals(ghostID)) {
				gameGrid.setGhostAt(x, y, playersInGame[i].getShip());
			}
		}
	}

	public void removeGhostAvatar(UUID ghostID,String playerNum) {
		int numOfPlayerToRemove=Integer.parseInt(playerNum);
		gameGrid.removeShip(numOfPlayerToRemove);
		if (playersInGame[numOfPlayerToRemove] != null && playersInGame[numOfPlayerToRemove].getShip() != null) {
			removeGameWorldObject(playersInGame[numOfPlayerToRemove].getShip().getShipModel());
			playersInGame[numOfPlayerToRemove].removeShip();
		}
	}
	
	public void checkNewTrenchGenerator(String numOfPlayerToRemove) {
		int playerNum = Integer.parseInt(numOfPlayerToRemove);
		if (playerNum == trenchGenerator) {
			for (Player p : playersInGame) {
				if (p != null && p.getShip() != null) {
					trenchGenerator = p.getPlayerNumber();
				}
			}
		}
	}

	public String getPlayerScore() {
		return Integer.toString(playersInGame[0].getScore());
	}

	public void updateGhostScore(UUID ghostID, String ghostScore) {
		for(int i=0;i<playersInGame.length;i++){
			if(playersInGame[i]!=null){
				if(playersInGame[i].getID().equals(ghostID)){
					playersInGame[i].setScore(Integer.parseInt(ghostScore));
					if(gameEnd){
						finalScoreboard[i].setText("  "+i
								+"           "+playersInGame[i].getPlayerDeaths()
								+"          "+customFormat(playersInGame[i].getShotsFired())
								+"        "+customFormat(playersInGame[i].getScore()));
					}
				}
			}
		}
	}
	
	public void respawnGhost(UUID ghostID, int x, int y, String shipModel, int ghostPlayerNumber){
		TriMesh modelType;
		switch(shipModel){
		case "ICARUS":{
			modelType=loader.loadModel(SHIPMODELICARUSLOCATION);
			modelType.setTexture(icarusTexture);
			Matrix3D rot = new Matrix3D();
			rot.rotate(90.0, 0.0, 0.0);
			modelType.setLocalRotation(rot);
		}
		break;
		case "ANDAR":{
			modelType=loader.loadModel(SHIPMODELANDARLOCATION);
			modelType.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			modelType.setLocalRotation(rot);
			modelType.setLocalScale(scale);
		}
		break;
		default:
			modelType=loader.loadModel(SHIPMODELANDARLOCATION);
			modelType.setTexture(andarTexture);
			Matrix3D rot = new Matrix3D();
			Matrix3D scale = new Matrix3D();
			rot.rotate(0.0, 90.0, 0.0);
			scale.scale(0.5, 0.5, 0.3);
			modelType.setLocalRotation(rot);
			modelType.setLocalScale(scale);
			break;
		}
		
		// Just in case the ghost wasn't already crashed for some reason
		for(int a=0;a<playersInGame.length;a++) {
			if(playersInGame[a]!=null && playersInGame[a].getID()==ghostID){
					removeGameWorldObject(playersInGame[a].getShip().getShipModel());//Working?
					gameGrid.removeShip(ghostPlayerNumber);			
			}
		}

		Ghost ghost = new Ghost(
			ghostID, gameGrid, x, y, ghostPlayerNumber,
			new Ship(100, 50, ghostPlayerNumber, modelType)
		);
		playersInGame[ghostPlayerNumber].setShip(ghost.getShip());
		addGameWorldObject(ghost.getShip().getShipModel());
	}

	public void gracefulClose() {
		ourClient.sendByeMessage(user.getPlayerNumber());
		try {
			ourClient.shutdown();
		} catch (IOException e) {
			e.printStackTrace();
		}
		display.close();
		shutDownAudio();
		this.setGameOver(true);
		super.shutdown();
	}

	private void shutDownAudio() {
		laserSound.release(audioMgr);
		codyLaser.release(audioMgr);
		failedMove.release(audioMgr);
		shieldHit.release(audioMgr);
		shieldFail.release(audioMgr);
		healthHit.release(audioMgr);
		explosion.release(audioMgr);
		alarm.release(audioMgr);
		andarDeclaration.release(audioMgr);
		icarusDeclaration.release(audioMgr);
		musicMasters.release(audioMgr);
		soundResource1.unload();
		soundResource2.unload();
		soundResource3.unload();
		soundResource4.unload();
		soundResource5.unload();
		soundResource6.unload();
		soundResource7.unload();
		soundResource8.unload();
		soundResource9.unload();
		soundResource10.unload();
		musicResource1.unload();
		audioMgr.shutdown();
		
	}
	public void createLaser(Point3D p) {
		Color laserColor=Color.white;
		switch(user.getPlayerNumber()){
		case 1: laserColor=Color.red;
			break;
		case 2: laserColor=Color.green;
			break;
		case 3:	laserColor=Color.cyan;
			break;
		case 4: laserColor=Color.magenta;
			break;
		default:	laserColor=Color.white;
				break;
		}
		Point3D endOfLaser = new Point3D(p.getX(),p.getY(),p.getZ()+LASERLENGTH);
		Line ready =new Laser(p,endOfLaser,laserColor,user.getPlayerNumber());
		addGameWorldObject(ready);
		lc1.addControlledNode(ready);
		ready.addController(lc1);
		laserCollection.add(ready);//!!!
		laserSound.play(60, false);
		ourClient.sendNewLaserMessage(p,user.getPlayerNumber());
		user.addShotFired();
	}
	
	public void createGhostLaser(Point3D p, int playerNum) {
		Color laserColor=Color.white;
		switch(playerNum){
		case 1: laserColor=Color.red;
			break;
		case 2: laserColor=Color.green;
			break;
		case 3:	laserColor=Color.cyan;
			break;
		case 4: laserColor=Color.magenta;
			break;
		default:	laserColor=Color.white;
				break;
		}
		Point3D endOfLaser = new Point3D(p.getX(),p.getY(),p.getZ()+LASERLENGTH);
		playersInGame[playerNum].addShotFired();
		Line ghostLaser =new Laser(p,endOfLaser,laserColor,playerNum);
		addGameWorldObject(ghostLaser);
		lc1.addControlledNode(ghostLaser);
		ghostLaser.addController(lc1);
		laserCollection.add(ghostLaser);
		codyLaser.setLocation(p);//Sets the origin of the sound to that of the GhostAvatar
		codyLaser.play(60, false);
	}
	
	public void failedMovement() {
		failedMove.setLocation(camera.getLocation());
		failedMove.play(40, false);
	}

	public void toggleScoreboard() {
		showScoreboard=!showScoreboard;
		if(showScoreboard){
			updateScoreboard();
			showScoreboard();
		}else{
			removeScoreboard();
		}
	}

	public void showScoreboard() {
		for(int i=1;i<playersInGame.length;i++){	
			if(scoreboard[i]!=null){
				camera.addToHUD(scoreboard[i]);
			}
		}
	}

	public void removeScoreboard() {
		for(int i=1;i<playersInGame.length;i++){
				camera.removeFromHUD(scoreboard[i]);
		}
	}
	
	//When a player is added to the array of players, a new HUDString is generated for
	//them, including assigning their color.
	public void generateNewScoreboardString(int playerNum) {
		scoreboard[playerNum] = new HUDString("");
		scoreboard[playerNum].setColor(playerColors[playerNum]);
		scoreboard[playerNum].setLocation(0.1, (0.3-(0.05*playerNum)));
		scoreboard[playerNum].setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		scoreboard[playerNum].setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
	}
	
	//Update Scoreboard pulls the scores of all players from the array of players 
	public void updateScoreboard() {
		for(int i=1;i<playersInGame.length;i++){
			if(playersInGame[i]!=null){
				scoreboard[i].setText("Player "+i+": "+playersInGame[i].getScore());
			}
		}
	}
	
	//This method is used to get the relevant info of a player and send it to the client,
	//which then sends it to the server, which then sends it to other users.
	public String[] getPlayerInfo() {
		String[] info=new String[5];
		info[0]=Double.toString(user.getShip().getLocation().getX());
		info[1]=Double.toString(user.getShip().getLocation().getY());
		info[2]=Double.toString(user.getShip().getLocation().getZ());
		info[3]=user.getShip().getTypeString();
		info[4]=Integer.toString(user.getPlayerNumber());
		return info;
	}
	
	/**
	 * Sets the physics for the given ship object
	 * @param s - ship
	 */
	public void setPhysics(Ship s) {
		float mass = 1.0f;
		shipP = physicsEngine.addSphereObject(physicsEngine.nextUID(),
				mass, s.getShipModel().getWorldTransform().getValues(), 0.0f);
		shipP.setBounciness(0.5f);
		float[] linearVel = new float[3];
		linearVel[1] = -5;
		linearVel[2] = 50;
		shipP.setLinearVelocity(linearVel);
		s.getShipModel().setPhysicsObject(shipP);
	}
	
	public String getVersion(){
		return VERSION;
	}

	public void sendMoveMessage(Ship s) {
		ourClient.sendMoveMessage(s.getLocation());
	}
	
	public void sendRespawnMessage() {
		ourClient.sendRespawnMessage(getPlayerInfo());
	}
	
	public void notifyBegin() {
		ourClient.sendBeginMessage();		
	}
	public Rectangle[] makeFontBlock(String phrase){
		Rectangle[] block=new Rectangle[phrase.length()];
		
		return block;
	}
	public void sendDeathMessage(){
		ourClient.sendDeathMessage();
	}
	
	public void ghostDeath(UUID ghostID){
		for(int i=0;i<playersInGame.length;i++) {
			if(playersInGame[i]!=null && playersInGame[i].getID().equals(ghostID)) {
				killShip(playersInGame[i].getShip());
			}
		}
	}
	
	public void killShip(Ship s) {
		playersInGame[s.getPlayerNum()].subtractFromScore(10);
		playersInGame[s.getPlayerNum()].addDeath();
		s.explode();
		explosion.play(10, false);
		gameGrid.removeShip(s.getPlayerNum());
		setPhysics(s);
	}
	
	/**
	 * Respawns the ship for the player.
	 * Creates a new ship and new HUD
	 */
	public void respawn(){
		camera.removeFromHUD(respawnDirections);
		initShip(user.getLastModelType());
		gameGrid.respawn(user.getShip());
		clearShipCommands();
		resetShipCommands();
		initHUD();
	}

	public void moveSelectWindowRight(){
		lobbySelector=lobbySelector-1;
		if(lobbySelector<0){
			lobbySelector=0;
		}
		Matrix3D moveRight = new Matrix3D();
		moveRight.translate(lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getX()-2, lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getY()-3, 0);
		lobbyLineGroup.setLocalTranslation(moveRight);
		checkKonami("RIGHT", "B");
	}

	public void moveSelectWindowLeft(){
		lobbySelector=lobbySelector+1;
		if(lobbySelector>(lobbyModels.length-1)){
			lobbySelector=(lobbyModels.length-1);
		}
		Matrix3D moveLeft = new Matrix3D();
		moveLeft.translate(lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getX()-2, lobbyModels[lobbySelector].getLocalTranslation().getCol(3).getY()-3, 0);
		lobbyLineGroup.setLocalTranslation(moveLeft);
		checkKonami("LEFT", "A");
	}
	public void moveSelectWindowDown(){
		checkKonami("DOWN", "_");
	}
	public void moveSelectWindowUp(){
		checkKonami("UP", "_");
	}
	private void checkKonami(String s, String t){
		if(s.equals(konamiCode[konamiCounter])||t.equals(konamiCode[konamiCounter])){
			if(konamiCounter==konamiCode.length-1){
				toggleDangerZone();
				ding.play(15, false);
				konamiCounter=0;
			}else{
				konamiCounter++;
			}
		}else{
			konamiCounter=0;
		}
	}
	
	private void toggleDangerZone() {
		dangerZonePlaying=!dangerZonePlaying;
		backgroundMusic.stop();
		if(dangerZonePlaying){
			backgroundMusic=musicDangerZone;
		}else{
			backgroundMusic=musicMasters;
		}
	}
	
	public void addDangerZone(DangerZone dz){
		addGameWorldObject(dz);
	}

	public void removeDangerZone(DangerZone dz){
		removeGameWorldObject(dz);
	}
	public void endGame(){
		gameEnd = true;
		clearShipCommands();
		camera.removeFromHUD(health);
		camera.removeFromHUD(shields);
		initFinalScoreboard();
		ourClient.sendScoreMessage(Integer.toString(user.getScore()));
		populateFinalStats();
	}
	private void initFinalScoreboard() {
		for(int i=0;i<playersInGame.length;i++){
			finalScoreboard[i] = new HUDString("");
			finalScoreboard[i].setColor(playerColors[i]);
			finalScoreboard[i].setLocation(0.4, (0.9-(0.05*i)));
			finalScoreboard[i].setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
			finalScoreboard[i].setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		}
		finalScoreboard[0].setText("Player  Deaths  Shots  Score");
	}
	public void populateFinalStats(){
		removeScoreboard();
		camera.addToHUD(finalScoreboard[0]);
		for(int i=1;i<playersInGame.length;i++){//1-4, NOT 0!!!
			if(playersInGame[i]!=null){
				finalScoreboard[i].setText("  "+i+"           "+playersInGame[i].getPlayerDeaths()
						+"          "+customFormat(playersInGame[i].getShotsFired())+"        "+customFormat(playersInGame[i].getScore()));
				camera.addToHUD(finalScoreboard[i]);
			}
		}
		
	}
	static public String customFormat(int value ){
	      String output = myFormatter.format(value);
	      return output;
	}
	private void turretsFire(){
		Iterator<Turret> i = turretCollection.iterator();
		while(i.hasNext()){
			Object temp = i.next();
			if(temp instanceof Turret){
				if(((Turret)temp).getPhase1HasFired() == false && 
						((Turret)temp).getTrenchNum() == currentTrenchNumber-5 &&
						playersInGame[((Turret)temp).getPlayerNum()] != null &&
						playersInGame[((Turret)temp).getPlayerNum()].hasShip()){
					((Turret) temp).setPhase1HasFired();
					int xLoc = (int) playersInGame[((Turret)temp).getPlayerNum()].getShip().getLocation().getX();
					int yLoc = (int) playersInGame[((Turret)temp).getPlayerNum()].getShip().getLocation().getY();
					gameGrid.createDangerZone(xLoc, yLoc, ((Turret)temp).getTurretModel(), 50, true);
				}
				if(((Turret)temp).getPhase2HasFired() == false && 
						((Turret)temp).getTrenchNum() == currentTrenchNumber-3 &&
						playersInGame[((Turret)temp).getPlayerNum()] != null &&
						playersInGame[((Turret)temp).getPlayerNum()].hasShip()){
					((Turret) temp).setPhase2HasFired();
					int xLoc = (int) playersInGame[((Turret)temp).getPlayerNum()].getShip().getLocation().getX();
					int yLoc = (int) playersInGame[((Turret)temp).getPlayerNum()].getShip().getLocation().getY();
					gameGrid.createDangerZone(xLoc, yLoc, ((Turret)temp).getTurretModel(), 50, true);
				}
			}
		}
	}
}
