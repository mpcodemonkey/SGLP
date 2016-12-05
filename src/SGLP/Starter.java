package SGLP;

/**
 * Created by ubufu on 11/15/2016.
 */
public class Starter {
    public static void main(String[] args)
    {
        boolean mode;
        if(args == null || args.length == 0){
            System.out.println("Usage: java SGLP.Starter client|server");
            return;
        }
        mode = args[0].equalsIgnoreCase("client") ? true : false;
        new GameLauncher().start(mode);
    }
}
