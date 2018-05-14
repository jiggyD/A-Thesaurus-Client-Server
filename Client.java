/*
	References :
	1)Multi client server chat: https://github.com/Likitha-Seeram/Multi-Client-Server-Chat-System
	2)A simple Chat program with client/server: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optionclients/
	3)synonym.txt: https://github.com/martiniturbide/os2-cse5306-projects/blob/master/os2-cse5306-projects/lab1/SynonymsFile.txt¬¬¬
	4)online dictionary: https://stackoverflow.com/questions/23759687/client-server-online-dictionary-program-in-java
*/

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;


/*
 * Client class runs as a GUI and is used to send requests/ messages to  
 * the server. It also listens to messages/responses sent by server
 */
public class Client 
{
	
	private String server, username;  //strings to store sever address and client username
	private int port; 				  //port to connect on
	private SimpleDateFormat sdf;  
	
	private ObjectInputStream sInput;	//input output streams
	private ObjectOutputStream sOutput;
	private Socket socket;

	private ClientGUI cGUI;  
	
	//client constructor
	Client(String server, int port, String username)
	{
		this(server, port, username, null);
	}
	
	Client(String server, int port, String username, ClientGUI cGUI)
	{
		this.server = server;
		this.port = port;
		this.username = username;
		this.cGUI = cGUI;
		sdf = new SimpleDateFormat("HH:mm:ss"); // messages are displayed in client window along with time
	}
	
	/*
	 * Method for a client to connect with server. If the connection is
	 * accepted, then message is sent to the client. A listener class is started
	 * that responds to messages from server.
	 */
	public boolean start()
	{
		try 
		{
			socket = new Socket(server, port);	//creating socket
		} catch (Exception ec) 
		{
			display("Error connectiong to server:" + ec);  //to display message on client window
			return false;
		}
		
		//when client connects to server
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);  //to client window
		try 
		{
			sInput = new ObjectInputStream(socket.getInputStream());	//input output streams
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} 
		catch (IOException eIO) 
		{
			display("Exception creating new Input/output Streams: " + eIO); //to display message on client window
			return false;
		}
		new ListenFromServer().start();  //When client is logged in, creating an instance of listener class to listen to server
		try
		{
			sOutput.writeObject(username);  //writing client user name to server
		}
		catch (IOException eIO) 
		{
			display("Exception in login : " + eIO);  //to display message on client window
			disconnect();
			return false;
		}
		return true;
	}//start
	
	//To display messages or server notifications on client's log window
	private void display(String msg)
	{
		cGUI.append(msg + "\n");  //to client window
	}
	
	//send msg from client to server
	void sendMessage(String user, String message, String type)
	{
		try {
			String timestamp = sdf.format(new Date()) + "\n";
			int length = message.length();
			//client request
			String HttpRequest = "GET /com/server.java HTTP/1.1?type=" + type + "&from=" + username + "&to=" + user	+ "&message=" + message + "&time=" + timestamp + "&length=" + length;
			sOutput.writeObject(HttpRequest);  //to write to server
		} catch (IOException e) {
			display("Exception writing to server: " + e);
		}
	}
	
	/*
	 * When a client disconnects by logging off or dies (by clicking close
	 * button), need to close the data streams and then socket
	 */
	private void disconnect()
	{
		try
		{
			if (sInput != null)
				sInput.close();
		} catch (Exception e) {		}
		
		try 
		{
			if (sOutput != null)
				sOutput.close();
		} catch (Exception e) {		}
		
		try
		{
			if (socket != null)
				socket.close();
		} catch (Exception e) {		}
		
		if (cGUI != null)
			cGUI.connectionFailed();
	}
	
	//Client is created by taking the default values of server address, port and user name
	public static void main(String[] args) {
		//default values
		int portNumber = 1234;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Client client = new Client(serverAddress, portNumber, userName);  //creating an instance of client
		if (!client.start())
			return;
		client.disconnect();
	}
	
	
	
	 /* This class takes the responses/messages from server. Based on the type
	 * of message, it acts accordingly by sending messages and actions to the client GUI*/
	class ListenFromServer extends Thread
	{

		public void run() 
		{
			while (true) 
			{
				try 
				{
					
					String HttpMessage = (String) sInput.readObject();	//Reading message from server
					String data = HttpMessage.substring(HttpMessage.lastIndexOf("OK") + 3);	//Parsing the HTTP message
					
					String type = retrieveParameter(data, "type");
					//String from = retrieveParameter(data, "from");
					String from = "server ";
					String to = retrieveParameter(data, "to");
					String msg = retrieveParameter(data, "message");
					String time = retrieveParameter(data, "time");
					String length = retrieveParameter(data, "length");
					String log = time + " " + from + ":" + msg + "\n";
					
					//based on the type of message received, do the following
					if (type.equals("connect")) 
					{
						cGUI.makeConnection(from, msg); 	// To make connection to server
					}
					else if (type.equals("disconnect") || type.equals("logout") ||  type.equals("close"))
					{
						cGUI.removeConnection(from, msg);
					}
					else
							cGUI.append(log);  //to  client window
					
				} catch (IOException e )
				{
					//Exception returned when client logs off or when server crashes
					System.out.println(e);
					display("Server has close the connection: " + e);  //to display message on client window
					if (cGUI != null)
						cGUI.connectionFailed();
					break;
				} 
				catch( ClassNotFoundException e2) {}
			}
		}//ListenFromServer
		
		//This method is used to retrieve required parameters from the HTTP message received
		private String retrieveParameter(String data, String field)
		{
			String parameters[] = data.split("&");   //split msg with delimiter '&'
			for (int i = 0; i < parameters.length; i++) {
				int x = parameters[i].indexOf("=");   //split msg with delimiter '='
				if (parameters[i].substring(0, x).equals(field)) {
					return parameters[i].substring(x + 1);  //returning value of requested field
				}
			}
			return null;
		}
	}
}