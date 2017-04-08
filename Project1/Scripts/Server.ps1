cd ..
cd ..
echo "Compiling..."
javac Project1/Server/Server.java
$protVer = Read-Host -Prompt "Protocol version"
$serverId = Read-Host -Prompt "Server id"
echo "Running..."
java Project1/Server/Server $protVer $serverId localhost:2005 224.0.0.243 2500 224.0.0.213 3000 224.0.0.251 6000
cd Project1/Server
echo "Removing .class files..."
rm *.class
pause
