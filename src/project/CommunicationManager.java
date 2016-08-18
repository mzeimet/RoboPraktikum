package project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.NXTConnection;

public class CommunicationManager {

	public LinkedList<Integer> start(LinkedList<Integer> memory) {

		// use this as long as you don't want to test the Bluetooth connection
		// return testData(memory);

		// this is for the dumbRobo
		try {
			return waitForRoboConnection(memory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return memory;
		}

		// this is for the intelligent Robo
		// findRobo(memory); return null;

	}

	private LinkedList<Integer> testData(LinkedList<Integer> memory) {

		memory.addFirst(1);
		memory.addFirst(2);
		memory.addFirst(0);
		memory.addFirst(0);
		memory.addFirst(2);
		memory.addFirst(1);
		return memory;
	}

	private LinkedList<Integer> waitForRoboConnection(LinkedList<Integer> memory) throws IOException {
		System.out.println("Start waiting for Connection");
		NXTConnection connection = null;

		while (connection == null)
			connection = Bluetooth.getNXTCommConnector().waitForConnection(100000, NXTConnection.PACKET);
		DataInputStream dataIn = connection.openDataInputStream();
		System.out.println("Get Packages");

		// Sender muss als letztes Packet die -1, für den Verbindungsabbruch
		// Senden
		while (memory.getFirst() != -1) {
			memory.addFirst(dataIn.readInt());
		}

		dataIn.close();

		return memory;

	}

	private void findRobo(LinkedList<Integer> memory) throws IOException {
		System.out.println("Start Search other Robo");
		NXTConnection connection = null;

		while (connection == null)
			connection = Bluetooth.getNXTCommConnector().connect("EV3", NXTConnection.PACKET);

		System.out.println("Start send packages");

		DataOutputStream dataOut = connection.openDataOutputStream();

		while (memory.getFirst() != null) {
			dataOut.write(memory.getFirst());
			memory.removeFirst();
		}
		dataOut.flush();
		dataOut.close();

	}

}
