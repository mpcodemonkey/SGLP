package a3;

import java.awt.Button;
import java.awt.Frame;
import java.io.IOException;

import javax.swing.JOptionPane;

import javafx.scene.chart.Axis;
import my_package.Axes;
import my_package.Cam3PController;
import my_package.CameraObject;
import my_package.DisplaySystem;
import my_package.FloorModel;
import my_package.Input;
import my_package.MoleModel;
import my_package.My3DSound;
import my_package.MyHud;
import my_package.MyScript;
import my_package.MySound;
import my_package.NetworkClient;
import my_package.Ogre3DModel;
import my_package.PhysicsEngine;
import my_package.Pyramid;
import my_package.SkyBoxObject;
import my_package.TerrainObject;
import my_package.TreeGroup;
import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.scene.SceneNode;
import net.java.games.input.Component.Identifier.Key;
import net.java.games.input.Component.Identifier;

public class MoleSeeker extends BaseGame
{
	// ************************
	// Begin Expo Modifications
	// ************************
	private String serverIP;
	private int serverPort;
	// ************************
	// End Expo Modifications
	// ************************


	// Input Constants
    private static final int igamepad = 0;  // flag which tells functions to perform actions on the keyboard
    private static final int ikeyboard = 0;	// flag which tells functions to perform actions on the gamepad
    
    // Input Choice
    private static final boolean activate_gamepad = false;	// to remove annoying crashes when not using a gamepad
    
	// Object Variables
	private DisplaySystem 		display_system 		= new DisplaySystem(this);
	private CameraObject 		camera_obj 			= new CameraObject(this);
	private Axes 				axes 				= new Axes(this);
	private Pyramid 			pyramid 			= new Pyramid(this);
	private Input				input				= new Input(this);
	private Cam3PController 	cam3P				= new Cam3PController(this);
	private NetworkClient		client				= null;	// because of try-catch requirement, network object can't be instantiated here so make it null
	private SkyBoxObject		skybox_obj			= new SkyBoxObject(this); // "SkyBoxObject" is a user-defined object; it is called "SkyBoxObject" instead of "SkyBox" to avoid conflict with sage's reserved name
	private TerrainObject		terrain_obj			= new TerrainObject(this, display_system);
	private MoleModel			molemodel			= new MoleModel(this);
	private FloorModel			floormodel			= new FloorModel(this);
	private PhysicsEngine		physics				= new PhysicsEngine(this, input, 1);
	private Ogre3DModel			boxhuman			= new Ogre3DModel(this, input);
	private Ogre3DModel			bomb				= new Ogre3DModel(this, input);
	private Ogre3DModel			hammer				= new Ogre3DModel(this, input);
	private MyHud				hud					= new MyHud(this);
	private TreeGroup			tree				= new TreeGroup(this);
	private MyScript			script				= new MyScript();
	private My3DSound			bg_music			= new My3DSound();
	
	// References
	private SceneNode player;
	
	// Attributes
	int player_color = 0; // supports 5 colors: 0=RED  1=BLUE  2=ORANGE 3=GREEN 4=YELLOW

	// Manager Variables
	IEventManager eventMgr;

	// ************************
	// Begin Expo Modifications
	// ************************
	public void expoStart(String ip, int port){
		serverIP = ip;
		serverPort = port;

		start();
	}
	// ************************
	// End Expo Modifications
	// ************************
	
    public void initSystem()
    {
		if (script.getScreenMode() == 1) // Java script will set the screen mode
			display_system.SetDefaultFullscreenResolution();
    	else
			display_system.SetDefaultResolution(false);
    }
    
