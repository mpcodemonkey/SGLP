package SGLP.Command;

import SGLP.Environment;
import SGLP.ExecutionManager.ExecutionManager;
import SGLP.ExecutionManager.ServerExecutionManager;
import SGLP.GameInfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by ubufu on 11/15/2016.
 *
 * A general command execution class that can launch a process on either the
 * client or server. It launches the game located in the Games\[game name] folder
 * if it is not already running
 */
public class ExecutionCommand implements ActionListener {

    private ExecutionManager em = null;
    private Environment environment = null;
    private GameInfo activeGame;
    private String runType = null;

    public ExecutionCommand(ExecutionManager em, Environment environment, String runType){
        this.em = em;
        this.environment = environment;
        this.runType = runType;
    }

    public void setActiveGame(GameInfo activeNameInfo){
        this.activeGame = activeNameInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String mCommand = "cmd /c " + environment.getPath(activeGame.getSemester()) + " " + (runType.equalsIgnoreCase("client") ? activeGame.getClientExecutable() : activeGame.getServerExecutable());
        em.launchProcess(mCommand, activeGame);

    }
}
