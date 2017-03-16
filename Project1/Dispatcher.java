import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Dispatcher {

    public static void main(String args[]) {
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
