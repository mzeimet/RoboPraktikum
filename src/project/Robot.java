package project;

import static project.Config.ABSTAND_IR_SENSOREN;
import static project.Config.CM_UM_KURVE;
import static project.Config.GRENZWERT_ABSTAND_WAND_FAHREN;
import static project.Config.INTERVALL_GROESSE_IR_MESSUNG;
import static project.Config.IR_SENSOR_HINTEN;
import static project.Config.IR_SENSOR_VORNE;
import static project.Config.KONSTANTE_RAD_UMFANG;
import static project.Config.MAGISCHE_TOLERANZ_KONSTANTE;
import static project.Config.START_SPEED;
import static project.Config.TOLERANZ_DIFF_IR;
import static project.Direction.LEFT;
import static project.Direction.RIGHT;
import static project.Direction.FORWARD;

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
				if (!checkeHindernisInfrarot()) {
					// links frei
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(CM_UM_KURVE);
					drehe(LEFT);
					motor.setGeschwindigkeit(30);
					motor.fahreGerade(CM_UM_KURVE);
				} else { // links hinderniss, sackgasse nicht möglich
					drehe(Direction.RIGHT);
				}
			}

			machePlatz();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Macht Platz damit der dumme ihn nicht rammt
	 */
	private void machePlatz() {
		motor.fahreGerade(15);

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
			if(!stehtVorHinderniss) fahre();
		}
		boolean darfFahren = !linksKeineWand && !stehtVorHinderniss;
		if (darfFahren) {
			vergleichsAbstandVorne = messeAbstand(IR_SENSOR_VORNE);
			System.out.println(vergleichsAbstandVorne);
			vergleichsAbstandHinten = messeAbstand(IR_SENSOR_HINTEN);
			System.out.println(vergleichsAbstandHinten);
			int x = 5;
			x++;
			fahre();
		}
		while (darfFahren) {
			linksKeineWand = !checkeHindernisInfrarot();
			stehtVorHinderniss = checkeHindernisUltraschall();
			darfFahren = !linksKeineWand && !stehtVorHinderniss;
			if (stehtVorHinderniss) {
				motor.stop();
				stehtVorHinderniss = pruefeUltraschallMitInfrarot();
				if(!stehtVorHinderniss) fahre();
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

		// fahre zurück um Abstand aufzubauen
		// Wert muss ertestet werden
		motor.fahreGerade(-5 / KONSTANTE_RAD_UMFANG);

		// Drehe zu Wand rechts
		motor.drehenAufDerStelle(135);
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

	public void fahre() {
		motor.fahreGerade();
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
		System.out.println("WAND?" + ergebnis);
		return ergebnis;
		
	}
}
