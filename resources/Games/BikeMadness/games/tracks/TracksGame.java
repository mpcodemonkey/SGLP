package games.tracks;

import java.util.UUID;
import sage.app.BaseGame;
import sage.display.IDisplaySystem;
import sage.camera.ICamera;
import sage.scene.SceneNode;
import sage.event.EventManager;
import graphicslib3D.Point3D;
import bardsEngine.scene.Axes;
import bardsEngine.scene.Cube;
import bardsEngine.display.jogl.JOGLFSDisplaySystem;
import bardsEngine.display.jogl.JOGLWindowedDisplaySystem;
import bardsEngine.camera.ICameraController;
import bardsEngine.camera.ThirdPersonDungeonCrawlerCameraController;
import games.tracks.configuration.input.ControllerFactory;
import games.tracks.action.ActionInitializer;
import games.tracks.scene.player.PlayerDeity;
import games.tracks.scene.player.Player;
import games.tracks.scene.player.BikePlayer;
import games.tracks.scene.player.event.PlayerAddedEvent;
import games.tracks.scene.player.event.PlayerMovedEvent;
import games.tracks.scene.skybox.SkyBoxDeity;
import games.tracks.scene.TracksTerrain;
import games.tracks.scene.GameSound;
import games.tracks.scene.gate.StartingGate;
import games.tracks.scene.gate.FinishLine;
import games.tracks.scene.track.RaceTrack;
import games.tracks.scene.track.BezierCurveList;
import games.tracks.scene.hud.HUDManager;
import games.tracks.net.client.ITracksClient;
import games.tracks.net.message.PlayerJoinMessage;
import games.tracks.net.message.PlayerByeMessage;
import games.tracks.configuration.display.DisplayConfiguration;
import games.tracks.scripting.JavaScriptExecuter;
import games.tracks.physics.TracksPhysicsWorld;
import games.tracks.configuration.net.NetworkConfiguration;
import games.tracks.event.StartCountdownEvent;

/**
 * The Tracks game.
 */
public class TracksGame extends BaseGame
{
  public static enum GameState {NOT_STARTED, COUNTDOWN, STARTED};

  private ControllerFactory         ctrlFactory;
  private IDisplaySystem            display;
  private PlayerDeity               playerDeity;
  private ICameraController         camCtrl;
  private ITracksClient             client;
  private DisplayConfiguration      dispConf;
  private SkyBoxDeity               skyBoxDeity;
  private TracksPhysicsWorld        physWorld; 
  private TracksTerrain             terrain;
  private int                       frameCount;
  private GameSound                 gameSound;
  private StartingGate              startingGate;
  private FinishLine                finishLine;
  private HUDManager                hudManager;
  private NetworkConfiguration.Mode networkMode;
  private GameState                 gameState;
  private float                     countdownTime;
  private CollisionDetector         collisionDetector;

  /**
   * Construct the game.
   * @param ctrlFactory A ControllerFactory that has the user-selected input devices.
   * @param client An instance of a TrackClient class.
   * @param dispConf A reference to the game's DisplayConfiguration.
   * @param dispConf A reference to the game's DisplayConfiguration.
   * @param networkMode The network mode for the game (single player, client, server, or demo).
   */
  public TracksGame(ControllerFactory ctrlFactory, ITracksClient client,
    DisplayConfiguration dispConf, NetworkConfiguration.Mode networkMode)
  {
    this.ctrlFactory      = ctrlFactory;
    this.client           = client;
    this.dispConf         = dispConf;
    this.frameCount       = 0;
    this.networkMode      = networkMode;
    this.gameState        = GameState.NOT_STARTED;
    this.countdownTime    = 0;
  }

  /**
   * Create the game's DisplaySystem.
   */
  @Override protected void createDisplay()
  {
    // Create the proper display: fullscreen or windowed.
    if (dispConf.getMode() == DisplayConfiguration.Mode.FULLSCREEN)
      this.display = new JOGLFSDisplaySystem();
    else
      this.display = new JOGLWindowedDisplaySystem(dispConf.getWidth(), dispConf.getHeight());

    // Poll until the display is created (and the renderer is realized).
    while (!this.display.isCreated())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (Exception ex)
      {
        System.out.println("Failed to sleep..");
      }
    }

