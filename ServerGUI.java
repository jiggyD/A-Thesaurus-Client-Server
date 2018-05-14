/*
	References :
	1)Multi client server chat: https://github.com/Likitha-Seeram/Multi-Client-Server-Chat-System
	2)A simple Chat program with client/server: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optionclients/
	3)synonym.txt: https://github.com/martiniturbide/os2-cse5306-projects/blob/master/os2-cse5306-projects/lab1/SynonymsFile.txt¬¬¬
	4)online dictionary: https://stackoverflow.com/questions/23759687/client-server-online-dictionary-program-in-java
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


//ServerGUI class 
public class ServerGUI extends JFrame implements ActionListener, WindowListener
{
	private static final long serialVersionUID = 1L;
	private JButton stopStart, client; 		// Stop and Start Button, Button to create client
	private JTextArea log, event; 			//log and events 
	private JTextField tPortNumber;  		//port number
	private Server server; 					//server instance
	
	//initialize the GUI
	ServerGUI(int port)
	{
		super("Server");
		server = null;
		
		//North panel for 'Start/Stop' and 'Create Client'
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		client = new JButton("Create Client");
		client.addActionListener(this);
		north.add(client);
		add(north, BorderLayout.NORTH);
		
		//Panels for log and events  text areas
		JPanel center = new JPanel(new GridLayout(1, 2));
		log = new JTextArea(100, 100);
		log.setEditable(false);
		appendRoom("Log \n");
		center.add(new JScrollPane(log));
		event = new JTextArea(100, 100);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));
		add(center);
		
		//add listener for client events
		addWindowListener(this);
		setSize(700, 500);
		setVisible(true);
	}
	
	//this method appends msg to log area
	void appendRoom(String str)			
	{
		log.append(str);  				//to append message to client log
		log.setCaretPosition(log.getText().length() - 1);
	}
	
	//this method appends msg to events area on server
	void appendEvent(String str)
	{
		event.append(str);  			//to append message to events
		event.setCaretPosition(log.getText().length() - 1);
	}
	
	//tracks button clicked on GUI and performs action accordingly
	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();  //retrieving the event object
		if (o == client)
		{
			//create new client
			if (server != null)
				new ClientGUI("localhost", 1234);
		} 
		else 
		{
			//stop server
			if (server != null) {
				server.stop();
				server = null;
				tPortNumber.setEditable(true);
				stopStart.setText("Start");
				return;
			}
			
			//to start server
			int port;
			try 
			{
				port = Integer.parseInt(tPortNumber.getText().trim());
			} catch (Exception er) {
				appendEvent("Invalid port number");
				return;
			}
			
			//creating new server
			server = new Server(port, this);
			new ServerRunning().start();
			stopStart.setText("Stop");
			tPortNumber.setEditable(false);
		}
	}
	
	
	//creating serverGUI instance
	public static void main(String[] arg) 
	{
		new ServerGUI(1234);
	}
	public void windowClosing(WindowEvent e)
	{
		if (server != null) {
			try {
				server.stop();
			} catch (Exception eClose) {
			}
			server = null;
		}
		dispose();
		System.exit(0);
	}
	
	public void windowClosed(WindowEvent e) {	}
	public void windowOpened(WindowEvent e) {	}
	public void windowIconified(WindowEvent e) {	}
	public void windowDeiconified(WindowEvent e) {	}
	public void windowActivated(WindowEvent e) {	}
	public void windowDeactivated(WindowEvent e) {	}
	
	//server thread
	class ServerRunning extends Thread 
	{
		public void run() {
			server.start();  //executes until server stops
			stopStart.setText("Start");		//if server stops
			//tPortNumber.setEditable(true);
			appendEvent("Server Stopped\n");
			server = null;
		}
	}
	
}
