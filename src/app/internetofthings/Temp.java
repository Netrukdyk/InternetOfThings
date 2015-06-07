package app.internetofthings;

public class Temp extends Device{
	private String temp;

	public Temp() {
	}

	public Temp(String id, String alias, String temp) {
		setId(id);
		setAlias(alias);
		setTemp(temp);
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getTemp() {
		return this.temp;
	}
}
