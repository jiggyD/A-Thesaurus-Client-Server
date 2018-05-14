/*
	References :
	1)Multi client server chat: https://github.com/Likitha-Seeram/Multi-Client-Server-Chat-System
	2)A simple Chat program with client/server: http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optionclients/
	3)synonym.txt: https://github.com/martiniturbide/os2-cse5306-projects/blob/master/os2-cse5306-projects/lab1/SynonymsFile.txt¬¬¬
	4)online dictionary: https://stackoverflow.com/questions/23759687/client-server-online-dictionary-program-in-java
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


//Client GUI
public class ClientGUI extends JFrame implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	private JLabel label,labelp; 					 	//To hold 'Enter UserName' and 'search' labels
	private JTextField tf;  					//To hold UserName and search values
	private JTextField tfServer, tfPort, tfuser; //Text fields to hold server address, port and connection request user name
	private JButton  send, connect, disconnect; //Buttons
	private JTextArea ta;  						//log area
	private Client client;  					//Client object
	private int defaultPort;  
	private String defaultHost; 				//To hold default server address

	String username;  //To hold client's username
	
	//constructor
	ClientGUI(String host, int port) 
	{
		super("Client");
		defaultPort = port;
		defaultHost = host;
		
		//North panel containing server address, port, connection client, label and textfield to enter user name and message
		JPanel northPanel = new JPanel(new GridLayout(2, 2));
		tfPort = new JTextField("" + port);
		labelp= new JLabel("Port Number:  ",SwingConstants.CENTER);
		northPanel.add(	labelp);
		northPanel.add(tfPort);
		label = new JLabel("Enter your username:", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);
	
		//response Panel
		ta = new JTextArea("Online synonyms:\n", 50,50);
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);
	
		//South Panel send, connect, disconnect buttons
		connect = new JButton("Connect");
		connect.addActionListener(this);
		connect.setEnabled(true);
		send = new JButton("Search");
		send.addActionListener(this);
		send.setEnabled(false);
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(this);
		disconnect.setEnabled(false);
	
		//south panel contains connect/send/disconnect buttons
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	
		JPanel actionBar2 = new JPanel(new GridLayout(1, 3));
		actionBar2.add(connect);
		actionBar2.add(send);
		actionBar2.add(disconnect);
		southPanel.add(actionBar2);

		add(southPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 400);
		setVisible(true);
		tf.requestFocus();
		
	}
	
	//this method append messages to log
	void append(String str) 
	{
		ta.append(str);  //appending messages to text area
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	
	//this method resets GUI when client disconnects
	void connectionFailed() 
	{	
		label.setText("Enter your username :");
		tf.setText("Anonymous");
		tf.setEditable(true);
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		tfServer.setEditable(true);
		tfPort.setEditable(true);
		tfuser.setEditable(false);
		connect.setEnabled(false);
		send.setEnabled(false);
		disconnect.setEnabled(false);
		tf.removeActionListener(this);
	}
	
	//this method captures action performed on client GUI 
	public void actionPerformed(ActionEvent e)
	{
		String type;
		Object o = e.getSource();
		
		if (o == connect)
		{
			username = tf.getText().trim();
			if (username.length() == 0)
				return;
			
			else
			{
				String portNumber = tfPort.getText().trim();
			if (portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			} catch (Exception en) {
				return;
			}
				append("Connecting with the server \n");  //appending messages to text area
				client = new Client("localhost", port, username, this);
				//client.sendMessage(username, "", "connect");
				if (!client.start())
					return;
				tf.setText("");
				tf.setEditable(true);
				connect.setEnabled(false);
				send.setEnabled(true);
				disconnect.setEnabled(true);
				
				label.setText("Enter search word :");
				tf.addActionListener(this);
				append("with UserName:" + username + "\n \n");
			}
		}//connect
		
		//when the event is to send message
		if (o == send)
		{
			if (tf.getText().isEmpty()) 
			{
				return;
			} 
			else 
			{
				append(" Search word :" + tf.getText() + "\n");
				client.sendMessage(username, tf.getText(), "search");
				tf.setText("");
				disconnect.setEnabled(true);
				return;
			}
		}//search
		
		if (o == disconnect) 
		{
			client.sendMessage(username, "", "disconnect");
			append("Disconnecting client " + username + "\n");  //appending messages to text area
			tf.setEditable(false);
			connect.setEnabled(true);
			send.setEnabled(false);
			disconnect.setEnabled(false);
			return;
			
		}
	}
	
	//this method is used to make connection with another client
	public void makeConnection(String user, String log) 
	{
		if (tfuser.getText().isEmpty()) {
			tfuser.setText(user);
			tf.setEditable(true);
			tfuser.setEditable(false);
			connect.setEnabled(false);
			send.setEnabled(true);
			disconnect.setEnabled(true);
			append(log +  "\n");
		} else {
			client.sendMessage(user, "", "busy");
		}
	
	}
	
	//Client GUI is initiated by by providing server address and port number
	public static void main(String[] args)
	{
		new ClientGUI("localhost", 1234);
	}
	
	//response from the server
	public void serverResponse(String user, String msg) {
		if (tfuser.getText().isEmpty()) {
			tfuser.setText(user);
			tf.setEditable(true);
			tfuser.setEditable(false);
			connect.setEnabled(false);
			send.setEnabled(true);
			disconnect.setEnabled(true);
			append(msg +  "\n");
		} else {
			client.sendMessage(user, "", "busy");
		}
	}
	
	//This method is used for disconnecting a client from its connection
	public void removeConnection(String from, String log) 
	{
		if (tfuser.getText().equals(from) || from.equals("server")) {
			tfuser.setEditable(true);
			tfuser.setText("");
			tf.setEditable(false);
			send.setEnabled(false);
			connect.setEnabled(true);
			disconnect.setEnabled(false);
			append(log);
		}
	}	

	
}