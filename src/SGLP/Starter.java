package SGLP;

import javax.swing.*;

/**
 * Created by ubufu on 11/15/2016.
 */
public class Starter {
    public static void main(String[] args)
    {
        boolean mode;

        Object[] options = {"Play Games Plz",
                "I need servers in my life"};
        int choice = JOptionPane.showOptionDialog(null,
                "Whatcha wanna do?",
                "SGLP Game Manager",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     //do not use a custom Icon
                options,  //the titles of buttons
                options[0]); //default button title

        //no need to continue if they don't wanna
        if(choice == JOptionPane.CLOSED_OPTION ) System.exit(0);

        mode = choice == JOptionPane.YES_OPTION ? true : false;
        new GameLauncher().start(mode);
    }
}
