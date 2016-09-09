package project;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedList;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.NXTConnection;

public class CommunicationManager {

	public LinkedList<Integer> start(LinkedList<Integer> memory) {

		try {
			return waitForRoboConnection(memory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return memory;
		}

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
		boolean bla = false;
		while (memory.size() < 1) {
			memory.addFirst(dataIn.readInt());
			// bla = (memory.getFirst() != -1);
		}

		dataIn.close();

		return memory;

	}

}
