package project;

import java.awt.Button;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.hardware.Audio;
import lejos.hardware.Bluetooth;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.LCD;
import lejos.remote.nxt.NXTConnection;

public class CommunicationManager {

	public int[] start() {
return testData();
		//return searchRobo();
		// findRobo(); return null;

	}

	private int[] testData() {
		
		return new int[]{0,1,1,666,2};
	}

	private int[] searchRobo() {
		LCD.drawString("Start", 0, 1);
		int count = 0;
		int memory[] = new int[5];

		NXTConnection connection = Bluetooth.getNXTCommConnector().waitForConnection(100000, NXTConnection.PACKET);

		LCD.clear();
		LCD.drawString("waiting for BT", 5, 2);

		DataInputStream dataIn = connection.openDataInputStream();
		LCD.drawString("waiting for BT2", 15, 3);
		try {
			
			while (count < 5) {
				memory[count] = dataIn.readInt();
				System.out.println(memory[count]);
				count++;
			}
			dataIn.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return memory;

	}

	private void findRobo() {
		LCD.drawString("A", 0, 1);
		NXTConnection connection = null;
		while (connection == null)
			connection = Bluetooth.getNXTCommConnector().connect("EV3", NXTConnection.PACKET);
		LCD.drawString("AB", 0, 2);

		DataOutputStream dataOut = connection.openDataOutputStream();
		try {
			dataOut.writeInt(6666);
			dataOut.writeInt(1);
			dataOut.writeInt(1);
			dataOut.writeInt(1);
			dataOut.writeInt(0);
			dataOut.flush();

		} catch (IOException e) {
			System.out.println(" write error " + e);
		}
		LCD.drawString("ABC", 0, 3);
	}

	private void getData() {

	}

}
