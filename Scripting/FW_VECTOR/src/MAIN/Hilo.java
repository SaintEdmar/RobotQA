package MAIN;

import java.sql.SQLException;

public class Hilo extends Thread{
		public void run() {
			try {
				Schedule.DemonioSchedule();
			} catch (InterruptedException | SQLException e) {
				e.printStackTrace();
			}
		}
}
