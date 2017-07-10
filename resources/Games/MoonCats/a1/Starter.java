package a1;

import games.cats.*;

public class Starter {
	public static void main(String[] args){
		if(args.length > 0){
			new MoonCats().startExpo(args);
		}
		else
			new MoonCats().start();
	}
}
