/*
 * Nazwa: WoTServerStatus.java 
 * Autory: Dawid Wedman(188618), Vladyslav Mostovych(187516) (c) 2017
 * Grupa: KrDZIs3013Io
 * 
 * Opis: Program for checking @World of Tanks@ servers status
 *
 */


import com.google.gson.*;       //Google GSON deserialize library

import org.icmp4j.IcmpPingUtil;
import org.icmp4j.IcmpPingRequest;     //ICMP4J Library for latency test
import org.icmp4j.IcmpPingResponse;

import java.util.regex.Matcher;     // Java regular expressions library
import java.util.regex.Pattern;

import java.net.*;
import java.io.*;
import java.util.List;

import javax.swing.*;	//Library for GUI and Timer
import java.awt.*;
import java.awt.event.*;

//-----------Classes for JSON Deserialization-------------
class ServerData
{
	class Data
	{
		class Wot
		{
			public int players_online; //Server online
			public String server;      //Server name
		}
		public List<Wot> wot;		   //List of servers
	}
	public String status;
	public Data data;
}
//-----------------------------------

//-----------------------------------
class CheckServerPlayers
{
    private Gson gson = new Gson();		// GSON library object
    private URL WGAPIPlayers;   		// Wargaming.net API URL
    private ServerData WoTData;			// Object for JSON deserialization
    
    public String WoTDataJSON;			// JSON file
    
    //----------------------------------
    public void getData() throws Exception	// Function sends request to servers and gets serialized data as JSON file
    {
        WGAPIPlayers = new URL("https://api.worldoftanks.eu/wgn/servers/info/?application_id=demo&game=wot");	// Server API URL
        WoTDataJSON = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(WGAPIPlayers.openStream()));	// BufferedReader gets data from server
        
        String inputLine;
        String answer = "";
        
        while ((inputLine = in.readLine()) != null)
        {
            WoTDataJSON = WoTDataJSON + inputLine;	// Reading JSON data to string
        }
        in.close(); // Close BufferedReader
    }
    //----------------------------------
    
    // Function deserializes data from JSON to Object
    public void deserializeJSON()
    {
        WoTData = gson.fromJson(WoTDataJSON, ServerData.class);
    }
    
    // Function returns players quanitiy from 1st server
    public String getEU1Players()
    {
        return "" + WoTData.data.wot.get(1).players_online;
    }
    // Function returns players quantity from 2nd server
    public String getEU2Players()
    {
        return "" + WoTData.data.wot.get(0).players_online;
    }
}
//-----------------------------------

//-------Class for ping latency check---------
class CheckPing
{
	final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();	// ICMP4J library object
    
    public String getLatency(String server)
    {
    	String result = "";
    	
    	request.setHost(server);	// Set host for check

		IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);	// Sending request
		String formattedResponse = IcmpPingUtil.formatResponse(response);		// And writing response to String
		
		Matcher matcher = Pattern.compile("time=(.*?)([a-z]){2}").matcher(formattedResponse);	// Setting parsing rules
		
        while(matcher.find())
        {
            result = matcher.group(1);	// Parsing String for results
        }
        return result;
    }
}
//-----------------------------------

class Window extends JFrame
{
	public static Timer checker;	//Timer object
	
	//--------Labels for 1st server----------
	JLabel labelServerEU1Name = new JLabel("Server: WoT Europe 1");
	JLabel labelServerEU1Players = new JLabel("Players online: receiving data");
	JLabel labelServerEU1Ping = new JLabel("Ping: testing latency");
	Image imageA = Toolkit.getDefaultToolkit().createImage(Window.class.getResource("/resources/EU1.png"));
	ImageIcon image1 = new ImageIcon(imageA);
    JLabel labelServerEU1Icon = new JLabel(image1);
    //---------------------------------------
    
    //--------Labels for 2nd server----------
    JLabel labelServerEU2Name = new JLabel("Server: WoT Europe 2");
    JLabel labelServerEU2Players = new JLabel("Players online: receiving data");
    JLabel labelServerEU2Ping = new JLabel("Ping: testing latency");
    Image imageB = Toolkit.getDefaultToolkit().createImage(Window.class.getResource("/resources/EU2.png"));
    ImageIcon image2 = new ImageIcon(imageB);
    JLabel labelServerEU2Icon = new JLabel(image2);
    //---------------------------------------
    
    JLabel labelServerEUTotal = new JLabel("Total players: calculating");	//Label for total calculation
    
    JLabel labelAutoCheckStatus = new JLabel("Auto-check is disabled");		//Label with information about auto-checking server status
    
