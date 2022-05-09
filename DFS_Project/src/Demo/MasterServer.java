package Demo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.rmi.*;

public class MasterServer implements MasterReplicaInterface, MasterServerClientInterface, Remote{
	
	static Registry registry ;
	
	private int nextTID;
	private Random randomGen;
	private int replicationN = 1; //number of replicas
	private Map<String,	 List<ReplicaLoc> > filesLocationMap;
	private Map<String,	 ReplicaLoc> primaryReplicaMap;
	private List<ReplicaLoc> replicaServersLocs;
	private List<ReplicaMasterInterface> replicaServersStubs; 
	private Map<String, String> permissionMap;
	//List<String> permissionDetails = new ArrayList<String>();
	public static final String xmlFilePath = "C:\\Users\\mouni\\eclipse-workspace\\muv\\MyProject01\\user.xml";
	public MasterServer() {
		filesLocationMap = new HashMap<String, List<ReplicaLoc>>();
		primaryReplicaMap = new HashMap<String, ReplicaLoc>();
		replicaServersLocs = new ArrayList<ReplicaLoc>();
		replicaServersStubs = new ArrayList<ReplicaMasterInterface>();
		permissionMap = new TreeMap<String, String>();
		nextTID = 0;
		randomGen = new Random();
		
	}
	
