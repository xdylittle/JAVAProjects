package virtuoso;

import virtuoso.jena.driver.VirtGraph;

public class DBConnectionFactory {
	
	public static synchronized VirtGraph getGeoVirtuosoConnection(){
		String VIRT_URL = "jdbc:virtuoso://114.212.87.43:1111/";
		String VIRT_USER = "dba";
		String VIRT_PW = "dba";
		return new VirtGraph(VIRT_URL, VIRT_USER, VIRT_PW);
	}
	
}
