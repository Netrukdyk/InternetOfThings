package app.internetofthings;

public class C {
	public static final String IP = "85.232.137.238";
	public static final int PORT = 9000;
	enum Type {
		STATUS, INFO, OTHER// 0, 1, 2
	}
	enum ServerStatus {
		Disconnected, Connecting, Connected
	}
}
