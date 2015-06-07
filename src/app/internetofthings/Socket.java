package app.internetofthings;

public class Socket extends Device{
	private Boolean relay;

	public Socket() {
	}

	public Socket(String id, String alias, Boolean relay) {
		setId(id);
		setAlias(alias);
		setRelay(relay);
	}

	public void setRelay(Boolean relay) {
		this.relay = relay;
	}

	public Boolean getRelay() {
		return this.relay;
	}
}
