package app.internetofthings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import app.internetofthings.C.Type;

class Server extends Thread {

	private OutputStream	out;
	private InputStream		in;
	private PrintWriter		output;
	private Socket			s				= null;
	private int				server_state	= 0;
			
	// Serverio Handleris, apdoroja þinutes ið UI
	@SuppressLint("HandlerLeak")
	private Handler			serverHandler	= new Handler() {
												@Override
												public void handleMessage(Message msg) {

													String cmd = msg.getData().getString("CMD");
													Log.v("Sending", cmd);
													output.print(cmd);
													output.flush();
													if (output.checkError()) {
														Log.v("Socket", "Error");
														setStatus(0);
														disconnect();
														sendToUI(C.Type.OTHER, "Disconnected");
													}
												}
											};
	private String			serverIP		= "mindis.inotecha.lt";
	private int				serverPORT		= 9000;
	private String			serverName		= "mindis.inotecha.lt";
	
	String devInit = "{\"a300S1\":{\"type\":\"android\",\"alias\":\"JIAYU S1\"}}";
	private Handler			uiHandler;

	public Server(Handler handler) {
		super("Worker");
		Log.v("Server", "Create");
		this.uiHandler = handler;
	}

	private void connect() {
		do {
			try {
				Log.v("Server", this.serverIP + " connecting...");
				s = new Socket();
				s.connect(new InetSocketAddress(this.serverIP, this.serverPORT), 1000);

				if (s.isConnected()) {
					setStatus(1);
					Log.v("Server", "Socket connected");
					sendToUI(C.Type.INFO, serverName);
					out = s.getOutputStream();
					in = s.getInputStream();
					output = new PrintWriter(out, true);
					
				}

			} catch (SocketTimeoutException e2) {
				Log.v("Server", "Socket timeout " + serverIP + ":" + serverPORT);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (UnknownHostException e1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				e1.printStackTrace();
			} catch (IOException e1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				e1.printStackTrace();
			}
		} while (getStatus() != 1);
	}

	private void disconnect() {
		if (getStatus() != 0) {
			try {
				if (output != null)
					output.close();
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (s != null)
					s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setStatus(0);
		}
	}

	// getter for local Handler
	public Handler getHandler() {
		return this.serverHandler;
	}

	public int getStatus() {
		return this.server_state;
	}

	public void kill() {
		disconnect();
		Log.v("Server", "Killed");
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void run() {
		Log.v("Server", "Run");
		// Pair<String, String> server = findServer(broadcastIp);
		// serverIP = server.first;
		// serverName = server.second;
		sendStatusToUI();
		connect();
		output.print(devInit);
		output.flush();
		
		Log.v("TCP", "Trying to read...");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		char[] bytes = new char[1000];
		
		StringBuilder total = new StringBuilder();
		while (true) {
			
			try {
				int numRead = 0;
				while ((numRead = br.read(bytes)) >= 0) {
				    total.append(new String(bytes, 0, numRead));
				    Log.v("TCP", total.toString());
				    sendToUI(C.Type.OTHER, total.toString());
				    total.setLength(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} // End of Run

	// Suformuoja þinutæ ir iðsiunèia UI
	private void sendStatusToUI() {
		Message msg = new Message();
		msg.what = 0;
		msg.arg1 = server_state;
		uiHandler.sendMessage(msg);
	}

	private void sendToUI(Type type, String message) {
		Bundle data = new Bundle();
		data.putString("Server", message);
		Message msg = new Message();
		msg.what = type.ordinal(); // MsgType
		msg.setData(data);
		uiHandler.sendMessage(msg);
	}

	public void setIp(String ip) {
		this.serverIP = ip;
	}

	public void setStatus(int state) {
		this.server_state = state;
		sendStatusToUI();
	}

} // End of Server
