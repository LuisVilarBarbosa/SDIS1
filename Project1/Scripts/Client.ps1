cd ..
cd ..
echo "Almost running..."
echo "Subprotocols: BACKUP, RESTORE, DELETE, RECLAIM, STATE"
$subprotocol = Read-Host -Prompt "Subprotocol"
$serverToConnect = Read-Host -Prompt "Server to connect ([host:]Port)"

if($subprotocol -ieq "BACKUP") {
    $filePath = Read-Host -Prompt "File path"
    $replicationDegree = Read-Host -Prompt "Replication degree"
    java Project1/Client/Client $serverToConnect $subprotocol $filePath $replicationDegree
}
elseif($subprotocol -ieq "RESTORE") {
    $filePath = Read-Host -Prompt "File path"
    java Project1/Client/Client $serverToConnect $subprotocol $filePath
}
elseif($subprotocol -ieq "DELETE") {
    $filePath = Read-Host -Prompt "File path"
    java Project1/Client/Client $serverToConnect $subprotocol $filePath
}
elseif($subprotocol -ieq "RECLAIM") {
    $numKBytes = Read-Host -Prompt "Number of KBytes"
    java Project1/Client/Client $serverToConnect $subprotocol $numKBytes
}
elseif($subprotocol -ieq "STATE") {
    java Project1/Client/Client $serverToConnect $subprotocol
}
else {
    echo "Invalid subprotocol."
}
cd Project1/Scripts
pause
