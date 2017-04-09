#!/bin/bash
cd .. || exit
cd .. || exit
echo "Compiling..."
javac Project1/Client/Client.java Project1/Server/ServerRMI.java
echo "Almost running..."
echo "Subprotocols: BACKUP, RESTORE, DELETE, RECLAIM, STATE"
echo "Subprotocol: "
read -r subprotocol
echo "Server to connect ([host:]Port): "
read -r serverToConnect

if [ "${subprotocol^^}" == "BACKUP" ]; then
	echo "File path: ";
    read -r filePath;
	echo "Replication degree: ";
	read -r replicationDegree;
    java Project1/Client/Client $serverToConnect $subprotocol $filePath $replicationDegree;
elif [ "${subprotocol^^}" == "RESTORE" ]; then
	echo "File path: ";
    read -r filePath;
    java Project1/Client/Client $serverToConnect $subprotocol $filePath;
elif [ "${subprotocol^^}" == "DELETE" ]; then
	echo "File path: ";
    read -r filePath;
    java Project1/Client/Client $serverToConnect $subprotocol $filePath;
elif [ "${subprotocol^^}" == "RECLAIM" ]; then
	echo "Number of KBytes: ";
	read -r numKBytes;
    java Project1/Client/Client $serverToConnect $subprotocol $numKBytes;
elif [ "${subprotocol^^}" == "STATE" ]; then
    java Project1/Client/Client $serverToConnect $subprotocol;
else
    echo "Invalid subprotocol.";
fi
cd Project1/Client || exit
echo "Removing .class files..."
rm *.class
echo "Press Enter to continue...: "
read -r enter
