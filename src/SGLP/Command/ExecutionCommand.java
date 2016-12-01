package SGLP.Command;

import SGLP.ExecutionManager.ExecutionManager;
import SGLP.ExecutionManager.ServerExecutionManager;
import SGLP.GameInfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by ubufu on 11/15/2016.
 *
 * A general command execution class that can launch a process on either the
 * client or server. It launches the game located in the Games\[game name] folder
 * if it is not already running
 */
public class ExecutionCommand implements ActionListener {

    private ExecutionManager em = null;
    private GameInfo activeGame;

    public ExecutionCommand(ExecutionManager em){
        this.em = em;
    }

    public void setActiveGame(GameInfo activeNameInfo){
        this.activeGame = activeNameInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        em.launchProcess(/*activeGame.getCommand()*/"cmd /c \"Games\\" + activeGame.getFolder() + " && " + activeGame.getCommand(), activeGame.getName());

    }
}
