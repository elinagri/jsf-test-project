/*
 * Util.java
 * JSF/Trinidad, HWR SS 2012
 */

package evaluationproject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;

import javax.faces.context.FacesContext;


/**
 * Diese Klasse stellt verschiedene Hilfsfunktionen zur VerfÃ¼gung
 *
 * @author Wolfgang Lang
 * @version 1.1.2, 2012-06-07
 * @see    "Foliensatz zur Vorlesung"
 */
public class Util {
  
  /**
   * Wichtig ist, dass hier der Bean-Name steht, der in der 
   * faces-config mit <managed-bean-name>xxx</managed-bean-name> definiert 
   * wurde
   */
  final static String DATABASE_BEAN_NAME = "mb_db";
  
  private MbDb mbDb = null;
  
  public Util() {}

  /**
   * Datenbankverbindung schlieÃŸen
   * @param con : Aktuelle DB-Verbindung
   */  
  public void closeConnection( Connection con ){
    if( mbDb == null ) mbDb = (MbDb) getBean( DATABASE_BEAN_NAME );
    if( mbDb != null ) mbDb.closeConnection( con );
    else System.err.println( 
              "Util.closeConnection(): Fehler beim SchlieÃŸen der Connection!" );
  }
  
  /**
  * Gibt ein Connection-Objekt aus dem Pool zurÃ¼ck
  * @return : Connection-Objekt
  */
  public Connection getCon(){
          
    return FacesContext.getCurrentInstance().getApplication().
           evaluateExpressionGet( FacesContext.getCurrentInstance(),
           "#{" + DATABASE_BEAN_NAME + ".con}", Connection.class ); 
  }
  
  /**
   * Gibt eine Referenz auf einen Managed Bean zurÃ¼ck
   * @param sBean
   * @return : Referenz auf Managed Bean
   */
  public Object getBean( String sBean ){
      
    if( sBean != null ){
      return FacesContext.getCurrentInstance().getApplication().
               evaluateExpressionGet( FacesContext.getCurrentInstance(),
               "#{" + sBean +"}", Object.class ); 
      
      /* FacesContext fc = FacesContext.getCurrentInstance();
      Application app = fc.getApplication();
      Object o = app.evaluateExpressionGet( FacesContext.getCurrentInstance(),
                                              "#{" + sBean +"}", Object.class );
      return o; */      
    }
    else return null;
  }
  
  /**
   * VerschlÃ¼sselung eines Passworts
   * Aus Kennung und Passwort wird mittels SHA-Hash das verschlÃ¼sselete Passwort
   * generiert. 
   * @param user Kennung
   * @param pw   Passwort im Klartext
   * @return out VerschlÃ¼sseltes Passwort oder unverschlÃ¼sselte 
   *             Input-Parameter bei Fehler
   */
  public String cryptpw( String user, String pw ) {
    
    String in = user + pw;
    String out = in;
    
    try{
      MessageDigest md = MessageDigest.getInstance( "SHA" );
      byte[] bHash = md.digest( in.getBytes() ); // oder getBytes( â€UTF-8â€³ ) ?
      StringBuffer sb = new StringBuffer();
      
      for( int i = 0; i < bHash.length; i++ ){
        sb.append( Integer.toHexString( 0xF0 & bHash[i] ).charAt(0) );
        sb.append( Integer.toHexString( 0x0F & bHash[i] ) );
      }
      out = sb.toString();
    }
    catch( NoSuchAlgorithmException ex ){
      ex.printStackTrace();   
    }
    
    return out;
  }
  
}

