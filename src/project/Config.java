package project;

import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

public final class Config { 
	public static final int TOP_SPEED = 740;
	public static final double RADABSTAND = 12.2;
	public static final double RADDURCHMESSER = 6;
	public static final double BODENFAKTOR = 1; // je schlechter der Boden,
												// desto hoeher der Faktor
	public static final float SCHWELLWERT = 99.5f;
	public static final int GRAD_FUER_DREHUNG = 380;

	public static final String AMBIENT_MODE = "Ambient";
	public static final String LICHT_PORT = "S1";
	public static final String US_PORT = "S2";
	public static final String IR_PORT_VORNE = "S3";
	public static final String IR_PORT_HINTEN = "S4";

	public static final Port MINI_MOTOR_PORT = MotorPort.A;
	public static final Port LINKER_MOTOR_PORT = MotorPort.B;
	public static final Port RECHTER_MOTOR_PORT = MotorPort.C;

	public static final int SCHWELLWERT_STOP = 65;
	public static final int START_SPEED = 30;

	public static final int GRENZWERT_ABSTAND_WAND_SUCHEN = 50;
	public static final String FEHLER_KEINE_WAND = "404 Wand nicht gefunden :(";
	public static final float GRENZWERT_ABSTAND_WAND_FAHREN = 5;
	public static final float GRENZWERT_ABWEICHUNG_IR = 1f;
	public static final int INTERVALL_GROESSE_IR_MESSUNG = 10;

	public static final int MAGISCHE_TOLERANZ_KONSTANTE = 3;
	public static final double KONSTANTE_RAD_UMFANG = 5.6f * 3.1415926;
	public static final float ABSTAND_IR_SENSOREN = 2.5f; // TODO
	public static final int TOLERANZ_DIFF_IR = 2;
	public static final double DREHUNGEN_UM_KURVE = 1.2;
	public static final int IR_SENSOR_HINTEN = 1;
	public static final int IR_SENSOR_VORNE = 0;
	
	public static final float CM_RUECKFAHREN_IN_ECKE = -5.2f;
	
	public static final int IR_SENSOR_MAX_ABSTAND = 15;

}
