/*
 * MbDb.java
 * JSF/Trinidad, SS 2012
 */

package evaluationproject;

//import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Ãœber diese Bean erfolgt der Connect zu Datenbank. Die Verbindungspararmeter
 * werden als managed properies aus der faces-config.xml gelesen. Alternativ ist
 * auch ein JNDI lookup mÃ¶glich. Wichtig: Dem Projekt muss der verwendete
 * Datenbanktreiber hinzugefÃ¼gt werden (
 * <code>Tools | Project Properties | Libraries</code>)
 *
 * @author Wolfgang Lang
 * @version 1.1.6, 2012-06-07
 * @see "Foliensatz zur Vorlesung"
 */
public class MbDb {

	// private MysqlConnectionPoolDataSource mds = null;
	private PrintWriter pwLog = null;
	private String user = "from faces-config.xml",
			pw = "from faces-config.xml", constr = "from faces-config.xml";

	public MbDb() {
		System.out.println("Creating evaluationproject.MbDb at " + new Date());
	}

	public void setConstr(String s) {
		constr = s;
	}

	public void setUser(String s) {
		user = s;
	}

	public void setPw(String s) {
		pw = s;
	}

	public PrintWriter getLogWriter() {
		return pwLog;
	}

	/**
	 * Logfile setzen (managed property) und Logwriter Ã¶ffnen
	 * 
	 * @param s
	 *            : logfile pfad und name
	 */

	/**
	 * Ausgabe einer Meldung in das Log file
	 * 
	 * @param s
	 *            : Meldung
	 */
	public void log(String s) {
		if (pwLog != null) {
			pwLog.println(new Date().toString() + ": " + s);
			pwLog.flush();
		}
	}

	/**
	 * Verbindung zur Datenbank an den Pool zurÃ¼ck geben
	 * 
	 * @param con
	 *            : Aktuelle connection zur Datenbank
	 */
	public void closeConnection(Connection con) {
		try {
			con.close();
		} catch (SQLException ex) {
			log("SQLException!");
			while (ex != null) {
				ex.printStackTrace();
				ex = ex.getNextException();
			}
		}
	}

	public Connection getCon() {
		URI dbUri = null;
		try {
			System.out.println("DBURL: " + System.getenv("DATABASE_URL"));
			dbUri = new URI(System.getenv("DATABASE_URL"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':'
				+ dbUri.getPort() + dbUri.getPath();

		try {
			return DriverManager.getConnection(dbUrl, username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Stellt eine Connection aus dem Pool zur VerfÃ¼gung. Falls noch kein Pool
	 * vorhanden ist, wird er angelegt.
	 * 
	 * @return Connection-Objekt
	 */
	/*
	 * public Connection getCon(){
	 * 
	 * Connection con = null;
	 * 
	 * try{ if( mds == null) { // Datenpool anlegen: mds = new
	 * MysqlConnectionPoolDataSource(); mds.setURL( constr ); mds.setUser( user
	 * ); mds.setPassword( pw ); mds.setLogWriter( pwLog ); }
	 * 
	 * if( mds != null ){ con = mds.getPooledConnection().getConnection();
	 * con.setAutoCommit( true ); //pwLog = mds.getLogWriter(); } } catch(
	 * SQLException ex ){ FacesContext.getCurrentInstance().addMessage( null,
	 * new FacesMessage( FacesMessage.SEVERITY_ERROR, "SQLException",
	 * ex.getLocalizedMessage()) ); log( "SQLException!" ); while( ex != null )
	 * { ex.printStackTrace(); ex = ex.getNextException(); } }
	 * 
	 * return con; }
	 */
}
