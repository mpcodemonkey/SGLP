package a3;

import java.io.IOException;

import gameEngine.network.TestNetworkingServer;

public final class Starter {
	public static void main(String[] args) {
		if(args.length > 0){
			try {
				new TestNetworkingServer(Integer.parseInt(args[1]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				new TestNetworkingServer(80);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
