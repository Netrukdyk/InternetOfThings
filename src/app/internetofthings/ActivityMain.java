package app.internetofthings;

import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class ActivityMain extends Activity implements OnClickListener {

	Button send;
	ImageView serverStatus;
	Server server;
	Handler serverHandler;

	SharedPreferences preferences;
	TextView status, debug, list;
	LinearLayout devList;
	String serverName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getOverflowMenu();

		devList = (LinearLayout) findViewById(R.id.devList);

		serverStatus = (ImageView) findViewById(R.id.statusImage);
		status = (TextView) findViewById(R.id.statusText);
		list = (TextView) findViewById(R.id.list);
		startServer();
	}

	private void startServer() {
		if (server == null) {
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
			if (menuKeyField != null) {
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
				status.append(" (" + serverName + ")");
				break;
			case OTHER:
				if (msg.getData().get("Server") == "Disconnected")
					reconnect();
				else {
					String json = msg.getData().getString("Server");
					list.setText(json + '\n' + list.getText());
					parse(json);
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

	// private void setServerStatus(int state, String reason) {
	// switch (state) {
	// case 0:
	// serverStatus.setImageResource(R.drawable.status_red);
	// break;
	// case 1:
	// serverStatus.setImageResource(R.drawable.status_green);
	// break;
	// }
	// status.setText(reason);
	// }
	Boolean a = false;

	@Override
	public void onClick(View v) {

	}

	// ------------------------------------------------------------------

	private void parse(String jsonText) {
		if (jsonText.equals("{}"))
			return;
		try {
			JSONObject FullJson = new JSONObject(jsonText);

			for (int i = 0; i < FullJson.names().length(); i++) {
				String id = FullJson.names().getString(i);
				JSONObject json = FullJson.getJSONObject(id);
				String type = json.getString("type");
				Log.v("JSON", json.toString());
				if (type.equals("device")) {
					String alias = json.getString("alias");
					;
					if (id.charAt(0) == 'R') {
						Boolean relay = (json.getInt("relay") == 1) ? true : false;
						addSocket(new Socket(id, alias, relay));
					} else if (id.charAt(0) == 'T') {
						String temp = json.getString("temp");
						addTemp(new Temp(id, alias, temp));
					}

				} else if (type.equals("del_device")) {
					removeDevice(id);
				} else if (type.equals("changed")) {
					if (id.charAt(0) == 'R') {
						Boolean relay = (json.getInt("relay") == 1) ? true : false;
						Switch switch1 = (Switch) devList.findViewWithTag(id).findViewById(R.id.switch1);
						switch1.setChecked(relay);
					} else if (id.charAt(0) == 'T') {
						String temp = json.getString("temp");
						TextView value = (TextView) devList.findViewWithTag(id).findViewById(R.id.value);
						value.setText(temp);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void addSocket(final Socket socket) {
		View v = getLayoutInflater().inflate(R.layout.socket_item, null);
		v.setTag(socket.getId());
		Switch toggle = (Switch) v.findViewById(R.id.switch1);
		toggle.setText(socket.getAlias());
		toggle.setChecked(socket.getRelay());
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int relay = (isChecked) ? 1 : 0;
				String data = "{\"" + socket.getId() + "\":{\"type\":\"request\",\"relay\":" + relay + "}}";
				sendToServer(data);
			}
		});
		devList.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	private void removeDevice(String id) {
		devList.removeViewInLayout(devList.findViewWithTag(id));
	}

	private void addTemp(Temp temp) {
		View v = getLayoutInflater().inflate(R.layout.temp_item, null);
		v.setTag(temp.getId());
		TextView alias = (TextView) v.findViewById(R.id.alias);
		TextView value = (TextView) v.findViewById(R.id.value);
		alias.setText(temp.getAlias());
		value.setText(temp.getTemp());
		devList.addView(v, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

	}
}