    //-----Action listener class for button@Enable auto-check@
    public class StartAutoChecking implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		if (timerIsRunning == false) //If timer is disabled
    		{
    			checker.start();	//Enable timer
    			timerIsRunning = true; //Remember it
    			labelAutoCheckStatus.setText("Auto-check is enabled"); //Change message in label
    			System.out.println("Info: Auto-check is enabled");	//Message in console
    		}
    		else // if timer is enabled
    		{
    			JOptionPane.showMessageDialog(null, "Auto-check is already enabled."); //Message if timer is already enabled
    			System.out.println("Info: Auto-check is already enabled."); //Message in console
    		}
    	}
	}
	JButton buttonStartAutoCheck = new JButton("Start auto update");
	//----------------------
	
	//-----Action listener class for button@Disable auto-check@
	public class StopAutoChecking implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		if (timerIsRunning == true)
    		{
    			checker.stop();
    			timerIsRunning = false;
    			labelAutoCheckStatus.setText("Auto-check is disabled");
    		}
    		else
    		{
    			JOptionPane.showMessageDialog(null, "Auto-check is already disabled.");
    			System.out.println("Info: Auto-check is already disabled.");	
    		}
    	}
	}
    JButton buttonStopAutoCheck = new JButton("Stop auto update");
    //---------------------
    
    boolean timerIsRunning = false;
    
	public Window()
	{
		super("World of Tanks EU servers status");
		this.setResizable(false); //Deny resizing of windows
		setSize(600, 430);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setLayout(null); //Disable auto-layout of object
		
		//------Server 1 labels setting positions------
    	
    	labelServerEU1Name.setBounds(100, 200, 150, 15);	//Server 1 name
    	this.add(labelServerEU1Name);
    	
    	labelServerEU1Players.setBounds(100, 230, 150, 15);	//Server 1 players
    	this.add(labelServerEU1Players);
    	
    	labelServerEU1Ping.setBounds(100, 260, 150, 15); 	//Server 1 ping
    	this.add(labelServerEU1Ping);
    	
    	labelServerEU1Icon.setBounds(100, 25, 150, 150);	//Server 1 Icon
    	this.add(labelServerEU1Icon);
    	//---------------------------
    	
    	//------Server 2 labels setting positions-----
    	
    	labelServerEU2Name.setBounds(330, 200, 150, 15);	//Server 2 name
    	this.add(labelServerEU2Name);
    	
    	labelServerEU2Players.setBounds(330, 230, 150, 15);	//Server 2 players
    	this.add(labelServerEU2Players);
    	
    	labelServerEU2Ping.setBounds(330, 260, 150, 15);	//Server 2 ping
    	this.add(labelServerEU2Ping);
    	
    	labelServerEU2Icon.setBounds(330, 25, 150, 150);	//Server 2 Icon
    	this.add(labelServerEU2Icon);
    	//--------------------------
    	
    	labelServerEUTotal.setBounds(225, 290, 150, 15);	//Label @Total players@ setting positions
    	this.add(labelServerEUTotal);
    	
    	this.setVisible(true);	//Set window visible
    	
    	//----------Action listener for Timer----------
    	ActionListener zadanie = new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent evt) 
			{
				try
				{
					updateData();	
				}
 				catch(Exception e)
 				{
 					
 				}
			}
		};
		//---------------------------------------------
		
    	SwingUtilities.invokeLater(new Runnable() 
    	{
 			@Override
 			public void run() 
 			{
				checker = new Timer(6000, zadanie);
			}
		});
		
		//----------Adding buttons to the form and adding action listeners-----------
		buttonStartAutoCheck.setBounds(140, 330, 150, 30);//setting position and size
		ActionListener actionListener1 = new StartAutoChecking();
		buttonStartAutoCheck.addActionListener(actionListener1);//adding action listener
		this.add(buttonStartAutoCheck);//adding to the form
		
		buttonStopAutoCheck.setBounds(300, 330, 150, 30);//setting position and size
		ActionListener actionListener2 = new StopAutoChecking();
		buttonStopAutoCheck.addActionListener(actionListener2);//adding action listener
		this.add(buttonStopAutoCheck);//adding to the form
		//-----------------------------------
		
		labelAutoCheckStatus.setBounds(235, 360, 150, 30);
		this.add(labelAutoCheckStatus);
	}
	
	public void updateData() throws Exception //data update function
	{
		String result1 = ""; //Variables for
    	String result2 = ""; //access latency
        
        CheckPing ping = new CheckPing();//ChackPing class object
        
        result1 = ping.getLatency("login.p1.worldoftanks.eu");//getting latency for 1st
        result2 = ping.getLatency("login.p2.worldoftanks.eu");//and 2nd server
        
        String EU1 = "";	//Variables for
        String EU2 = "";	//players online
        
        CheckServerPlayers CPSObject = new CheckServerPlayers(); //CheckServerPlayers class object
        
        CPSObject.getData();// Getting data from Wargaming.net API Server
        
        CPSObject.deserializeJSON();//Deserialize JSON to object
        
        EU1 = CPSObject.getEU1Players();//Getting EU1 online players
        EU2 = CPSObject.getEU2Players();//Getting EU2 online players
        
        int players_total = Integer.parseInt(EU1) + Integer.parseInt(EU2);	//Calculating total online
        
        this.labelServerEU1Players.setText("Players online: " + EU1);	//Output to labels Servers online
        this.labelServerEU2Players.setText("Players online: " + EU2);
        
        this.labelServerEU1Ping.setText("Ping: " + result1);	//Output to labels latency
        this.labelServerEU2Ping.setText("Ping: " + result2);
        
        this.labelServerEUTotal.setText("Total players: " + players_total); // Output Total online
        
        System.out.println("EU1: " + EU1 + ", ping: " + result1);
        System.out.println("EU2: " + EU2 + ", ping: " + result2);//Output to the consloe
        System.out.println("Total players: " + players_total); 
	}
}

//------Application main class-------
public class WoTServerStatus 
{
	Window window = new Window(); //Initialization of main window object
    public static void main(String[] args) throws Exception 
    {
    	//Window window = new Window(); //Initialization of main window object
    	window.updateData();	//Updating statistics about servers
	}
}
//-----------------------------------
