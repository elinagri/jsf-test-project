package evaluationproject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

/**
 * Die Klasse EvaluationFB ist eine ManagedBean, die Methoden zum Auslesen der
 * Nutzer-Eingaben aus dem Formular fragebogen.jsf sowie deren Auswertung und
 * anschließenden Speicherung in der Datenbank enthält. Außerdem stellt die
 * Klasse Methoden zum Auslesen von bestimmten Angaben aus der Datenbank, wie
 * TAN, Modulnummer, Modulname, Dozentenname und Semester, bereit.
 * 
 * @author Elina Ovcinnikova, Emira Koussa, Dicle Aksoy
 */
public class EvaluationFB implements Serializable {

	private String tanabfrage = "select verwendet from tantabelle where tan=";

	private boolean connected = false;

	private Util util = new Util();

	private Connection con = null;
	private Statement stm = null;
	private ResultSet rs = null;

	private int modul_nr;
	private String modul_name, dozent_name, semester;
	private int radiovalue = 0;
	private String kommentar = "";
	private final int FRAGENANZAHL = 60;
	private int[] result_int_array = new int[FRAGENANZAHL];
	private String[] ergebnis_array = new String[FRAGENANZAHL];
	private boolean[] cb_boolean_array = new boolean[7];

	private int[] cbxValues = null;

	private int tan = -1;

	private String tanmeldung = "";

	private List options = new ArrayList<SelectItem>();
	private List janein = new ArrayList<SelectItem>();

	@SuppressWarnings("unchecked")
	public EvaluationFB() {

		options.add(new SelectItem(6, "6", "trifft vollkommen zu"));
		options.add(new SelectItem(5, "5"));
		options.add(new SelectItem(4, "4"));
		options.add(new SelectItem(3, "3"));
		options.add(new SelectItem(2, "2"));
		options.add(new SelectItem(1, "1 | ", "trifft überhaupt nicht zu"));
		options.add(new SelectItem(0, "weiß nicht", "Defaulteinstellung"));

		janein.add(new SelectItem(2, "ja"));
		janein.add(new SelectItem(1, "nein | "));
		janein.add(new SelectItem(0, "weiß nicht", "Defaulteinstellung"));

		for (int z = 0; z < FRAGENANZAHL; z++)
			result_int_array[z] = radiovalue;

		for (int ind = 0; ind < cb_boolean_array.length; ind++)
			cb_boolean_array[ind] = false;
	}

	/**
	 * Die Methode ermöglicht es, auf das Integer-Array cbxValues von einer
	 * jsf-Seite aus zuzugreifen.
	 * 
	 * @return Wert des Integer-Arrays cbxValues
	 */
	public int[] getCbxValues() {
		return cbxValues;
	}

	/**
	 * Beim Aufruf dieser Methode wird das Integer-Array cbxValues aktualisiert
	 * und mit den Indizes der aktuell ausgewählten Checkboxen gefüllt.
	 * 
	 * @param an
	 *            aktuell ausgewählte Checkboxen
	 */
	public void setCbxValues(int[] an) {
		cbxValues = an;
	}

	/**
	 * Die Methode überwacht die Ereignisse der Checkboxen. Falls eine Änderung
	 * der Checkboxen-Werte eintritt (d. h., eine oder mehrere Checkboxen werden
	 * aus- oder abgewählt), wird das Integer-Array cbxValues entsprechend
	 * aktualisiert.
	 * 
	 * @param vce
	 *            eingetretenes Ereignis (Wertänderung) - ValueChangeEvent
	 */
	public void vclCheckboxen(ValueChangeEvent vce) {
		cbxValues = (int[]) vce.getNewValue();
	}

	/**
	 * Die Methode ermöglicht den Zugriff auf die Variable radiovalue von der
	 * jsf-Seite aus.
	 * 
	 * @return aktueller Wert der Variable radiovalue
	 */
	public int getRadiovalue() {
		return radiovalue;
	}

	/**
	 * Beim Aufruf dieser Methode wird die Variable radiovalue mit dem aktuellen
	 * Wert der entsprechenden Radiobutton-Gruppe besetzt.
	 * 
	 * @param n
	 *            Index des aktuell ausgewählten Radiobuttons in einer Gruppe
	 *            von Radiobuttons
	 */
	public void setRadiovalue(int n) {
		radiovalue = n;
	}

	/**
	 * Die Methode ermöglicht den Zugriff auf die String-Variable kommentar von
	 * der jsf-Seite aus.
	 * 
	 * @return aktueller Wert der String-Variable kommentar
	 */
	public String getKommentar() {
		return kommentar;
	}

