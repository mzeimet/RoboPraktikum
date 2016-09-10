package project;

import static project.Config.*;
import static project.Direction.*;
import project.motors.*;
import project.sensors.*;

import java.util.LinkedList;

import lejos.hardware.port.Port;

public class Robot {

	private CommunicationManager Brain;
	private LinkedList<Integer> memory = new LinkedList<Integer>();

	private Lichtsensor lichtSensor;
	private InfrarotSensor infrarotSensorVorne;
	private InfrarotSensor infrarotSensorHinten;
	private UltraschallSensor ultraschallSensor;

	private Motor motor;
	private MiniMotor minimotor;

	private float vergleichsAbstandVorne;
	private float vergleichsAbstandHinten;
	private boolean zielGefunden = false;

	public Robot(String usPort, String irPortVorne, String irPortHinten, Port miniMotorPort, Port linkerMotorPort,
			Port rechterMotorPort, String lichtPort) {
		this.ultraschallSensor = new UltraschallSensor(usPort);
		this.minimotor = new MiniMotor(miniMotorPort);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		this.infrarotSensorVorne = new InfrarotSensor(irPortVorne);
		this.infrarotSensorHinten = new InfrarotSensor(irPortHinten);
		this.lichtSensor = new Lichtsensor(lichtPort);
		Brain = new CommunicationManager();
	}

	public void dreheInfrarotSensor(Direction richtung) {
		minimotor.drehe(richtung);
	}

	private void drehe(Direction richtung) {
		if (richtung.equals(RIGHT)) {
			SaveMove(1);
		} else {
			SaveMove(0);
		}
		motor.drehe(richtung);
	}

	public void doWhatLemmingsDo(LinkedList<Integer> memory) {
		for (int i = 0; i < memory.size(); i++) {
			int aktuellerMove = memory.get(i);
			switch (aktuellerMove) {
			case 0:// Links
				linksDrehung();
				break;
			case 1:// Rechts
				rechtsDrehungDoof();
			default:
				motor.driveTachoCount(aktuellerMove);
				break;
			}
		}
	}

	private void rechtsDrehung() {
		// schaue in die Ecke, robo steht dann im 45° Winkel zu beiden Wänden
		motor.drehenAufDerStelle(-45);

		// fahre zurück um Abstand aufzubauen
		motor.fahreGerade(CM_RUECKFAHREN_IN_ECKE / KONSTANTE_RAD_UMFANG);

		// wieder parallel zur linken Wand, 90° Winkel zur vorderen Wand
		// herstellen
		motor.drehenAufDerStelle(45);

		if (checkZielGefunden())
			return; // breche Drehung ab

		minimotor.drehe(FORWARD);
		float abstand = messeAbstand(IR_SENSOR_VORNE);
		if (abstand == IR_SENSOR_MAX_ABSTAND) {
			woIstMeineWandHin();
		} else { // fahre näher an die Wand ran
			motor.fahreGerade((abstand - GRENZWERT_ABSTAND_WAND_FAHREN) / KONSTANTE_RAD_UMFANG);
			// Drehe parallel zur ehemals rechten Wand
			motor.drehenAufDerStelle(90);
		}
		minimotor.drehe(LEFT);
		SaveMove(2);
	}

	private void rechtsDrehungDoof() {
		motor.drehenAufDerStelle(-45);
		motor.fahreGerade(CM_RUECKFAHREN_IN_ECKE / KONSTANTE_RAD_UMFANG);
		motor.drehenAufDerStelle(45);
		motor.drehenAufDerStelle(90);
		minimotor.drehe(LEFT);
	}

	public void linksDrehung() {
		motor.setGeschwindigkeit(30);
		motor.fahreGerade(DREHUNGEN_UM_KURVE);
		if (checkZielGefunden())
			return; // bricht ab weil ende signalisiert wurde
		drehe(LEFT);
		if (checkZielGefunden())
			return; // bricht ab weil ende signalisiert wurde
		motor.setGeschwindigkeit(30);
		motor.fahreGerade(DREHUNGEN_UM_KURVE * 0.5);
	}

	public float getUltraschallAbstand() {
		return ultraschallSensor.getAbstandInCm();
	}

	private void SaveMove(int dir) {
		if (dir < 0)
			throw new IllegalArgumentException();
		switch (dir) {
		case 0: // LEFT
			getMemory().addFirst(1);
			break;
		case 1: // RIGHT
			getMemory().addFirst(2);
			break;
		default: // FORWARD
			getMemory().addFirst(dir);
			break;
		}

	}

	public LinkedList<Integer> getMemory() {
		return memory;
	}

	public CommunicationManager getBrain() {
		return Brain;
	}

