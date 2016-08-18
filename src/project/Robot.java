package project;

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
	private static final float GRENZWERT_ABSTAND_WAND_FAHREN = 10;

	private boolean zielGefunden;
	
	public Robot(String lichtPortLinks, String lichtPortRechts, Port linkerMotorPort, Port rechterMotorPort) {
		this.lichtSensorLinks = new Lichtsensor(lichtPortLinks);
		this.lichtSensorRechts = new Lichtsensor(lichtPortRechts);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
	}

	public Robot(String usPortNummer, String irPortNummer, Port miniMotorPort, Port linkerMotorPort,
			Port rechterMotorPort) {
		this.ultraschallSensor = new UltaschallSensor(usPortNummer);
		this.minimotor = new MiniMotor(miniMotorPort);
		this.motor = new project.motors.Motor(linkerMotorPort, rechterMotorPort);
		this.infrarotSensor = new InfrarotSensor(irPortNummer);
	}

	public void rotate() {
		minimotor.invertiere();
	}

	public boolean checkHinderniss() {
		return infrarotSensor.checktHinderniss();
	}

	public void steheStill() {
		motor.setGeschwindigkeit(0);
	}

	public void DriveForward() {
		SaveMove(Direction.FORWARD);
		motor.fahreGerade();
	}

	private void turnLeft() {
		SaveMove(Direction.LEFT);
		motor.turnLeft();
	}

	private void turnRight() {
		SaveMove(Direction.RIGHT);
		motor.turnRight();
	}

	public float getUltraschallAbstand() {
		return ultraschallSensor.getAbstandInCm();
	}

	private void SaveMove(Direction right) {
		// Just save the Direction to send it later

	}

	public void findeWand(){
		try {
			sucheRichtungWand();
			fahreZuWand();
			steheStill();
			System.out.println(" Unn nu?");
//			dreheZuWand();
//			while(!zielGefunden){
//				folgeWand();
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void folgeWand() {
		// TODO Auto-generated method stub
		
	}

	private void dreheZuWand() {
		// TODO Auto-generated method stub
		
	}

	private void fahreZuWand() {
		System.out.println("fahre zu Wand");
		motor.setGeschwindigkeit(30);
		while(getUltraschallAbstand() > GRENZWERT_ABSTAND_WAND_FAHREN){
			System.out.println(getUltraschallAbstand());
			motor.fahreGerade();
		}
	}

	/**
	 * Dreht den Roboter zu einer Wand. dreht in immer kleineren Abständen.
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
					// rotiere um grad Grad zum neuen suchen
				} else {
					return; // Gefunden!
				}
			}
			i *=2;
		}
		throw new Exception(FEHLER_KEINE_WAND);
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
}
