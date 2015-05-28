import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JPanel;

public class ChatServer {

	ArrayList<ObjectOutputStream> clientOutputStreams;
	ArrayList<String> usernames;
	ArrayList<ChatObject> clientObjects;
	ArrayList<Integer> xCoordinates;
	ArrayList<Integer> yCoordinates;
	ObjectOutputStream outStream;
	static JPanel serverPanel;

	public class ClientHandler implements Runnable {
		ObjectInputStream inStream;
		public ClientHandler(Socket clientSocket){
			try{
				Socket sock = clientSocket;
				inStream = new ObjectInputStream(sock.getInputStream());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		
		//The object comes in from a client
		public void run(){
			Object o1 = null;
			try{
				while ((o1 = inStream.readUnshared()) != null){
					ChatObject info = (ChatObject) o1;
					//the casted fressh new object that came in
					info.setArrayList(usernames);

					if(info.getUsername() != "undefined" && info.getArrayList().indexOf(info.getUsername()) < 0){
						System.out.println("New User Logged in");
						/*JLabel newLogin = new JLabel("New User Logged in");
						serverPanel.add(newLogin);
						serverPanel.validate();
						serverPanel.repaint();
						serverPanel.setSize(600,400);
						serverPanel.setVisible(true);
						*/
						
						usernames.add(info.getUsername());
						clientObjects.add(info);
						tellThisGuy(clientObjects,usernames.indexOf(info.getUsername()));
						xCoordinates.add(info.getXCoordinate());
						yCoordinates.add(info.getYCoordinate());
					}
					else if(info.getUsername() != "undefined" && usernames.indexOf(info.getUsername()) >= 0){
						clientObjects.set(usernames.indexOf(info.getUsername()), info);
						xCoordinates.set(usernames.indexOf(info.getUsername()),info.getXCoordinate());
						yCoordinates.set(usernames.indexOf(info.getUsername()),info.getYCoordinate());
					}
					info.setArrayList(usernames);

					for(int i : xCoordinates) {
					    System.out.print(i+"\t");
					}
					for(int j : yCoordinates) {
						System.out.print(j+"\n");
					}

					tellEveryone(info);
				}
			}
			catch(SocketException e){
				System.err.println("User Logged out");
				removeLoggedOutUsers();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

		public static void main (String[] args){
			//new ChatServer().go();  //Old way of doing it.
			ChatServer server = new ChatServer();	//New and improved way
			server.go();
		}

		public void go(){
			clientOutputStreams = new ArrayList<ObjectOutputStream>();
			usernames = new ArrayList<String>();
			clientObjects = new ArrayList<ChatObject>();
			xCoordinates = new ArrayList<Integer>();
			yCoordinates = new ArrayList<Integer>();
			
			try{
				@SuppressWarnings("resource")
				ServerSocket serverSock = new ServerSocket(5000);
				
				System.out.println("Server Up");
				
				
				while(true){
					Socket clientSocket = serverSock.accept();
		            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
		            clientOutputStreams.add(outStream);
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		public void removeLoggedOutUsers(){
				Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
				ObjectOutputStream out = null;
				ChatObject conTest = new ChatObject();
					try{
						synchronized(it){
							while(it.hasNext()){
								out = (ObjectOutputStream) it.next();
								out.writeUnshared(conTest);
								out.reset();
							}
						}
					}
					catch(SocketException e){
						//clientObjects.remove(clientOutputStreams.indexOf(out));
						clientOutputStreams.remove(out);
						System.err.println("Removing terminated user from clientOutputStreams");
					}
					catch(Exception e){
						e.printStackTrace();
					}
		}
		
		//The object gets sent to the ONE client it has One Job Oneee Job
		public void tellThisGuy(ArrayList<ChatObject> clientObjects, int thisInt){
			
			Iterator<ChatObject> clientObject = clientObjects.iterator();
			ObjectOutputStream thisGuy = null;
			
			thisGuy = clientOutputStreams.get(thisInt);
				try{
					synchronized(thisGuy){			
							while(clientObject.hasNext()){
								thisGuy.writeUnshared(clientObject.next());
								thisGuy.reset();
							}
						}
				}
				catch(Exception e){
					e.printStackTrace();
				}
		}
		
		//The object gets sent out to every client
		public void tellEveryone(Object one){
			Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
			ObjectOutputStream out = null;

				try{
					synchronized(it){
						while(it.hasNext()){
							out = (ObjectOutputStream) it.next();
							out.writeUnshared(one);
							out.reset();
						}
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
		}

		public void setJPanel(JPanel serverPane) {
			// TODO Auto-generated method stub
			serverPanel = serverPane;
		}
}