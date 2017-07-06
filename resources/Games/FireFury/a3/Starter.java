package a3;

import game.theGame.TheGame;


/**
 * User: Sam Kerr
 * Date: 3/4/14
 * Time: 6:42 PM
 */
public class Starter {

    public static void main(String[] args) {
        TheGame game = new TheGame();
        if(args.length == 3)
            game.expoStart(args[1], args[2]);
        else game.start();
    }

}
