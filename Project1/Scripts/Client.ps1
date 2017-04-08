cd ..
cd ..
echo "Compiling..."
javac Project1/Client/Client.java Project1/Server/ServerRMI.java
$subprotocol = Read-Host -Prompt "Subprotocol"
$filePath = Read-Host -Prompt "File path"
echo "Running..."
java Project1/Client/Client localhost:2005 $subprotocol $filePath
cd Project1/Client
echo "Removing .class files..."
rm *.class
pause