	public void findeWand() {
		try {
			// fahreZuWand();
			// dreheZuWand();
			while (!checkZielGefunden()) {
				folgeWand();
				if (zielGefunden)
					break;
				else if (!checkeHindernisInfrarot()) {
					// links frei
					linksDrehung();
				} else { // links hinderniss, sackgasse nicht möglich
					rechtsDrehung();
				}
			}
			motor.hardStop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dreht den Roboter parallel zur Wand (nach rechts). Wird aufgerufen wenn
	 * der Roboter mit der Front zur Wand steht.
	 */
	private void dreheZuWand() {
		int gradBeiMin = 0;
		float min = Float.MAX_VALUE;
		for (int aktGradZahl = -minimotor.getMaxGradzahl(); aktGradZahl <= minimotor
				.getMaxGradzahl(); aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensorVorne.messeAbstand();
			if (abstand < min) {
				min = abstand;
				gradBeiMin = aktGradZahl;
			}
		}
		minimotor.dreheZurueck();
		motor.drehenAufDerStelle(gradBeiMin);
		drehe(RIGHT);
		minimotor.drehe(LEFT);
	}

	/**
	 * Folgt der linken Wand bis diese nicht mehr da ist oder er vor einem
	 * Hindernis steht
	 */
	public void folgeWand() {
		int tachoCount = motor.getTachoCount();
		boolean linksKeineWand = !checkeHindernisInfrarot();
		boolean stehtVorHinderniss = checkeHindernisUltraschall();
		if (stehtVorHinderniss) {
			motor.stop();
			stehtVorHinderniss = pruefeUltraschallMitInfrarot();
		}
		boolean darfFahren = !linksKeineWand && !stehtVorHinderniss;
		if (darfFahren) {
			vergleichsAbstandVorne = messeAbstand(IR_SENSOR_VORNE);
			vergleichsAbstandHinten = messeAbstand(IR_SENSOR_HINTEN);
			fahre();
		}
		while (darfFahren) {
			if (checkZielGefunden())
				return;
			linksKeineWand = !checkeHindernisInfrarot();
			stehtVorHinderniss = checkeHindernisUltraschall();
			darfFahren = !linksKeineWand && !stehtVorHinderniss;
			if (stehtVorHinderniss) {
				motor.stop();
				stehtVorHinderniss = pruefeUltraschallMitInfrarot();
				if (!stehtVorHinderniss)
					fahre();
			}
			neueKorrektur();
		}
		motor.stop();
		tachoCount = motor.getTachoCount() - tachoCount; // TODO
		SaveMove(tachoCount);
	}

	private void neueKorrektur() {
		float abstandVorne = messeAbstand(IR_SENSOR_VORNE);
		float abstandHinten = messeAbstand(IR_SENSOR_HINTEN);
		float diffVorne = abstandVorne - vergleichsAbstandVorne;
		float diffHinten = abstandHinten - vergleichsAbstandHinten;

		motor.setGeschwindigkeitSpezifisch(START_SPEED - diffVorne, Direction.RIGHT);
		motor.setGeschwindigkeitSpezifisch(START_SPEED + diffVorne, Direction.LEFT);

		motor.forward();
	}

	/**
	 * Roboter hat in der rechsDrehung seine Wand verloren
	 */
	private void woIstMeineWandHin() {
		// findet die Wand links nicht mehr
		boolean stehtVorHinderniss = false;
		fahre();
		while (!stehtVorHinderniss) {
			if (checkZielGefunden())
				return; // breche Drehung ab
			stehtVorHinderniss = checkeHindernisUltraschall();
			if (stehtVorHinderniss) {
				motor.stop();
				stehtVorHinderniss = checkeHindernisInfrarot();
				if (!stehtVorHinderniss) {
					fahre();
				}
			}
		}

	}

	/**
	 * Lässt den Roboter geradeaus fahren
	 */
	public void fahre() {
		motor.fahreGerade();
	}

	/**
	 * Misst den Abstand per Infrarot Sensor
	 * 
	 * @param i:
	 *            IR_SENSOR_VORNE oder IR_SENSOR_HINTEN
	 */
	private float messeAbstand(int i) {
		if (i == IR_SENSOR_VORNE) {
			// vorne
			return infrarotSensorVorne.messeAbstand();
		} else if (i == IR_SENSOR_HINTEN) {
			// hinten
			return infrarotSensorHinten.messeAbstand();
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Fährt gerade aus bis zur nächsten Wand und bleibt dann stehen
	 */
	private void fahreZuWand() {
		boolean nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		int tachoCountVorher = motor.getTachoCount();
		motor.fahreGerade();
		while (nichtErreicht) {
			nichtErreicht = getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN;
		}
		motor.stop();
		SaveMove(motor.getTachoCount() - tachoCountVorher);
	}

	public boolean checkeHindernisInfrarot() {
		boolean hasHindernis = infrarotSensorVorne.checktHinderniss();
		return hasHindernis;
	}

	public boolean checkeHindernisUltraschall() {
		float abstand = ultraschallSensor.getAbstandInCm();
		return abstand < GRENZWERT_ABSTAND_WAND_FAHREN;
	}

	/**
	 * Wird benutzt um zu testen ob der Ultraschall-Messwert richtig war
	 */
	private boolean pruefeUltraschallMitInfrarot() {
		minimotor.setAusrichtung(LEFT);
		minimotor.drehe(FORWARD);
		boolean ergebnis = checkeHindernisInfrarot();
		minimotor.drehe(LEFT);
		return ergebnis;
	}

	/**
	 *
	 * @return Helligkeit in Prozent, 0-100
	 */
	public int getLichtInProzent() {
		int wert = new Double(lichtSensor.getWert() * 100.0).intValue();
		return wert;
	}

	/**
	 * Testet ob per Licht ein Ende-Signal erzeugt wurde
	 */
	public boolean checkZielGefunden() {
		this.zielGefunden = getLichtInProzent() > SCHWELLWERT_STOP;
		return zielGefunden;
	}

	/*
	 * ##################################### NICHT VERWENDETE METHODEN ######
	 */

	/**
	 * Macht Platz damit der dumme ihn nicht rammt, wird beim solo robo nicht
	 * verwendet
	 */
	private void machePlatz() {
		motor.fahreGerade(1);
		motor.setGeschwindigkeit(0);
		motor.forward();
		motor.stop();
	}

	/**
	 * Testet ob der Abstand insgesamt stimmt, und ob die differenz zwischen den
	 * ir sensoren nicht zu groß geworden ist
	 * 
	 * @return true falls noch alles stimmt
	 */
	private boolean pruefeUndKorrigiere() {
		float abstandVorne = messeAbstand(IR_SENSOR_VORNE);
		float abstandHinten = messeAbstand(IR_SENSOR_HINTEN);
		float diff = (abstandVorne - ABSTAND_IR_SENSOREN) - abstandHinten;

		if (vergleichsAbstandVorne + MAGISCHE_TOLERANZ_KONSTANTE < abstandVorne
				|| vergleichsAbstandVorne - MAGISCHE_TOLERANZ_KONSTANTE > abstandVorne) {
			korregiereAbstand(diff);
			return true;
		}

		if (diff > TOLERANZ_DIFF_IR) {
			motor.setGeschwindigkeitSpezifisch(START_SPEED + diff * 2, Direction.RIGHT);
			motor.setGeschwindigkeitSpezifisch(START_SPEED, Direction.LEFT);

		} else if (diff < 0 && Math.abs(diff) > TOLERANZ_DIFF_IR) {
			motor.setGeschwindigkeitSpezifisch(START_SPEED + Math.abs(diff) * 2, Direction.LEFT);
			motor.setGeschwindigkeitSpezifisch(START_SPEED, Direction.RIGHT);

		} else {
			motor.setGeschwindigkeitSpezifisch(START_SPEED, Direction.LEFT);
			motor.setGeschwindigkeitSpezifisch(START_SPEED, Direction.RIGHT);
		}
		motor.forward();
		return true;
	}

	private void korregiereAbstand(float differenz) {

		motor.drehenAufDerStelle(-90);
		motor.fahreGerade((double) -differenz / KONSTANTE_RAD_UMFANG);
		motor.drehenAufDerStelle(90);

	}

	/**
	 * Misst den Abstand des vorderen IR-Sensors zum nächstmöglichen Objekt
	 * 
	 * @return
	 * @throws Exception:
	 *             Geringster Abstand wurde an einer messgrenze (+-
	 *             miniMotor.MAX_GRADZAHL) gemessen, d.h. korrektheit des wertes
	 *             ist nicht mehr garantiert
	 */
	public float messeAbstandKreis() throws Exception {
		float min = Float.MAX_VALUE;
		boolean groessterWinkel = true;
		for (int aktGradZahl = -minimotor.getMaxGradzahl(); aktGradZahl <= minimotor
				.getMaxGradzahl(); aktGradZahl += INTERVALL_GROESSE_IR_MESSUNG) {
			minimotor.drehe(aktGradZahl);
			float abstand = infrarotSensorVorne.messeAbstand();
			if (abstand < min) {
				min = abstand;
				if (Math.abs(aktGradZahl) < minimotor.getMaxGradzahl()) {
					groessterWinkel = false;
				}
			}
		}
		minimotor.dreheZurueck();

		return min;
	}

	public float getAbstand() {
		return messeAbstand(0);
	}

	public float getIrErgebnis() {
		return infrarotSensorVorne.getWert();
	}
}
