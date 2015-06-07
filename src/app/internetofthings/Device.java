package app.internetofthings;

public class Device {
	protected String alias;
	protected String	id;

	public Device() {
	}

	public Device(String id, String alias) {
		setId(id);
		setAlias(alias);
	}

	public void setAlias(String alias) {
		this.alias = (alias != null && alias != "") ? alias : "Device";
	}

	public String getAlias() {
		return this.alias;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}
}