    public void initGame()
    {    	
    	// ask player to choose color
    	AskPlayerColor();
    	
    	// initialize event manager
    	eventMgr = EventManager.getInstance();
    	
    	// camera
    	camera_obj.InitializeCamera();
    	
    	// skybox/terrain
    	skybox_obj.InitializeSkyBox(camera_obj.GetICamera());     	
    	terrain_obj.InitializeTerrain();
    	
    	// standard scene objects
    	//axes.AddToGameWorld();
    	//player = pyramid.AddToGameWorld(-5,0,0);

    	/*floormodel.Add();
    	floormodel.SetRotation(90, new Vector3D(1,0,0));
    	floormodel.SetRotation(20, new Vector3D(0,0,1));
    	floormodel.SetScale(10, 10, 10);*/
    	
    	// .obj scene objects
//    	if(molemodel.LoadModel("src\\resources\\mole_blender.obj"))
//    	{
//    		molemodel.LoadTexture("src\\resources\\mole_blender.png");
//    		molemodel.SetLocation(5,1,0);
//    		molemodel.GetTriMesh().updateGeometricState(1.0f, true);
//    	}      	
    	
    	// ogre3d scene objects
//    	if(boxhuman.Load("src\\resources\\boxhuman.mesh.xml", "src\\resources\\boxhuman.material", "src\\resources\\boxhuman.skeleton.xml", "src\\resources\\boxhuman.jpg", "src\\resources\\sound_swoosh.wav"))
//    	{
//    		boxhuman.AttachKeyboardToAnimation("waving", ikeyboard, Key.SPACE, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
//    	}   	
    	
//    	if(bomb.Load("src\\resources\\bomb.mesh.xml", "src\\resources\\bomb.material", "src\\resources\\bomb.skeleton.xml", "src\\resources\\bomb_FlipY.png"))
//    	{
//    		bomb.AttachKeyboardToAnimation("wick_wave", ikeyboard, Key.SPACE, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);
//    	}
    	
    	if(hammer.Load("src\\resources\\hammer.mesh.xml", "src\\resources\\hammer.material", "src\\resources\\hammer.skeleton.xml", GetHammerTexture(player_color), "src\\resources\\se_hammer_raise_2.wav"))
    	{
    		//hammer.Rotate90AboutX();
    		hammer.AttachKeyboardToAnimation("slam", "src\\resources\\se_hammer_raise_2.wav", ikeyboard, Key.SPACE, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    		if(activate_gamepad) hammer.AttachKeyboardToAnimation("slam", "src\\resources\\se_hammer_raise_2.wav", igamepad, net.java.games.input.Component.Identifier.Button._4, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY); 
    		hammer.DisableMoveForwardActionUponAnimate();
    		player = hammer.GetMesh();
    	}    	

    	// physics
    	/*physics.AssociateInput(Key.SPACE, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	physics.InitializePhysicsSystem();
    	physics.CreatePhysicsWorld_BallOnPlane(molemodel.GetTriMesh(), floormodel.GetMesh());*/
    	
    	// input keyboard
    	input.AttachInputToMoveForwardAction(player, ikeyboard, Key.W, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, -1, terrain_obj.GetTerrainBlock());
    	input.AttachInputToMoveForwardAction(player, ikeyboard, Key.S, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, 1, terrain_obj.GetTerrainBlock());
    	input.AttachInputToRotateAction(player, ikeyboard, Key.A, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, -1);
    	input.AttachInputToRotateAction(player, ikeyboard, Key.D, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, 1);
    	input.AttachInputToQuitGameAction(ikeyboard, Key.ESCAPE, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	
    	// input gamepad
    	if(activate_gamepad)
    	{
	    	input.AttachInputToMoveForwardAction(player, igamepad, Identifier.Axis.Y, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, -1, terrain_obj.GetTerrainBlock());
	    	input.AttachInputToMoveForwardAction(player, igamepad, Identifier.Axis.Y, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, 1, terrain_obj.GetTerrainBlock());
	    	input.AttachInputToRotateAction(player, igamepad, Identifier.Axis.X, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, -1);
	    	input.AttachInputToRotateAction(player, igamepad, Identifier.Axis.X, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN, 1);    	
	    	input.AttachInputToQuitGameAction(igamepad, net.java.games.input.Component.Identifier.Button._9, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);    		
    	}

    	// camera controller
    	cam3P.Attach(camera_obj.GetICamera(), player, input.GetInputManager(), input.GetKeyboardName(ikeyboard), input.GetGamePadName(igamepad));
    	
    	// client
    	try 
    	{
    		client = new NetworkClient(this);
    		//client.ConnectToDefaultLocalServer();
			//client.ConnectToServer();
    		client.ConnectToServer(serverIP, serverPort);
    	} 
    	catch (IOException e) {e.printStackTrace();}
    	
    	// hud
    	hud.InitializeHud();
    	
    	// groups
    	tree.InitializeGroup(eventMgr);
    	
    	// initialize background audio
    	bg_music.InitializeAudio("src\\resources\\its_bond.wav", player, true);
    	
    	// bring to front (else need to manually click icon in fullscreen mode)
    	display_system.GetJFrame().toFront();
    	display_system.GetJFrame().setState(Frame.NORMAL);
    }
    
    public void update(float elapsedTimeMS)
    {
    	physics.Update(elapsedTimeMS);
    	cam3P.Update(elapsedTimeMS);
    	client.Update(elapsedTimeMS);
    	boxhuman.Update(elapsedTimeMS);
    	bomb.Update(elapsedTimeMS);
    	hammer.Update(elapsedTimeMS);
    	tree.Update();
    	skybox_obj.Update();	// to prevent jerkiness, the skybox update should be the last update before we call super.update
    	hud.Update(elapsedTimeMS);
    	super.update(elapsedTimeMS);
    }
    
    protected void render()
    {	
    	camera_obj.Render();
    	super.render();    	
    }
    
	protected void shutdown()
	{
		bg_music.Release();
		client.ShutDown();
		getDisplaySystem().close();
		setGameOver(true);
		super.shutdown();
	}
	
	public void exit()
	{
		super.exit();
	}
	
    public static void main(String[] args)
    {
		// ************************
		// Begin Expo Modifications
		// ************************
		if(args.length == 3){
			String serverIP = "";
			int serverPort = 0;
			try{
				serverIP = args[1];
				serverPort = Integer.parseInt(args[2]);
			}
			catch(NumberFormatException e){
				System.out.println("could not convert " + args[2] + " to integer");
			}
			new MoleSeeker().expoStart(serverIP, serverPort);
		}
		// ************************
		// End Expo Modifications
		// ************************

    	new MoleSeeker().start();

    }	
    
 // ******************************************************************************
 // BASEGAME FUNCTION WRAPPERS
 // ******************************************************************************    
     
     public void AddGameWorldObject(SceneNode obj)
     {
     	addGameWorldObject(obj);
     } 
     
     public void RemoveGameWorldObject(SceneNode node)
     {
     	removeGameWorldObject(node);
     }    
     
     public void ShutDown()
     {
     	shutdown();
     }    
     
 // ******************************************************************************
 // HELPER FUNCTIONS
 // ****************************************************************************** 
     
     public SceneNode GetPlayerObject()
     {
    	 return player;
     }
     
     public Ogre3DModel GetOgrePlayerObject()
     {
    	 return hammer;
     }    
     
     public void IncrementPlayerScore()
     {
    	 hud.IncrementScore();
     }
     
     public void DecrementPlayerScore()
     {
    	 hud.DecrementScore();
     }
     
     public void SetPlayerColor(int color)
     {
    	 // 0=RED  1=BLUE  2=ORANGE 3=GREEN 4=YELLOW
    	 player_color = color;
     }
     
     public String GetHammerTexture(int color)
     {
    	 // 0=RED  1=BLUE  2=ORANGE 3=GREEN 4=YELLOW
    	 String texture = null;
    	 String path = "src\\resources\\";
    	 switch(color)
    	 {
	    	 case 0:
	    	 {
	    		 texture = path + "hammer.png"; // default
	    	 } break;
	    	 
	    	 case 1:
	    	 {
	    		 texture = path + "hammer_blue.png";
	    	 } break;
	    	 
	    	 case 2:
	    	 {
	    		 texture = path + "hammer_orange.png";	    		 
	    	 } break;
	    	 
	    	 case 3:
	    	 {
	    		 texture = path + "hammer_green.png";	    		 
	    	 } break;	 
	    	 
	    	 case 4:
	    	 {
	    		 texture = path + "hammer_yellow.png";	    		 
	    	 } break;	    	 
    	 }
    	 return texture;
     }
     
     private void AskPlayerColor()
     {
 		String color_str = JOptionPane.showInputDialog("Choose your color. 0=RED  1=BLUE  2=ORANGE 3=GREEN 4=YELLOW ");
 		int color = 0;
 		try
 		{
 			color = Integer.parseInt(color_str);
 		}
 		catch(NumberFormatException e)
 		{
 			color = 0;
 		}
 		SetPlayerColor(color);    	 
     }
     
     public int GetPlayerColor()
     {
    	 return player_color;
     }
     
     public Input GetInputObject()
     {
    	 return input;
     }
     
     public void StartBackgroundMusic()
     {
     	// background music
     	// new Thread(new MySound("src\\resources\\its_bond.wav", true)).start();  
    	 bg_music.Play();
     }
     
     public NetworkClient GetNetworkClient()
     {
    	 return client;
     }
     
     public boolean GamePadActive()
     {
    	 return activate_gamepad;
     }
}
