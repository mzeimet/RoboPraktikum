package project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.NXTConnection;

public class CommunicationManager {

	
	public LinkedList<Integer> start(LinkedList<Integer> memory) {

		 try {
			findRobo(memory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private LinkedList<Integer> testData(LinkedList<Integer> memory) {

		for (Integer integer : memory) {
			System.out.println(integer);
		}
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
