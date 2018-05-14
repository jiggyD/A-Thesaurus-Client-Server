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
	Server class that handles all the server side requests coming from the client.
	Sockets are used for reading and writing HTTP format messages.
 */

public class Server {
		
	private static int uniqueId;  			//unique ID for each client
	private int port;  						//connection port
	private ServerGUI sGUI;  				//server gui instance	
	private SimpleDateFormat sdf;  			//to display time
	
	private boolean acceptCon;  			//Holds state of server and client
	private ArrayList<ClientThread> clients;     //Holds list of registered clients 
	String HttpResponse, HttpMessage; 		//HTTP messages used at input output streams

	//constructors that receives port to listen for connection
	public Server(int port) {this(port, null);}
	
	public Server(int port, ServerGUI sGUI) 
	{
		this.sGUI = sGUI;  					//initiclientsize GUI
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");  
		clients = new ArrayList<ClientThread>();  
	}
	
	

	//method to keep track of on going server session and all the clients registered to it
	//it runs untill server is stopped
	public void start() 
	{
		acceptCon = true;
		try
		{
			
			ServerSocket serverSocket = new ServerSocket(port);			//create server socket
								
			while (acceptCon)											//server waiting for connection
			{
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();
				
				if (!acceptCon)											//if server stops
					break;
				ClientThread t = new ClientThread(socket);			 	//Client thread 
				clients.add(t);  
				t.start();
			}
			try
			{
				//if server stops
				serverSocket.close();		//close server socket
				for (int i = 0; i < clients.size(); ++i) {
					ClientThread tc = clients.get(i);
					try {
						//closing all streams
						tc.in.close();
						tc.out.close();
						tc.socket.close();
					} catch (IOException ioE) {
					}
				}
			} catch (Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		} catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	//This method is executed when the server connection is stopped
	protected void stop()
	{
		acceptCon = false;
		try 
		{
			new Socket("localhost", port);
		}
		catch (Exception e) 
		{	display("Exception  in stopping the server: " + e);
		}
		
	}
	
	//This method appends msgs/exceptions/notifications in the Events log area
	private void display(String msg)
	{
		String time = sdf.format(new Date()) + " " + msg;
		if (sGUI == null)
			System.out.println(time);  //If GUI is not present, message is printed on console
		else
			sGUI.appendEvent(time + "\n");
	}
	
	
	// This method is used to to frame a HTTPResponse at server side when a client sends connection request/messages
	// also used to notify disconnection notification
	
	private synchronized void sendPrivateMessage(String user1, String message, String user2, String type)
	{
		String time = sdf.format(new Date());
		//Looping through the connected clients
		for (int i = clients.size(); --i >= 0;) {
			ClientThread ct = clients.get(i);
			boolean check = ct.username.equals(user1);
			//To check if the the client to whom the message is being sent is available
			if (check) {
				HttpResponse = "PUT HTTP/1.1 200 OK type=" + type + "&from=" + user1 + "&to=" + user1 + "&message="
						+ message + "&time=" + time + "&length=" + message.length();
				ct.writeMsg(HttpResponse);  //to write to the required client
			}
		}
	}
	
	//to remove client after it disconnects
	synchronized void remove(int id)			
	{
		for (int i = 0; i < clients.size(); ++i)
		{
			ClientThread ct = clients.get(i);
			if (ct.id == id) {
				clients.remove(i);
				return;
			}
		}
	}
	
	//creating server instance and starting it
	public static void main(String[] args) {
		int portNumber = 1234;
		
		Server server = new Server(portNumber);	//create server 
		server.start();
	}
	
	//Each client is handled by separate thread.This class creates new thread and data streams for client
	class ClientThread extends Thread 
	{
		Socket socket;  			 //client socket
		ObjectInputStream in;		//input output strems
		ObjectOutputStream out;
		int id;  					 //client id 
		String username; 			 //client username 
		String date;  			
		
		File inFile = null;			//file handler
		final String FILE_NAME = "Synonyms.txt";	//synonym file which holds test data
		
		//constructor
		ClientThread(Socket socket)
		{
			id = ++uniqueId;
			this.socket = socket;
			try 
			{
				//Creating io streams
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				
				username = (String) in.readObject();	//get client name and display it in logs
				display(username + " connected.");
			} 
			catch (IOException e )
			{
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch(ClassNotFoundException e){}
					
			date = new Date().toString() + "\n";
		}
		
		//this method runs untill a client disconnects and reacts to all msgs sent to client
		public void run() 
		{
			boolean acceptCon=true;
			while(acceptCon)
			{
				try 
				{	//read msg frm input stream
					HttpMessage = (String) in.readObject();		//read socket input
				} 
				catch (IOException e)
				{
					//This Exception occurs if the client is not available or logs off
					display(username + " Exception reading Streams: " + e);
					String time = sdf.format(new Date());
					//When a client in connection closes by clicking X button of window, notifying the server
					String msg = username + " closed \n";
					for (int i = clients.size(); --i >= 0;) {
						ClientThread ct = clients.get(i);
						HttpResponse = "PUT HTTP/1.1 200 OK type=close&from=" + username + "&to=" + ct.username
								+ "&message=" + msg + "&time=" + time + "&length=" + msg.length();
						ct.writeMsg(HttpResponse);  //writing to the client
					}
					break;
				}
				catch(ClassNotFoundException e){}
				
				//Parsing the HTTP message received
				String data = HttpMessage.substring(HttpMessage.lastIndexOf("?") + 1); //removing header of http message
				//Extracting each parameter present in http message
				String type = retrieveParameter(data, "type");
				String from = retrieveParameter(data, "from");
				String to = retrieveParameter(data, "to");
				String msg = retrieveParameter(data, "message");
				String time = retrieveParameter(data, "time");
				String len = retrieveParameter(data, "length");
				
				//Appending the HTTP message in events log
				sGUI.appendEvent(HttpMessage + "\n");
				if (!msg.isEmpty()) {
					String log = time + " " + from + ":" + msg + "\n";
					sGUI.appendRoom(log);  							//appending message to the server log
				}
				
				/*
				 * The following conditions checks for type of the message received and acts accordingly. 
				 * 'Connect'- Input: Connection request, Action: Connection is made for a set of desired clients
				 * 'Search'- Input: search word entered by client, Action: search the word in synonym.txt and reply to client
				 * 'Disconnect' -- Client disconnects from the server 
				 */
				if (type.equals("connect")) 
				{
					//If the requested client is logged in, then request is sent to the client.
					//Else request is rejected
					Boolean clientCheck = false;
					for (int i = 0; i < clients.size(); ++i)
					{
						ClientThread ct = clients.get(i);
						if (ct.username.equals(to))
						{
							clientCheck = true;
							break;
						}
					}
					if (clientCheck) 
					{
						//If request client is available, make the connection request
						msg = "Connection accepted";
						sendPrivateMessage(from, msg, to, type);  //sending messages among connected clients
					} 
					else 
					{
						//If request client is not available, reject the connection request
						msg = "Client not available \n";
						String timestamp = sdf.format(new Date());
						HttpResponse = "PUT HTTP/1.1 200 OK type=disconnect&from=server&to=" + from + "&message=" + msg
								+ "&time=" + timestamp + "&length=" + msg.length();
						writeMsg(HttpResponse);  //writing to client
					}
				}//connect
				
				
				if (type.equals("search"))
				{
					
					String temp=msg;
					msg="";
					//call method to check if search word is present in synonym file
					String result=search(temp);
					
					//if synonym found
					if(result!=null)
					{
						msg=result; // Respond with the whole line containing the synonyms
						
					}
					else
					{
						msg="No knowledge for " + temp;
					}
					
					sendPrivateMessage(from, msg, to, type);  //sending messages to connected client
				}
				
				if (type.equals("logout")) 
				{
					display(username + " disconnected with a LOGOUT message.");  //displaying in events log
					if (!to.isEmpty()) {
						sendPrivateMessage(from, "User logged off  \n", to, type); //sending messages to server
					}
					//when a client logs off, stop the loop that listens for events
					acceptCon = false;
					break;
				}
				
				
				if (type.equals("disconnect")) 
				{
					msg = "User disconnected  \n";
					sendPrivateMessage(from, msg, to, type);  //sending messages to server
				}
				
			}//while
			
			remove(id);
			close();
		}//run
		
		
		// This method is used for retrieving useful parameters from the HTTP message received
		public String retrieveParameter(String data, String field) 
		{

			String parameters[] = data.split("&");  //split msg with delimiter '&'
			for (int i = 0; i < parameters.length; i++) {
				int x = parameters[i].indexOf("=");  //split msg with delimiter '='
				if (parameters[i].substring(0, x).equals(field)) {
					return parameters[i].substring(x + 1);  
				}
			}
			return null;
		}
		
		//Closing input and output data streams
		private void close() 
		{
			try 
			{
				if (out != null)
					out.close();
			} catch (Exception e) {}
			
			try
			{
				if (in != null)
					in.close();
			} catch (Exception e) {}
			
			try 
			{
				if (socket != null)
					socket.close();
			}catch (Exception e) {}
		}
		
		//HTTP Response messages from server to client
		private boolean writeMsg(String msg)
		{
			if (!socket.isConnected())
			{
				close();
				return false;
			}
			try 
			{	//write message to the stream
				out.writeObject(msg);
			} 
			catch (IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}//writemsg
		
	
	//searches synonym in file
	String search(String temp)
	{
		String line = null;
		
		try
		{
			 inFile = new File(FILE_NAME); 							// The dictionary file
             FileInputStream fis = new FileInputStream(inFile);		//open the file 
	         BufferedReader br = new BufferedReader(new InputStreamReader(fis));  //to read contents of file
		 
			//search line by line in file
			while ((line = br.readLine()) != null)
			{
				//if found return synonyms
				if (line.contains(temp))
				     return (line);
                            
            }
			br.close();	
		}
		catch(IOException ex)
		{
				System.err.println("Exception: " + ex.getMessage());
		}
		
		return null;
	}//search
		
	}//clientthread
	
}//serverclass

