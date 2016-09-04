package project;

import static project.Direction.FORWARD;
import static project.Direction.LEFT;
import static project.Direction.RIGHT;

import lejos.hardware.port.Port;
import project.motors.MiniMotor;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltaschallSensor;

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
	private static final float GRENZWERT_ABWEICHUNG_IR = 1f;
	private static final int INTERVALL_GROESSE_IR_MESSUNG = 20;

	private static final float FAHRE_GERADE_DISTANZ = 5f;
	private static final int MAGISCHE_TOLERANZ_KONSTANTE = 1;
	private static final double KONSTANTE_RAD_UMFANG = 5.6f * 3.1415926;

	private float letzterAbstand;

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

	public void drehe(int grad) {
		motor.drehenAufDerStelle(grad);
	}

	public void findeWand() {
		try {
			// sucheRichtungWand();
			fahreZuWand();
			dreheZuWand();
			letzterAbstand = messeAbstand();
			while (!zielGefunden) {
				folgeWand();
				if (!checkeHindernisInfrarot(LEFT)) {
					// links frei
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(3);
					steheStill();
					drehe(LEFT);
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(3);
					steheStill();
				} else { // links hinderniss
					if (checkeHindernisInfrarot(RIGHT)) {
						// sackgasse
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
		int gradBeiMin = 0;
		minimotor.dreheZurueck();
		float min = Float.MAX_VALUE;
		for (int aktGradZahl = -minimotor.getMaxGradzahl(); aktGradZahl <= minimotor
				.getMaxGradzahl(); aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG / 2) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensor.messeAbstand();
			if (abstand < min) {
				min = abstand;
				gradBeiMin = aktGradZahl;
			}
		}
		minimotor.dreheZurueck();
		motor.drehenAufDerStelle(gradBeiMin);
		motor.drehe(RIGHT);
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

	public void fahreEinFeld() {
		korregiereAbstand();

		motor.setGeschwindigkeit(30);
		motor.fahreGerade(1);
		steheStill();
	}

	/**
	 * Misst den Abstand des IR-Sensors zum nächstmöglichen Objekt
	 * 
	 * @return
	 * @throws Exception:
	 *             Geringster Abstand wurde an einer messgrenze (+-
	 *             miniMotor.MAX_GRADZAHL) gemessen, d.h. korrektheit des wertes
	 *             ist nicht mehr garantiert
	 */
	public float messeAbstand() throws Exception {
		float min = Float.MAX_VALUE;
		boolean groessterWinkel = true;
		for (int aktGradZahl = -minimotor.getMaxGradzahl(); aktGradZahl <= minimotor
				.getMaxGradzahl(); aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensor.messeAbstand();
			if (abstand < min) {
				min = abstand;
				if (Math.abs(aktGradZahl) < minimotor.getMaxGradzahl()) {
					groessterWinkel = false;
				}
			}
		}
		minimotor.dreheZurueck();
		if (groessterWinkel) {
			throw new Exception();
		}
		return min;
	}

	/**
	 * berechnet den Winkel in dem der Roboter relativ zum ausgangswinkel zur
	 * wand steht, abhägig vom ursprünglichen Abstand und Winkel
	 * 
	 * @param aktuellerAbstand
	 * @return
	 */
	private float berechneWinkel(float differenz) {
		float hypothenuse = FAHRE_GERADE_DISTANZ;
		return (float) Math.toDegrees(Math.asin(differenz / hypothenuse));
	}

	private void korregiereAbstand() {
		float aktuellerAbstand = 0;
		try {
			aktuellerAbstand = messeAbstand();
		} catch (Exception e) {
			motor.drehenAufDerStelle(-20);
			korregiereAbstand();
			return;
		}

		if (letzterAbstand < aktuellerAbstand + MAGISCHE_TOLERANZ_KONSTANTE
				&& letzterAbstand > aktuellerAbstand - MAGISCHE_TOLERANZ_KONSTANTE)
			return;

		float differenz = letzterAbstand - aktuellerAbstand;
		float winkel = berechneWinkel(differenz);

		motor.drehenAufDerStelle((int) winkel);
		motor.drehenAufDerStelle(-90);
		motor.fahreGerade((double) -differenz / KONSTANTE_RAD_UMFANG);
		motor.drehenAufDerStelle(90);

	}

	private void fahreZuWand() {
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
		boolean hasHindernis = infrarotSensor.checktHinderniss();
		minimotor.drehe(FORWARD);
		return hasHindernis;
	}

	public boolean checkeHindernisUltraschall() {
		float abstand = ultraschallSensor.getAbstandInCm();
		return abstand < GRENZWERT_ABSTAND_WAND_FAHREN;
	}

	public void drehenAufDerStelle() {

		motor.drehenAufDerStelle(-90);
	}

	public float messeInfrarot() {
		return infrarotSensor.messeAbstand();
	}

}
