cd ..
cd ..
$protVer = Read-Host -Prompt "Protocol version"
$serverId = Read-Host -Prompt "Server id"
$serverPort = Read-Host -Prompt "Server port"
echo "Running..."
java Project1/Server/Server $protVer $serverId localhost:$serverPort 224.0.0.243 2500 224.0.0.213 3000 224.0.0.251 6000
cd Project1/Scripts
pause
