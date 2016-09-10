package project;

import static project.Config.IR_PORT_HINTEN;
import static project.Config.IR_PORT_VORNE;
import static project.Config.LICHT_PORT;
import static project.Config.LINKER_MOTOR_PORT;
import static project.Config.MINI_MOTOR_PORT;
import static project.Config.RECHTER_MOTOR_PORT;
import static project.Config.SCHWELLWERT_STOP;
import static project.Config.US_PORT;

import java.util.LinkedList;

import lejos.hardware.Sound;

public class Main {

	private Robot robot;

	public Main() {
		this.robot = new Robot(US_PORT, IR_PORT_VORNE, IR_PORT_HINTEN, MINI_MOTOR_PORT, LINKER_MOTOR_PORT,
				RECHTER_MOTOR_PORT, LICHT_PORT);
	}

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
//		Sound.beep();
//		System.out.println("Start fahre und speichere Strecke");
//		LinkedList<Integer> memory = fahreUndSpeicherStrecke();
//		Sound.beep();
//		System.out.println("Ende fahre und speichere Strecke, beginne fahre der gespeicherten Strecke");
//		Sound.beep();
//		//Hier dann Breakpoint um Robo zurückzustellen
//		LinkedList<Integer> revertedMemory = new LinkedList<Integer>();
//		for (Integer integer : memory) {
//			revertedMemory.addLast(integer);
//		}
		LinkedList<Integer> revertedMemory = beispielDaten();
		fahreGespeicherteStrecke(revertedMemory);
		System.out.println("Ende :)");
		Sound.beep();
		Sound.beep();
		Sound.beep();
	}

	private LinkedList<Integer> beispielDaten() {
		LinkedList<Integer> result = new LinkedList<Integer>();
		result.addLast(429);
		result.addLast(1);
		result.addLast(666);
		result.addLast(2);
		return result;
	}

	private LinkedList<Integer> fahreUndSpeicherStrecke() {
		robot.findeWand();
		return robot.getMemory();
	}
	
	private void fahreGespeicherteStrecke(LinkedList<Integer> memory){
		robot.doWhatLemmingsDo(memory);
	}
	
	
}
