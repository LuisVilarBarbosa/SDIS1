#!/bin/bash
cd .. || exit
cd .. || exit
echo "Protocol version: "
read -r protVer 
echo "Server id: "
read -r serverId
echo "Server port: "
read -r serverPort
echo "Running..."
java Project1/Server/Server $protVer $serverId localhost:$serverPort 224.0.0.243 2500 224.0.0.213 3000 224.0.0.251 6000
cd Project1/Scripts || exit
echo "Press Enter to continue...: "
read -r enter
