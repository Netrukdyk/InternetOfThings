package app.internetofthings;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityMain extends Activity implements OnClickListener {

	Button send;
	ImageView serverStatus;
	Server server;
	Handler serverHandler;

	SharedPreferences preferences;
	TextView status, debug, list;

	String serverName = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getOverflowMenu();

		serverStatus = (ImageView) findViewById(R.id.statusImage);
		status = (TextView) findViewById(R.id.statusText);
		list = (TextView) findViewById(R.id.list);
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(this);
		startServer();
	}

	private void startServer() {
		if(server == null){
			server = new Server(uiHandler);
			server.start();
		}
	}

	private void killServer() {
		if (server != null && server.getStatus() == 1) {
			server.kill();
			server = null;
		}
	}

	private void reconnect() {
		killServer();
		startServer();
	}

	private void getOverflowMenu() {

	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	@Override
	protected void onDestroy() {
		killServer();
		super.onDestroy();
	}

	// Apdoroja þinutes, gautas ið serverio
	@SuppressLint("HandlerLeak")
	public Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			C.Type type = C.Type.values()[msg.what];

			switch (type) {
			case STATUS:
				if (msg.arg1 == 1)
					setServerStatus(1);
				else
					setServerStatus(0);
				break;
			case INFO:
				serverName = msg.getData().getString("Server");
				status.append(" ("+serverName+")");
				break;
			case OTHER:
				if (msg.getData().get("Server") == "Disconnected")
					reconnect();
				else {
					list.setText(msg.getData().getString("Server")+'\n'+list.getText());
				}
				break;
			}
		};
	};

	private void sendToServer(String text) {
		if (server != null && server.getStatus() != 0) {
			serverHandler = server.getHandler();
			Message msgObj = serverHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("CMD", text);
			msgObj.setData(b);
			serverHandler.sendMessage(msgObj);
		} else
			setServerStatus(0);

	}

	private void setServerStatus(int state) {
		switch (state) {
		case 0:
			serverStatus.setImageResource(R.drawable.status_red);
			status.setText("Not connected");
			break;
		case 1:
			serverStatus.setImageResource(R.drawable.status_green);
			status.setText("Connected");
			break;
		}
	}

//	private void setServerStatus(int state, String reason) {
//		switch (state) {
//		case 0:
//			serverStatus.setImageResource(R.drawable.status_red);
//			break;
//		case 1:
//			serverStatus.setImageResource(R.drawable.status_green);
//			break;
//		}
//		status.setText(reason);
//	}
	Boolean a = false; 
	@Override
	public void onClick(View v) {
		a = !a;
		int relay = (a) ? 1 : 0;
		String data = "{\"d10001\":{\"type\":\"request\",\"relay\":\""+relay+"\"}}";
//		String msg = "";
		switch (v.getId()) {
		case R.id.send:
			sendToServer(data);
			break;
		}
		
	}

}