	/**
	 * Der String-Variable kommentar wird der Wert des Parameters k zugewiesen.
	 * Die Methode wird von dem Element inputTextArea in fragebogen.jsf
	 * aufgerufen.
	 * 
	 * @param k
	 *            Der in der inputTextArea eingegebene Text wird als String
	 *            übergeben.
	 */
	public void setKommentar(String k) {
		kommentar = k;
	}

	/**
	 * Die Methode überwacht die Wertveränderungen der Radiobuttons. Wird ein
	 * anderer Radiobutton ausgewählt, wird der neue Wert in result_int_array
	 * eingetragen bzw. aktualisiert. Die Stelle, an welcher der Wert in
	 * result_int_array eingefügt wird, entspricht dem Wert des Attributs
	 * "myAttribute" der Komponente, die das Ereignis hervorgerufen hat.
	 * 
	 * @param vce
	 *            eingetretenes ValueChangeEvent
	 */
	public void vclRadio(ValueChangeEvent vce) {
		String myAttribute = vce.getComponent().getAttributes()
				.get("myAttribute").toString();
		int i = Integer.parseInt(myAttribute);
		result_int_array[i] = Integer.parseInt((vce.getNewValue()).toString());
	}

	/**
	 * Die Methode liefert List options zurück. Mit Hilfe dieser Methode kann
	 * man von einer jsf-Seite auf options zugreifen und damit beispielsweise
	 * vereinfacht selectItems für selectOneRadio definieren.
	 * 
	 * @return List options
	 */
	public List getOptions() {
		return options;
	}

	/**
	 * Die Methode liefert List janein zurück. Mit Hilfe dieser Methode kann man
	 * von einer jsf-Seite auf janein zugreifen und damit beispielsweise
	 * vereinfacht selectItems für selectOneRadio definieren.
	 * 
	 * @return List janein
	 */
	public List getJanein() {
		return janein;
	}

	/**
	 * Die Methode wandelt den im InputTextfeld eingegebenen String in
	 * Integer-Wert um und weist diesen der Variable tan zu.
	 * 
	 * @param t
	 *            Eingabe im InputTextfeld auf der Seite taneingabe.jsf
	 */
	public void setTan(String t) {
		this.tan = Integer.parseInt(t);
	}

	/**
	 * Die Methode wandelt die int-Variable tan zu einem String um und gibt
	 * diesen zurück.
	 * 
	 * @return tan als String
	 */
	public String getTan() {
		String str = "";
		if (tan >= 0)
			str = Integer.toString(tan);
		return str;
	}

	/**
	 * Die Methode gewährleistet den Zugriff der jsf-Seiten auf die Variable
	 * tanmeldung, sodass diese ausgegeben werden kann.
	 * 
	 * @return String tanmeldung - Fehlermeldung bei der Eingabe einer falschen
	 *         TAN
	 */
	public String getTanmeldung() {
		return tanmeldung;
	}

