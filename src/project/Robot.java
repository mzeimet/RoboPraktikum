package project;

import static project.Config.*;
import static project.Direction.*;

import java.util.LinkedList;

import lejos.hardware.port.Port;
import project.motors.MiniMotor;
import project.motors.Motor;
import project.sensors.InfrarotSensor;
import project.sensors.Lichtsensor;
import project.sensors.UltraschallSensor;

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
			while (!zielGefunden) {
				folgeWand();
				if(zielGefunden) return;
				if (!checkeHindernisInfrarot()) {
					// links frei
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(DREHUNGEN_UM_KURVE);
					drehe(LEFT);
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(DREHUNGEN_UM_KURVE * 0.5);
				} else { // links hinderniss, sackgasse nicht m�glich
					rechtsDrehung();
					this.zielGefunden = getLichtInProzent() > SCHWELLWERT_STOP;
					if(zielGefunden) return;
				}
			}
			motor.hardStop();
//			machePlatz();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Macht Platz damit der dumme ihn nicht rammt
	 */
	private void machePlatz() {
//		motor.fahreGerade(1);
//		motor.setGeschwindigkeit(0);
//		motor.forward();
//		motor.stop();

	}

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
			this.zielGefunden = getLichtInProzent() > SCHWELLWERT_STOP;
			if(zielGefunden) return;
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

	private void korregiereAbstand(float differenz) {

		motor.drehenAufDerStelle(-90);
		motor.fahreGerade((double) -differenz / KONSTANTE_RAD_UMFANG);
		motor.drehenAufDerStelle(90);

	}

	private void rechtsDrehung() {
		// schaue in die Ecke
		motor.drehenAufDerStelle(-45);

		// fahre zur�ck um Abstand aufzubauen
		// Wert muss ertestet werden
		motor.fahreGerade(-5.2 / KONSTANTE_RAD_UMFANG);
		
		motor.drehenAufDerStelle(45);
		minimotor.drehe(FORWARD);
		float abstand = messeAbstand(0);
		if(abstand == 15){
			boolean stehtVorHinderniss = false;
			fahre();
			while(!stehtVorHinderniss){
				stehtVorHinderniss = checkeHindernisUltraschall();
				if (stehtVorHinderniss) {
					motor.stop();
					stehtVorHinderniss = checkeHindernisInfrarot();
					if(!stehtVorHinderniss){
						fahre();
					}
				}
			}
		}
		motor.fahreGerade((abstand-GRENZWERT_ABSTAND_WAND_FAHREN)/ KONSTANTE_RAD_UMFANG);
		minimotor.drehe(LEFT);
		// Drehe zu Wand rechts
		motor.drehenAufDerStelle(90);
		SaveMove(2);
	}

	/**
	 * Testet ob der Abstand insgesamt stimmt, und ob die differenz zwischen den
	 * ir sensoren nicht zu gro� geworden ist
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

	public void fahre() {
		motor.fahreGerade();
	}

	/**
	 * Misst den Abstand des vorderen IR-Sensors zum n�chstm�glichen Objekt
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
}
