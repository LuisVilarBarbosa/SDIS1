import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    public static void main(String args[]) {
        if(args.length != 9) {
            System.out.println("Usage: Dispatcher <protocol_version> <serverId> <srvc_access_point> " +
                    "<mcast_control_ip> <mcast_control_port> <mcast_data_backup_ip> <mcast_data_backup_port> " +
                    "<mcast_data_restore_ip> <mcast_data_restore_port>");
            return;
        }

        String protocolVersion = args[0];
        int serverId = Integer.parseInt(args[1]);
        String accessPoint = args[2];
        String mControlIp = args[3];
        int mControlPort = Integer.parseInt(args[4]);
        String mDataBackupIp = args[5];
        int mDataBackupPort = Integer.parseInt(args[6]);
        String mDataRestoreIp = args[7];
        int mDataRestorePort = Integer.parseInt(args[8]);

        String command = "";
        Scanner scanner = new Scanner(System.in);
        ArrayList<Timer> timers = new ArrayList<>();

        do {
            command = scanner.nextLine();

            Timer timer = new Timer();
            String finalCommand = command;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        System.out.println(finalCommand);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            timer.schedule(timerTask, 0);
            timers.add(timer);
        } while(!command.equalsIgnoreCase("exit"));

        scanner.close();

        for(int i = 0; i < timers.size(); i++)
            timers.get(i).cancel();
    }
}
