/*
 * Hey Dan, wassup. Your work for the first milestone is with the client.
 * Start by looking at line 66
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class MadLibsHandler implements Runnable {
	 DataOutputStream output;
	 DataInputStream input;
	 Socket remote_socket;
	 String connected_user_name;
	 HashMap<String, String> users;
	 long id;
	 Thread server;
	 IOException disconnectException;
	 
	/**
	 * Constructor
	 *
	 * @param remote_socket
	 * @param users 
	 * @param main_thread 
	 */
	public MadLibsHandler(Socket remote_socket, HashMap<String, String> users, Thread t) {
		this.remote_socket = remote_socket;
		this.users = users;
		this.server = t;
		this.disconnectException = null;
		try {
			// Get input/output streams
			this.input = new DataInputStream(remote_socket.getInputStream());
			this.output = new DataOutputStream(remote_socket.getOutputStream());

		} catch (IOException e) {
			System.out.println("Exception: " + e.getClass().toString());
			System.out.println("\t" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		id = Thread.currentThread().getId();
		
		// Check for prior login
		connected_user_name = users.get(remote_socket.getInetAddress().toString());
		if (connected_user_name != null) {
			sendString("MadLibsServer: Welcome back, "+connected_user_name+"!\n");
		} else {
			sendString("");
			// Logging user connection
			connected_user_name = receiveString();
			if (Thread.interrupted()) {
				disconnect(disconnectException);
				System.out.printf("Handler%d: Exiting thread\n", id);
				server.interrupt();
				return;
			}
			if ( connected_user_name.equals("") )
				connected_user_name = "[Unknown User]";
				users.put(remote_socket.getInetAddress().toString(), connected_user_name);
		}
		
		System.out.printf("Handler%d: %s joined (ip %s)\n",
				id,
				connected_user_name,
				remote_socket.getInetAddress().toString() );

		int mode = 0;
		int interrupt = 0;
		do {
			mode = chooseMode();
			try {
				switch (mode) {
				case (1):
					interrupt = beginPlayMode();
					break;
				case (2):
					interrupt = beginCreateMode();
					break;
				case (3):
					interrupt = beginReadMode();
					break;
				default:
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while ( (mode > 0) && (interrupt != -1) );

		disconnect(disconnectException);
		System.out.printf("Handler%d: Exiting thread\n", id);
		server.interrupt();
		return;
	}

	private int chooseMode () {
		// Declare variables used to store client communications
		Integer client_input = 1;
		if (Thread.interrupted()) return -1;

		while (!Thread.interrupted() /*(client_input >= 0) &&  (client_input < 4)*/ ) {
			// Write the game mode options to the remote socket
			sendString(
					  "+-------------------+\n"
					+ "|    Choose Mode    |\n"
					+ "|   1) Play         |\n"
					+ "|   2) Create       |\n"
					+ "|   3) Read         |\n"
					+ "|   0) Disconnect   |\n"
					+ "+-------------------+\n"
					+ "> (int) ");

			// Read int from client
			client_input = receiveInt();
			if (Thread.interrupted()) return -1;

			// Depending on the int read, enter app mode accordingly
			switch (client_input) {
			case (1):
				sendInt(0);
				sendString("MadLibsServer: Starting \"Play\" mode.\n");
				return client_input;
			case (2):
				sendInt(0);
				sendString("MadLibsServer: Starting \"Create\" mode.\n");
				return client_input;
			case (3):
				sendInt(0);
				sendString("MadLibsServer: Starting \"Read\" mode.\n");
				return client_input;
			case (0):
				sendInt(0);
				return client_input;
			default:
				sendInt(1);
				sendString("MadLibsServer: Integer is out-of-bounds.\n");
			}
		}
		return -1;
	}

	/**
	 * Begins running "play" mode
	 */
	private int beginPlayMode() {
		if (Thread.interrupted()) return -1;
		Integer number;
		String filledBlank;
		MadLib choice = null;
		String blank = null;
		sendString(MadLibSet.getMadLibList());
		do {
			sendString("MadLibsServer: Pick a MadLib to play (or just return to exit):\n > (int) ");	
			number = receiveInt();
			if (Thread.interrupted()) return -1;
			if ( number.equals(Integer.MIN_VALUE) )
				break;
			choice = MadLibSet.getPlayable(number);
			if ( choice != null ) {
				sendInt(choice.getNumBlanks());
				sendString("MadLibsServer: Now playing \""+choice.getTitle()+"\"\n");
			} else {
				sendInt(0);
				sendString("MadLibsServer: "+number+" is out-of-bounds.\n");
				continue;
			}
			sendString("MadLib: Fill in the blanks:\n");
			while ( (blank=choice.getNextBlank()) != null ) {
				sendString(" > ("+blank+") ");
				filledBlank = receiveString();
				if (Thread.interrupted()) return -1;
				choice.fillNextBlank(filledBlank);
			}
			sendString("MadLibsServer: Your finished MadLib reads...\n\""+choice.getFilledMadlib()+"\"\n");
			MadLibSet.addCompleted(new CompletedMadLib(choice, connected_user_name));
			MadLibSet.saveCompleted();
			receiveInt();
			if (Thread.interrupted()) return -1;
			
		} while ( !(number == Integer.MIN_VALUE) );
		
		sendString("MadLibsServer: Exiting mode...\n");
		return 0;
	}

	/**
	 * Begins running "create" mode
	 */
	private int beginCreateMode() {
		// sendString("Use %word% to show which words are the madlibs");
		String ent = "";
		String key = "";
		boolean key_in_use = false;
		do {
			//System.out.println("Server: Top of loop");
			sendString("MadLibsServer: Enter a new MadLib below (or just return to exit):\n > (String) ");
			ent = receiveString();
			if (Thread.interrupted()) return -1;
			if ( !(ent.equals("")) ) {
				try{
					MadLib new_m_l = new MadLib(ent);
					sendInt(0);
					sendString("MadLibsServer: MadLib title?\n > (String) ");
					key = receiveString();
					if (Thread.interrupted()) return -1;
					if ( !(key.equals("")) ) {
						new_m_l.setTitle(key);
						key_in_use = !MadLibSet.add(new_m_l);
					}
					if (key_in_use) {
						sendString("MadLibsServer: Sorry, that name is already used! Upload cancelled\n");
					} else {
						MadLibSet.saveMadLibs();
						sendString("MadLibsServer: Your new MadLib has been uploaded!\n");
					}
				} catch (BadMadLibDataException ex) {
					sendInt(1);
					sendString("MadLibsServer: MadLib formatting error!\n"
							 + "               eg. \"The %animal% jumped over the %noun%.\"\n");
				}
			}
		} while ( !(ent.equals("")) );
		MadLibSet.saveMadLibs();
		sendString("MadLibsServer: Exiting mode...\n");
		return 0;
	}

	/**
	 * Begins running "read" mode
	 */
	private int beginReadMode() {
		Integer number;
		String choice = null;
		sendString(MadLibSet.getCompletedList());
		do {
			sendString("MadLibsServer: Pick a MadLib to read (or just return to exit):\n > (int) ");
			number = receiveInt();
			if (Thread.interrupted()) return -1;
			if ( number == Integer.MIN_VALUE )
				break;
			choice = MadLibSet.getReadable(number);
			if ( choice != null ) {
				sendInt(0);
				sendString("MadLibsServer: The MadLib reads...\n\""+choice+"\"\n");
			} else {
				sendInt(1);
				sendString("MadLibsServer: "+number+" is out-of-bounds.\n");
				continue;
			}
		} while ( !(number == Integer.MIN_VALUE) );
		
		sendString("MadLibsServer: Exiting mode...\n");
		return 0;
	}

	/**
	 * Disconnects the client from the server, and cleans up
	 */
	private void disconnect(Exception e) {
		// Get message (disconnect) from server and print to screen
		if (connected_user_name == null)
			connected_user_name = "[Unknown User]";
		if (e == null) {
			sendString("MadLibsServer: Disconnecting...\n");
			System.out.printf("Handler%d: %s disconnected gracefully\n", id, connected_user_name);
		} else {
			System.out.printf("Handler%d: %s lost connection\n", id, connected_user_name);
		}
		try {
			input.close();
			output.close();
			remote_socket.close();
		} catch (IOException ioe) {
			System.out.println("MadLibsClient: Client did not disconnect gracefully");
		}
		return;
	}

	/**
	 * Writes a string to the connected client socket
	 * @param s:String - string to be sent
	 * @return 	0 if successful
	 * 			1 if unsuccessful
	 */
	public int sendString(String s) {
		try {
			output.writeUTF(s);
			return 0;
		} catch (IOException e) {
			Thread.currentThread().interrupt();
			disconnectException = e;
			return 1;
		}
	}

	/**
	 * Writes an int to the connected client socket
	 * @param i:int - int to be sent
	 * @return 	0 if successful
	 * 			1 if unsuccessful
	 */
	public int sendInt(int i) {
		try {
			output.writeInt(i);
			return 0;
		} catch (IOException e) {
			Thread.currentThread().interrupt();
			disconnectException = e;
			return 1;
		}
	}

	/**
	 * Reads an int from the connected client socket
	 * @return 	received int if successful
	 * 			null if unsuccessful
	 */
	public Integer receiveInt() {
		try {
			Integer i = input.readInt();
			return i;
		} catch (IOException e) {
			Thread.currentThread().interrupt();
			disconnectException = e;
			return null;
		}
	}

	/**
	 * Reads a String from the connected client socket
	 * @return 	received String if successful
	 * 			null if unsuccessful
	 */
	public String receiveString() {
		try {
			String s = input.readUTF();
			return s;
		} catch (IOException e) {
			Thread.currentThread().interrupt();
			disconnectException = e;
			return null;
		}
	}

	public void main(String[] args) {
		// Usage
		System.out.println("This class is meant to be used by a MadLibsServer object");
		return;
	}
}
