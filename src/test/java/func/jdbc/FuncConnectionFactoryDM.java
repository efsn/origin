package func.jdbc;

class FuncConnectionFactoryDM extends FuncConnectionFactory {

	public FuncConnectionFactoryDM() {
	}

	public String getUrl(String ip, String sid, String port) {
		return "jdbc:dm:://" + getDefaultIp(ip) + ":" + getDefaultPort(port, "12345") + "/" + sid;
	}

	public String getName() {
		return DB_DM;
	}

	protected String[] getCustom() {
		return CUSTOMJDBC[7];
	}

	public String getDriver() {
		return DRIVER_DM;
	}
}
