import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class InGameChat{

	private JTextArea incoming;
	private JTextField outgoing;
	private JPanel applet;
	private ChatClient networkStartup;
	private PlayerMob player;
	private JPanel chatPanel;
	private JPanel chatPanelSection;
	private JLayeredPane layeredPane;
	private JLayeredPane keyListenerLayer;
	private ArrayList<PlayerMob> players;


	public InGameChat(PlayerMob plyr){
		player = plyr;
		layeredPane = new JLayeredPane();
	}

	public void chat(JPanel frm){
		System.out.println("inside chat method of gameChat");
		applet = frm;

		AddKeyListener keyListener =new AddKeyListener();
		keyListener.setPlayer(player);

		chatPanel = new JPanel();
		chatPanelSection = new JPanel();

		keyListenerLayer = new JLayeredPane();
		keyListenerLayer.add(keyListener, 10);

		System.out.println("before the JTextArea");
		
		incoming = new JTextArea(5,49);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		DefaultCaret caret = (DefaultCaret)incoming.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoing = new JTextField(45);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());

		applet.add(BorderLayout.CENTER, keyListenerLayer);
		applet.add(BorderLayout.CENTER, layeredPane);

		keyListener.setFocusable(true);
		keyListener.requestFocusInWindow();

		System.out.println("setting the Layout");
		
		chatPanel.setLayout( new BoxLayout( chatPanel, BoxLayout.Y_AXIS ) );

		chatPanel.add(qScroller);
			chatPanelSection.add(outgoing);
			chatPanelSection.add(sendButton);
		chatPanel.add(chatPanelSection);

		applet.add(BorderLayout.SOUTH, chatPanel);

		applet.validate();
		applet.repaint();

		//initialize
		networkStartup.InGameChatInitialize(outgoing,incoming);

		applet.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				keyListener.setFocusable(true);
				keyListener.requestFocusInWindow();
			}
		});

		outgoing.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	networkStartup.InGameChatSendButtonListener(outgoing,incoming);
        }});
		
		System.out.println("Starting Drawing Panel Thread");
		
		try{
			Thread chatThread = new Thread(new DrawingPanel());
			chatThread.start();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}

	public void drawFromServer(PlayerMob plyr){
		System.out.println("Added player to layeredPane");
		layeredPane.add(plyr, 100);

	}

	public void setPlayers(ArrayList<PlayerMob> allPlayers){
		players = allPlayers;

	}

	public class DrawingPanel implements Runnable{
			
		public void run(){
			try{
				System.out.println("right before the while loop of the thread");
				while(true){
					System.out.println("Right before the sleep");
					
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					applet.remove(layeredPane);
					Iterator<PlayerMob> allPlayers = players.iterator();
					PlayerMob aPlayer = null;
					while(allPlayers.hasNext()){
						aPlayer = (PlayerMob) allPlayers.next();
						//System.out.println("INTHELOOP:info.getUsername =" + info.getUsername() + " myChat.getUsername =" + myChat.getUsername());
						aPlayer.move();
					}
					
					System.out.println("in the thread while loop about to validate");
					
					applet.add(BorderLayout.CENTER,layeredPane);
					applet.validate();
					applet.repaint();

				}
			}catch(Exception ev){}
		}
	}

	public void setNetObject(ChatClient netObj){
		networkStartup = netObj;
	}
	
	public class SendButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			networkStartup.InGameChatSendButtonListener(outgoing,incoming);
		}
	}
}