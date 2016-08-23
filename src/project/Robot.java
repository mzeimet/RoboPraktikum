package project;

import lejos.hardware.port.Port;
import project.motors.MiniMotor;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltaschallSensor;
import static project.Direction.*;

public class Robot {

	private Lichtsensor lichtSensorLinks;
	private Lichtsensor lichtSensorRechts;

	private InfrarotSensor infrarotSensor;

	private UltaschallSensor ultraschallSensor;

	private Motor motor;
	private MiniMotor minimotor;

	private static final int GRENZWERT_ABSTAND_WAND_SUCHEN = 50;
	private static final String FEHLER_KEINE_WAND = "404 Wand nicht gefunden :(";
	private static final float GRENZWERT_ABSTAND_WAND_FAHREN = 5;
	private static final float FELDLAENGE = 12;

	private boolean zielGefunden = false;

	public Robot(String lichtPortLinks, String lichtPortRechts, Port linkerMotorPort, Port rechterMotorPort) {
		this.lichtSensorLinks = new Lichtsensor(lichtPortLinks);
		this.lichtSensorRechts = new Lichtsensor(lichtPortRechts);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
	}

	public Robot(String usPort, String irPort, Port miniMotorPort, Port linkerMotorPort, Port rechterMotorPort) {
		this.ultraschallSensor = new UltaschallSensor(usPort);
		this.minimotor = new MiniMotor(miniMotorPort);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		this.infrarotSensor = new InfrarotSensor(irPort);
	}

	public void dreheInfrarotSensor(Direction richtung) {
		minimotor.drehe(richtung);
	}

	public boolean checkHinderniss() {
		return infrarotSensor.checktHinderniss();
	}

	public void steheStill() {
		motor.setGeschwindigkeit(0);
	}

	public void DriveForward() {
		SaveMove(FORWARD);
		// motor.fahreGerade();
	}

	private void drehe(Direction richtung) {
		SaveMove(richtung);
		motor.drehe(richtung);
	}

	public float getUltraschallAbstand() {
		return ultraschallSensor.getAbstandInCm();
	}

	private void SaveMove(Direction right) {
		// Just save the Direction to send it later

	}
	
	public void drehe(int grad){
		motor.drehenAufDerStelle(grad);
	}

	public void findeWand() {
		try {
			sucheRichtungWand();
			fahreZuWand();
 			dreheZuWand();
			while (!zielGefunden) {
				folgeWand();
				if (!checkeHindernisInfrarot(LEFT)) {
					// links frei
					drehe(LEFT);
				} else { //links hinderniss
					if(checkeHindernisInfrarot(RIGHT)){
						//sackgasse
						drehe(RIGHT);
						drehe(RIGHT);
					} else { 
						drehe(Direction.RIGHT);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dreheZuWand() {
		drehe(RIGHT);
	}

	/**
	 * Folgt der linken Wand bis diese nicht mehr da ist oder er vor einem
	 * Hinderniss steht
	 */
	public void folgeWand() {
		boolean linksKeineWand = false;
		boolean stehtVorHinderniss = false;
		boolean darfFahren = true;
		while (darfFahren) {
			linksKeineWand = !checkeHindernisInfrarot(LEFT);
			stehtVorHinderniss = checkeHindernisUltraschall();
			if (stehtVorHinderniss) {
				stehtVorHinderniss = pruefeUltraschallMitInfrarot();
			}
			darfFahren = !linksKeineWand && !stehtVorHinderniss;
			if (darfFahren) {
				fahreEinFeld();
			}
		}
	}

	/**
	 * Prueft ob der US-sensor richtige werte liefert, noch zu impelenieren TODO
	 * 
	 * @return
	 */
	private boolean pruefeUltraschallMitInfrarot() {
		return checkeHindernisInfrarot(FORWARD);
	}

	// TODO
	private void fahreEinFeld() {
		// eine Einheit nach vorne fahren, evtl mit Korrektur?!
		motor.setGeschwindigkeit(30);
		motor.fahreGerade(1);
		steheStill();
	}

	private void fahreZuWand() {
		System.out.println("fahre zu Wand");
		motor.setGeschwindigkeit(30);
		boolean nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		while (nichtErreicht) {
			motor.fahreGerade(1);
			nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		}
		steheStill();
	}

	/**
	 * Dreht den Roboter zu einer Wand. dreht in immer kleineren Abständen.
	 * 
	 * @throws Exception
	 */
	private void sucheRichtungWand() throws Exception {
		System.out.println("SucheWand");
		int i = 2;
		int gradDrehung;
		while (i < 32) {
			for (int j = 0; j < i; j++) {
				if (getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_SUCHEN) {
					System.out.println(getUltraschallAbstand());
					gradDrehung = 360 / i;
					// rotiere um grad Grad zum neuen suchen TODO
				} else {
					return; // Gefunden!
				}
			}
			i *= 2;
		}
		throw new Exception(FEHLER_KEINE_WAND);
	}

	public boolean checkeHindernisInfrarot(Direction richtung) {
		minimotor.drehe(richtung);
		return infrarotSensor.checktHinderniss();
	}

	public boolean checkeHindernisUltraschall() {
		float abstand = ultraschallSensor.getAbstandInCm();
		return  abstand < GRENZWERT_ABSTAND_WAND_FAHREN;
	}

	// /**
	// *
	// * @return Helligkeit in Prozent, 0-100
	// */
	// public int getLichtInProzent(LeftRight lr) {
	// float sample[] = new float[1];
	// if (lr.equals(LEFT)) {
	// lichtSensorLinks.getWert();
	// } else {
	// lichtSensorRechts.getWert();
	// }
	// return new Double(sample[0] * 100.0).intValue();
	// }
	
	public void drehenAufDerStelle(){
		
		motor.drehenAufDerStelle(-90);
	}

	public float messeInfrarot() {
		return infrarotSensor.messeAbstand();
	}
}