	/**
	 * Die Methode prüft, ob die eingegeben TAN korrekt und nicht bereits
	 * verwendet worden ist.
	 * 
	 * @return String "success", wenn die TAN gültig ist; String "fail", wenn
	 *         die TAN nicht in der Datenbank vorhanden oder bereits verbraucht
	 *         ist
	 */
	public String tan_pruefen() {
		String s = "fail";
		if (util != null)
			con = util.getCon();
		if (con != null) {
			try {
				stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				String selectstatement = tanabfrage + tan;
				rs = stm.executeQuery(selectstatement);
				if (!rs.first()) {
					s = "fail";
					tanmeldung = "Ungültige TAN!";
					tan = -1;
				} else {
					connected = true;
					if (rs.getBoolean("verwendet")) {
						s = "fail";
						tan = -1;
						tanmeldung = "Diese TAN wurde bereits verwendet. Mehrfache Verwendung einer TAN ist ausgeschlossen.";
					} else
						s = "success";
				}
			} catch (Exception ex) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR,
								"SQLException", ex.getLocalizedMessage()));
				System.out.println("Error: " + ex);
				ex.printStackTrace();
			}
		}
		return s;
	}

	/**
	 * Die Methode liest die zu der eingegebenen TAN gehörende Modul-Nummer aus
	 * der Datenbank aus und gibt diese zurück.
	 * 
	 * @return int Modulnummer
	 */
	public int getModul_nr() {
		String modul_nr_abfrage = "SELECT modulnr FROM tantabelle where tan="
				+ tan;
		ResultSet res = null;
		try {
			stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			res = stm.executeQuery(modul_nr_abfrage);
			if (res.first())
				modul_nr = res.getInt("modulnr");
			res.close();
		} catch (SQLException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SQLException", e.getLocalizedMessage()));
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
		return modul_nr;
	}

	/**
	 * Die Methode liest den zu der eingegebenen TAN gehörenden Modul-Namen aus
	 * der Datenbank aus und gibt diesen zurück.
	 * 
	 * @return String Modulname
	 */
	public String getModul_name() {
		String modul_name_abfrage = "SELECT modulname FROM tantabelle where tan="
				+ tan;
		ResultSet res = null;
		try {
			res = stm.executeQuery(modul_name_abfrage);
			if (res.first())
				modul_name = res.getString("modulname");
			res.close();
		} catch (SQLException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SQLException", e.getLocalizedMessage()));
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
		return modul_name;
	}

	/**
	 * Die Methode liest den zu der eingegebenen TAN gehörenden Dozenten-Namen
	 * aus der Datenbank aus und gibt diesen zurück.
	 * 
	 * @return String Name des Dozenten
	 */
	public String getDozent_name() {
		String dozent_abfrage = "SELECT dozent FROM tantabelle where tan="
				+ tan;
		ResultSet res = null;
		try {
			res = stm.executeQuery(dozent_abfrage);
			if (res.first())
				dozent_name = res.getString("dozent");
			res.close();
		} catch (SQLException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SQLException", e.getLocalizedMessage()));
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
		return dozent_name;
	}

	/**
	 * Die Methode liest die zu der eingegebenen TAN gehörende Semester-Angabe
	 * aus der Datenbank aus und gibt diese zurück.
	 * 
	 * @return String Semester der Veranstaltung
	 */
	public String getSemester() {
		String semester_abfrage = "SELECT semester FROM tantabelle where tan="
				+ tan;
		ResultSet res = null;
		try {
			res = stm.executeQuery(semester_abfrage);
			if (res.first())
				semester = res.getString("semester");
			res.close();
		} catch (SQLException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SQLException", e.getLocalizedMessage()));
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
		return semester;
	}

	/**
	 * Die Methode geht nacheinander alle Werte in result_int_array durch,
	 * ermittelt ggf., welcher Text anstatt des Integer-Wertes in der Datenbank
	 * erscheinen soll, und speichert den entsprechenden String in
	 * ergebnis_array ab. Außerdem wird das Array cbxValues von einer Schleife
	 * durchlaufen und cb_boolean_array mit Boolean-Werten erstellt, die in die
	 * Datenbank geschrieben werden sollen.
	 */
	public void ergebnis_umwandeln() {
		if (cbxValues != null) {
			for (int index = 0; index < cbxValues.length; index++) {
				cb_boolean_array[cbxValues[index]] = true;
			}
		}
		for (int i = 1; i < FRAGENANZAHL; i++) {
			int wert = result_int_array[i];
			if ((i >= 43 && i <= 45) || i == 50 || i <= 21
					|| (i >= 54 && i <= 58) || i == 41)
				ergebnis_array[i] = Integer.toString(wert);
			if (i >= 22 && i <= 25) {
				if (wert == 0)
					ergebnis_array[i] = "weiß nicht";
				if (wert == 1)
					ergebnis_array[i] = "berücksichtigt; hilfreich";
				if (wert == 2)
					ergebnis_array[i] = "berücksichtigt; nicht hilfreich";
				if (wert == 3)
					ergebnis_array[i] = "nicht berücksichtigt; haben gefehlt";
				if (wert == 4)
					ergebnis_array[i] = "nicht berücksichtigt; haben nicht gefehlt";
			}
			if (i >= 26 && i <= 32) {
				if (wert == 0)
					ergebnis_array[i] = "weiß nicht";
				if (wert == 1)
					ergebnis_array[i] = "wurde eingesetzt; hilfreich";
				if (wert == 2)
					ergebnis_array[i] = "wurde eingesetzt; nicht hilfreich";
				if (wert == 3)
					ergebnis_array[i] = "nicht eingesetzt; hat gefehlt";
				if (wert == 4)
					ergebnis_array[i] = "nicht eingesetzt; hat nicht gefehlt";
			}
			if (i == 33) {
				if (wert == 0)
					ergebnis_array[i] = "weiß nicht";
				if (wert == 1)
					ergebnis_array[i] = "zu umfangreich";
				if (wert == 2)
					ergebnis_array[i] = "angemessen";
				if (wert == 3)
					ergebnis_array[i] = "zu gering";
			}
			if (i == 42) {
				if (wert == 0)
					ergebnis_array[i] = "nie";
				else if (wert == 1 || wert == 2 || wert == 3 || wert == 4)
					ergebnis_array[i] = wert + "x";
				else if (wert == 5)
					ergebnis_array[i] = ">4x";
				else if (wert == 6)
					ergebnis_array[i] = "weiß nicht";
			}

			if (i == 46) {
				if (wert == 0)
					ergebnis_array[i] = "weniger als 1 Std.";
				if (wert == 1)
					ergebnis_array[i] = "1-3 Std.";
				if (wert == 2)
					ergebnis_array[i] = "4-6 Std.";
				if (wert == 3)
					ergebnis_array[i] = "mehr als 6 Std.";
			}
			if (i >= 47 && i <= 49) {
				if (wert == 0)
					ergebnis_array[i] = "weiß nicht";
				if (wert == 1)
					ergebnis_array[i] = "nein";
				if (wert == 2)
					ergebnis_array[i] = "ja";
			}
			if (i == 51) {
				if (wert == 0)
					ergebnis_array[i] = "männlich";
				else if (wert == 1)
					ergebnis_array[i] = "weiblich";
			}
			if (i == 52) {
				if (wert == 0)
					ergebnis_array[i] = "Ausländer/in";
				else if (wert == 1)
					ergebnis_array[i] = "Deutsche/r";
			}
			if (i == 53) {
				if (wert == 0)
					ergebnis_array[i] = "Tagesstudent/in";
				if (wert == 1)
					ergebnis_array[i] = "Abendstudent/in";
			}
		}
	}

	/**
	 * Es wird zuerst die Methode ergebnis_umwandeln() aufgerufen, anschließend
	 * werden die Variable tan, die Daten aus ergebnis_array und
	 * cb_boolean_array sowie die String-Variable kommentar in die Tabelle
	 * evaluation_results der Datenbank eingetragen. Wenn die Daten erfolgreich
	 * in die Datenbank eingefügt werden konnten, wird ein Update von
	 * "tantabelle" durchgeführt und das Attribut "verwendet", das zu der
	 * entsprechenden TAN gehört, auf 1 (true) gesetzt.
	 * 
	 * @param actionEvent
	 *            Ereignis, das durch das Klicken des Buttons
	 *            "Fragebogen abschicken" ausgelöst wird
	 */
	public void in_db_schreiben(javax.faces.event.ActionEvent actionEvent) {
		ergebnis_umwandeln();
		try {
			String sQl = "INSERT INTO evaluation_results "
					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			PreparedStatement ps = con.prepareStatement(sQl);
			ps.setInt(1, tan);
			for (int i = 2; i <= 22; i++)
				ps.setInt(i, Integer.parseInt(ergebnis_array[i - 1]));
			for (int i = 23; i <= 34; i++)
				ps.setString(i, ergebnis_array[i - 1]);
			for (int i = 35; i <= 41; i++)
				ps.setBoolean(i, cb_boolean_array[i - 35]);
			ps.setInt(42, Integer.parseInt(ergebnis_array[41]));
			ps.setString(43, ergebnis_array[42]);
			ps.setInt(44, Integer.parseInt(ergebnis_array[43]));
			ps.setInt(45, Integer.parseInt(ergebnis_array[44]));
			ps.setInt(46, Integer.parseInt(ergebnis_array[45]));
			ps.setString(47, ergebnis_array[46]);
			ps.setString(48, ergebnis_array[47]);
			ps.setString(49, ergebnis_array[48]);
			ps.setString(50, ergebnis_array[49]);
			ps.setInt(51, Integer.parseInt(ergebnis_array[50]));
			ps.setString(52, ergebnis_array[51]);
			ps.setString(53, ergebnis_array[52]);
			ps.setString(54, ergebnis_array[53]);
			ps.setString(55, kommentar);
			for (int i = 56; i <= 60; i++)
				ps.setInt(i, Integer.parseInt(ergebnis_array[i - 2]));
			int n = ps.executeUpdate();
			if (n == 1)
				stm.executeUpdate("update tantabelle set verwendet=1 where tan="
						+ tan);
		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"SQLException", ex.getLocalizedMessage()));
			System.out.println("Error: " + ex);
			ex.printStackTrace();
		}
	}
}
