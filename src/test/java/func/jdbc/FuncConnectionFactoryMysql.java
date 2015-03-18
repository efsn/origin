package func.jdbc;

import com.esen.util.StrFunc;

class FuncConnectionFactoryMysql extends FuncConnectionFactory {

	public FuncConnectionFactoryMysql() {

	}

	public String getUrl(String ip, String sid, String port) {
		return "jdbc:mysql://" + getDefaultIp(ip) + (StrFunc.isNull(port) ? "" : ":" + port) + "/" + sid
				+ "?useUnicode=true&characterEncoding=utf8";
	}

	public String getName() {
		return DB_MYSQL;
	}

	protected String[] getCustom() {
		return CUSTOMJDBC[1];
	}

	public String getDriver() {
		return DRIVER_MYSQL;
	}
}
