package hybrid;

import hybrid.Worker;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WorkFleet {
	private static int total_worked=0;
	private static long startTime;
	private static long elapsedTime;
	private static ArrayList<Worker> workers;
	private static int work_num=4;
	private static int total_rescheduled=0;
	private static int hits=0;
	private static int worked_goal=40;
	private static char policy='M';

	public static void main(String[] argv) {
		
		System.out.println("Initialising Workfleet...");
		workers = new ArrayList<Worker>();
		
		for(int i=0;i<work_num;i++) {
			workers.add(new Worker(i+1, policy));
		}
		
		try {
			for(Worker worker : workers) {
				worker.listen();
			}
		} catch (Exception e) {
			
		}
	}
	
	public static synchronized void prefChange(int id, String pref) {
		for (int i=0;i<work_num;i++) {
			workers.get(i).updatePrefs(id, pref);
		}
	}
	
	public static synchronized void incTotalRescheduled(int k) {
		total_rescheduled += k;
	}
	
	public static synchronized void incHits() {
		hits++;
	}
	
	public static synchronized void incTotalWorked() {
		total_worked++;
		System.out.println("Total="+total_worked);
		if(total_worked==1) {
			startTime = System.nanoTime();
		}
		if(total_worked==worked_goal) {
			//System.out.println("total_worked=" + total_worked);
			elapsedTime = System.nanoTime() - startTime;
			try {
			FileWriter fileWriter = new FileWriter("results.txt", true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
		//	fileWriter.write("workers: " + work_num + "\n");
		//	fileWriter.write("time:"+String.valueOf(elapsedTime/ 1_000_000_000.0) + "s"+", hits:"+hits+", resch:"+total_rescheduled);
			printWriter.println("workers: " + work_num + " policy: " + policy + " goal: " + worked_goal);
			printWriter.println("time:"+String.valueOf(elapsedTime/ 1_000_000_000.0) + "s"+", hits:"+hits+", resch:"+total_rescheduled);
			/*System.out.println("FINAL TIME: " + String.valueOf(elapsedTime/ 1_000_000_000.0) + "s");
			System.out.println("HITS: " + hits);
			System.out.println("TASKS RESCHEDULED: " + total_rescheduled + " times");
			for(Worker w: workers) {
				System.out.println(w.toString());
			}
			*/
			printWriter.close();
			}
			catch(IOException e) {
				
			}
			System.out.println("Exiting..");
			System.exit(0);
		}
	}
	
}
