cd ..
cd ..
javac Project1/Client/Client.java Project1/Server/ServerRMI.java
java Project1/Client/Client localhost:2005 restore abc.txt
cd Project1/Client
rm *.class
pause
