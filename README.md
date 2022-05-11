# PCS_DFS
## CSMC 626:Priniples of Computer Security
## Secure Distributed File System 

[GitHub Link for this project](https://github.com/MadhavGTX47/PCS_DFS).

### Team Members & Contribution:-

- Sai Jahnavi Bachu : LU59970  - Worked on the Replica Server Implementation, Encryption.
- Sai Madhav Kolluri : ND92132 - Worked on the Replica Server Implementation, Encryption, Environment Setup and Report.
- Mounica Uddagiri : QL23899   - Worked on the Master and the Replica Server Implementation, File permissions and Encryption.
- Venkata Rama Lakshman Nukala : HR39332 - Worked on the Client and Master Server Implementation, File Permissions, Malicious Server Defense. 

## Necessary Stuff to know 

- We used Eclipse to develop this project in JAVA, and we have listed the Libraries used int the section below.
- Make sure your JAVA is installed on your pc (The latest version if possible)
- To Run You need at least one instance of the console running each:- Replica, Client, Master.
- The overall flow to execute would be run the Replica servers, then the master and the client, and all the commands will be sent from a client.

## Libraries Used
- [Package java.rmi](https://docs.oracle.com/javase/8/docs/api/java/rmi/package-summary.html)
- [Package javax.crypto](https://docs.oracle.com/javase/8/docs/api/javax/crypto/package-summary.html)
- [Package javax.xml](https://docs.oracle.com/javase/8/docs/api/index.html)
- [java.security ](https://docs.oracle.com/javase/7/docs/api/java/security/package-summary.html)



##  Step-by-Step instructions:-
- First Install Eclipse in the machines where you will be running the code (i.e Server, Client, Master.)
- Then Open Eclipse and Click File -> Open Projects from File System and import the Project Folder.
- Now once the project is open go to *src/demo*, till here it is common for all the machines.
- Now we should setup each of the following in order.

# Replica Server
- First got to the file **ReplicaServer.java** and change the port number for each replica. 
- Now change the replica details for each replica as shown below in the *line:177*
```sh
Example:-
ReplicaServer rs = new ReplicaServer(0, "./");
```
```sh
ReplicaServer rs = new ReplicaServer(Replicanumber, "Directory path");
```
- Now  Run the **ReplicaServer.java**, then in the console, it should show Replica is Ready.
- And Repeat this Process on Each Replica. And keep in mind every Replica should have a different port number and different Replica Number. 

> As we start each replica a Folder named replica along with a replica number will be created above the current directory path. 

# Master Server
- For the Master Server, run the **MasterServer.java**, then in the Console, it should show It is connected to the Replica Servers.

# Client Server
- For the Client, run the **ClientServer.java**, then in the Console it should show sign in or signup, for first type sign up, then it will ask for username give a user name, next it will ask a password give a password.
```sh
signin
signup
``` 

- Now, after signup it forwoards the user to sign in, here give user username and passsword.
- After an succesful sign in Possible opertaions which can be done are displayed, And the user has to type the name of the operation he needs.

```sh
read
write 
rename
delete
list files
```
- **read** : Here it asks us the name of the file to read, after we give a name to read,it shows the content of the file.
- **write** : Here it will ask name of the file to write, then after giving the name , it asks for file permission (Example:rw, rwx) we give it in unix style, then it shows if it was able to write succesfully.
- **rename** : Here it will ask name of the file to rename, and you give a new name to rename it to.
- **delete** : Here it will ask name of the file to delete, and you give naem of the file you want to delete.
- **list files** : here it lists all the files present, but the names here are all encrypted.