    // Set the display system and the title.
    this.setDisplaySystem(this.display);
    this.display.setTitle("Bike Madness 16");
  }

  /**
   * Initialize the game.
   */
  @Override protected void initGame()
  {
    ICamera             camera;
    JavaScriptExecuter  scriptExecuter;
    RaceTrack           raceTrack;
    BezierCurveList     trackCurves;
    boolean             isDemoMode = this.networkMode == NetworkConfiguration.Mode.DEMO;
    Player              botPlayer  = null;

    // Set up the skybox (starts at level 1).
    this.skyBoxDeity = new SkyBoxDeity(this);

    // Add the axes to the world.
    //this.addGameWorldObject(new Axes());

    // Add the Terrain.
    this.terrain = new TracksTerrain();
    this.addGameWorldObject(this.terrain.getTerrainBlock());

    // Create the player deity, which manages the game's players.
    this.playerDeity = new PlayerDeity(this, this.networkMode);

    // Add the first player and announce that the player has joined.
    this.playerDeity.addPlayer(this.client.getClientID(), new BikePlayer("Player 1"));
    this.client.sendMessage(new PlayerJoinMessage(this.playerDeity.getPlayer(0)));

    // Add a bot if in sigle player mode.
    if (this.networkMode == NetworkConfiguration.Mode.SINGLE_PLAYER)
    {
      botPlayer = new BikePlayer("NPC");
      this.playerDeity.addPlayer(UUID.randomUUID(), botPlayer);
    }

    // The skybox tracks the player.
    this.skyBoxDeity.setTracked(this.playerDeity.getPlayer(0));

    // Configure the camera (it's a chase camera that tracks the player).
    camera = this.display.getRenderer().getCamera();
    camera.setViewport(0, 1.0, 0, 1.0);
    camera.setPerspectiveFrustum(60, (double)this.display.getWidth() / this.display.getHeight(), 0.01f, 1000f);
    this.camCtrl = new ThirdPersonDungeonCrawlerCameraController(camera, this.playerDeity.getPlayer(0));

    // Initialize the physics world.
    this.physWorld = new TracksPhysicsWorld();
    this.physWorld.addPlayer(this.playerDeity.getPlayer(0));

    if (botPlayer != null)
      this.physWorld.addPlayer(botPlayer);

    // Adds the sound to the game.
    this.gameSound = new GameSound(camera);
    this.gameSound.playBackgroundMusic();

    // Now that the actions/event listeners are wired up notify any listeners
    // that player 1 has been added.
    EventManager.getInstance().triggerEvent(new PlayerAddedEvent(this.playerDeity.getPlayer(0)));

    // Add the race track.  The race track is created via a script, which
    // defines lists of control points for Bezier curves.
    trackCurves    = new BezierCurveList();
    scriptExecuter = new JavaScriptExecuter();
    scriptExecuter.executeScript("./resource/script/raceTrack.js");
    scriptExecuter.invokeFunction("createTrack", trackCurves);

    raceTrack = new RaceTrack(trackCurves);
    for (Cube cube : raceTrack.getTrackParts())
      this.addGameWorldObject(cube);
    this.physWorld.setRaceTrack(raceTrack);

    // Add the starting gate.
    this.startingGate = new StartingGate();
    this.addGameWorldObject(this.startingGate.getSceneNode());
    this.physWorld.setStartingGate(this.startingGate);

    // Add the finish line.
    this.finishLine = new FinishLine(raceTrack.getEndPoint());
    this.addGameWorldObject(this.finishLine);
    this.physWorld.setFinishLine(this.finishLine);

    // Create the HUD manager.  Order is somewhat important here.  This should
    // be last due to a bug in sage.
    this.hudManager = new HUDManager(this.playerDeity.getPlayer(0), this.networkMode);
    this.addGameWorldObject(this.hudManager);
    
    // Announce that the game is ready using a script.
    scriptExecuter = new JavaScriptExecuter();
    scriptExecuter.executeScript("./resource/script/hello.js");

    // Wire up all the actions.
    new ActionInitializer(this, this.ctrlFactory, this.camCtrl, 
      this.playerDeity, this.client, this.networkMode, this.gameSound,
      this.hudManager).initializeActions();

    // Set up collision detection.
    this.collisionDetector = new CollisionDetector(this.playerDeity,
      this.finishLine, this.networkMode, this.client);

    // Start the game if in single-player mode.
    if (this.networkMode == NetworkConfiguration.Mode.SINGLE_PLAYER || 
      this.networkMode == NetworkConfiguration.Mode.DEMO)
      EventManager.getInstance().triggerEvent(new StartCountdownEvent());
  }

  /**
   * Fires on every game pulse.
   * @param elapsed The elapsed time since the last pulse, in milliseconds.
   */
  @Override protected void update(float elapsed)
  {
    this.physWorld.update(elapsed);
    this.skyBoxDeity.update();
    this.camCtrl.update(elapsed);
    this.client.processPackets();
    this.playerDeity.update(elapsed, this.gameState);
    this.hudManager.update(elapsed);
    this.collisionDetector.handleCollisions();
    
    // Dispatch a move event every so often.  Networked players'
    // locations and headings are interpolated!
    if (this.frameCount % 4 == 0)
      EventManager.getInstance().triggerEvent(new PlayerMovedEvent(this.playerDeity.getPlayer(0)));

    this.gameSound.updateEar();

    ++this.frameCount;

    // Raise the starting gate when the countdown is done.
    if (this.gameState == GameState.COUNTDOWN)
    {
      this.countdownTime += elapsed;

      if (this.countdownTime >= 5500)
      {
        this.gameState = GameState.STARTED;
        this.physWorld.removeRigidBody(this.startingGate.getRigidBody());
      }
    }
    
    this.startingGate.update(elapsed, this.gameState);
    super.update(elapsed);
  }

  /**
   * Increase the access level of addGameWorldObject.
   * @param node The node to add.
   */
  @Override public void addGameWorldObject(SceneNode node)
  {
    super.addGameWorldObject(node);
  }

  /**
   * Increase the access level of removeGameWorldObject.
   * @param node The node to remove.
   */
  @Override public boolean removeGameWorldObject(SceneNode node)
  {
    return super.removeGameWorldObject(node);
  }

  /**
   * Cleanly shutdown the game.
   */
  @Override public void shutdown()
  {
    // Send a bye message.
    this.client.sendMessage(new PlayerByeMessage());

    // Close the client connection.
    this.client.shutdown();

    // Make sure the audio is cleaned up.
    this.gameSound.shutdown();

    // End the game.
    super.shutdown();
  }

  /**
   * Start the countdown (e.g. on your marks, get set, go).
   */
  public void startCountdown()
  {
    this.gameState = GameState.COUNTDOWN;
  }
}
