#!/bin/bash
cd .. || exit
cd .. || exit
echo "Compiling..."
javac Project1/Server/Server.java
javac Project1/Client/Client.java Project1/Server/ServerRMI.java
cd Project1/Scripts