	private void createNewFile(String fileName) {
		System.out.println("Creating new file initiated");
		int luckyReplicas[] = new int[replicationN];
		List<ReplicaLoc> replicas = new ArrayList<ReplicaLoc>();
		Set<Integer> chosenReplicas = new TreeSet<Integer>();
		
		for(int i=0; i<luckyReplicas.length;i++) {
			do {
				luckyReplicas[i] = randomGen.nextInt(replicationN);
			}while(!replicaServersLocs.get(luckyReplicas[i]).isAlive() || chosenReplicas.contains(luckyReplicas[i]));
			
			chosenReplicas.add(luckyReplicas[i]);
			replicas.add(replicaServersLocs.get(luckyReplicas[i]));
		
			try {
				replicaServersStubs.get(luckyReplicas[i]).createFile(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		int primary = luckyReplicas[0];
		try {
			replicaServersStubs.get(primary).takeCharge(fileName, replicas);			
		}catch(RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		filesLocationMap.put(fileName, replicas);
		primaryReplicaMap.put(fileName, replicaServersLocs.get(primary));
	}
	
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException, IOException, RemoteException{
		List<ReplicaLoc> replicaLocs = filesLocationMap.get(fileName);
		if(replicaLocs == null) {
			throw new FileNotFoundException();
		}
		return replicaLocs;
		}
	
	public List<ReplicaLoc> readReplicas() throws FileNotFoundException, IOException, RemoteException{
		List<ReplicaLoc> replicaLocs = replicaServersLocs;
		
		return replicaLocs;
		}
	
	public WriteAck write(String fileName) throws RemoteException, IOException{
		System.out.println("Write request initiated");
		long timeStamp = System.currentTimeMillis();
		
		List<ReplicaLoc> replicaLocs = filesLocationMap.get(fileName);
		int tid = nextTID+1;
		if (replicaLocs == null) {
			createNewFile(fileName);
		}
		
		ReplicaLoc primaryReplicaLoc = primaryReplicaMap.get(fileName);
		
		if (primaryReplicaLoc == null)
			throw new IllegalStateException("No primary replica found");
		
		return new WriteAck(tid, timeStamp,primaryReplicaLoc);
	}
	
	
	public void registerReplicaServer(ReplicaLoc replicaLoc, ReplicaInterface replicaStub) {
		replicaServersLocs.add(replicaLoc);
		replicaServersStubs.add((ReplicaMasterInterface) replicaStub);
		
	}
	
	public static MasterServer startMaster() throws AccessException, RemoteException{
		MasterServer master = new MasterServer();
		MasterServerClientInterface stub = 
				(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);
		registry.rebind("MasterServerClientInterface", stub);
		System.out.println("Server ready");
		return master;
	}
	
	public static void connectToReplicaServers(MasterServer master)throws IOException, NotBoundException{
		System.out.println("Contacting replica servers ");
		BufferedReader br = new BufferedReader(new FileReader("ReplicaDetails.txt"));
		int n = Integer.parseInt(br.readLine().trim());
		ReplicaLoc replicaLoc;
		String s;
		String[] s1;
		String port;

		for (int i = 0; i < n; i++) {
			s = br.readLine().trim();
			s1 = s.split(":");
			port = s1[1];
			replicaLoc = new ReplicaLoc(i, s1[0] ,Integer.parseInt(port), true);
//			Registry registry = LocateRegistry.getRegistry(s1[0],Integer.parseInt(port));
			Registry registry = LocateRegistry.getRegistry(Integer.parseInt(port));
			ReplicaMasterInterface stub1 = (ReplicaMasterInterface) registry.lookup("ReplicaClient"+i);

			master.registerReplicaServer(replicaLoc, stub1);

			System.out.println("replica server state: "+stub1.isAlive());
		}
		br.close();
	}
//	public boolean loginUser(String username, String password) throws IOException {
//		String[] userdet_arr;
//		String userdetails;
//		boolean loginresult = false;
//		BufferedReader br = new BufferedReader(new FileReader("UserDetails.txt"));
//		
//		while((userdetails = br.readLine())!= null){
//				System.out.println(userdetails);
//				userdet_arr = userdetails.split(":");
//				if(username.equals(userdet_arr[0]) && password.equals(userdet_arr[1]))  {
//					loginresult = true;
//				}
//			}
//		if(loginresult) {
//			System.out.println("User logged in successfully");
//		}
//		else{
//			System.out.println("Incorrect username or password");
//		}
//		
//			br.close();	
//			return loginresult;
//	}
	
	
	
	
	public boolean loginUser(String username, String password) throws IOException{
		boolean flag = false;
		try {
			
			File inputfile = new File(xmlFilePath);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (inputfile);
	        doc.getDocumentElement().normalize();
//	        System.out.println(username + "   "+ password);
//	        Element rootElement = doc.getDocumentElement();
	        NodeList nList = doc.getElementsByTagName("user");
	        for (int i=0;i< nList.getLength();i++) {
	        	Node n = nList.item(i);
	        	if(n.getNodeType() == n.ELEMENT_NODE) {
	        		Element e = (Element) n;
//	        		System.out.println("Elements "+ e);
//	        		System.out.println(e.getElementsByTagName("username").item(0).getTextContent());
//	        		System.out.println(e.getElementsByTagName("password").item(0).getTextContent());
	        		if (e.getElementsByTagName("username").item(0).getTextContent().equals(username)) {
	        			{
	        				String svalue = e.getElementsByTagName("saltvalue").item(0).getTextContent();
	        				String epwd = e.getElementsByTagName("password").item(0).getTextContent();
	        				flag = PassBasedEnc.verifyUserPassword(password, epwd, svalue);	        				
	        			}
	        		}
	        	}
	        }
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	@Override
	public void setPermission(String filename, String owner, String permission) {
		// TODO Auto-generated method stub
		permissionMap.put(filename, owner+":"+permission);
		System.out.println("Permission Map: " + permissionMap);
		try {
			File inputfile = new File(xmlFilePath);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (inputfile);
	        doc.getDocumentElement().normalize();
	        NodeList nList = doc.getElementsByTagName("user");
	        for (int i=0;i< nList.getLength();i++) {
	        	Node n = nList.item(i);
	        	if(n.getNodeType() == n.ELEMENT_NODE) {
	        		Element e = (Element) n;
//	        		System.out.println(e.getElementsByTagName("username").item(0).getTextContent());
//	        		System.out.println(e.getElementsByTagName("password").item(0).getTextContent());
	        		if (e.getElementsByTagName("username").item(0).getTextContent().equals(owner)) {
	        			{
	        				Element fname = doc.createElement("file");
	        				Element prmsn = doc.createElement("permission");
	        				Element name = doc.createElement("name");
	        				e.appendChild(fname);
//	        				fname.setAttribute("name", filename);
//	        				fname.appendChild(doc.createTextNode(filename));
	        				fname.appendChild(name);
	        				name.appendChild(doc.createTextNode(filename));
	        				fname.appendChild(prmsn);
	        				prmsn.appendChild(doc.createTextNode(permission));
	        				TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        		        Transformer transformer = transformerFactory.newTransformer();
	        		        DOMSource source = new DOMSource(doc);
	        		        StreamResult result = new StreamResult(inputfile);
	        		        transformer.transform(source, result);        				
	        			}
	        		}
	        	}
	        }
        
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
//	public String fetchPermission(String filename, String userloggedIn) {
//		// TODO Auto-generated method stub
//		String permissionvalue;
//		String permission = permissionMap.get(filename);
//		String [] permissionarray = permission.split(":");
//		System.out.println("Permission Array:  " +permissionarray);
//		String userId = permissionarray[0];
//		if(userId.equals(userloggedIn)) {
//			permissionvalue = "owner";
//		}
//		else {
//			permissionvalue = permissionarray[1];
//		}
//		
//		return permissionvalue;
//	}
	
	public String fetchPermission(String filename, String userloggedIn) {
		try {
			File inputfile = new File(xmlFilePath);
			String permissionValue;
			String permissions;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (inputfile);
	        doc.getDocumentElement().normalize();
	        NodeList nList = doc.getElementsByTagName("file");
	        
	        for(int i = 0; i< nList.getLength();i++) {
	        	Node n = nList.item(i);
	        	Element e = (Element) n;
	        	Node p = n.getParentNode();
	        	Element p1 = (Element) p;
	        	permissionValue = p1.getElementsByTagName("username").item(0).getTextContent();
//	        	System.out.println("p1  " + permissionValue);
	        	if(e.getElementsByTagName("name").item(0).getTextContent().equals(filename)) {
	        		permissions = e.getElementsByTagName("permission").item(0).getTextContent();
//	        		System.out.println("Permissions " + permissions);
	        		if(permissionValue.equals(userloggedIn)) {
	        			return "owner";
	        		}
	        		else {
	        			return permissions;
	        		}
	        		
	        	}
	        	
	        }     
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public boolean registerNewUser(String uname, String pwd) {
		try {
			String saltvalue = PassBasedEnc.getSaltvalue(30);
			String encryptedpwd = PassBasedEnc.generateSecurePassword(pwd, saltvalue); 
			
			File inputfile = new File(xmlFilePath);
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (inputfile);
	        doc.getDocumentElement().normalize();
	        
	        
	        Element rootElement = doc.getDocumentElement();
//	        System.out.println(rootElement);
	        
	        Element user = doc.createElement("user");
	        rootElement.appendChild(user);
	        
	        Element username = doc.createElement("username");
	        username.appendChild(doc.createTextNode(uname));
	        user.appendChild(username);
	        
	        Element password = doc.createElement("password");
	        password.appendChild(doc.createTextNode(encryptedpwd));
	        user.appendChild(password);
	        
	        Element salt = doc.createElement("saltvalue");
	        salt.appendChild(doc.createTextNode(saltvalue));
	        user.appendChild(salt);
	        
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        StreamResult result = new StreamResult(inputfile);
	        transformer.transform(source, result); 
	        return true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	public static void main(String[] args) throws IOException, NotBoundException {
		try {
			int regPort = 59218;
//			System.setProperty("java.rmi.server.hostname", "10.200.152.195");
			LocateRegistry.createRegistry(regPort);
			registry = LocateRegistry.getRegistry(regPort);
//			registry = LocateRegistry.getRegistry("10.200.152.195", regPort);
			MasterServer master = startMaster();
			connectToReplicaServers(master);
			
			
		}catch(RemoteException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

	

	

	
		

}
