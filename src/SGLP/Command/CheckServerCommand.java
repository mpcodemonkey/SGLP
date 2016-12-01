package SGLP.Command;

import SGLP.GameInfo;
import SGLP.GameLauncher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by ubufu on 11/27/2016.
 */
public class CheckServerCommand implements ActionListener{

    GameLauncher gl;
    GameInfo activeGame;

    public CheckServerCommand(GameLauncher launcher){
        gl = launcher;
    }

    public void setActiveGame(GameInfo activeNameInfo){
        this.activeGame = activeNameInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gl.checkGameServer(activeGame);

    }
}
