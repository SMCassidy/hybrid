package hybrid;

import hybrid.Worker;

import java.util.ArrayList;

public class WorkFleet {
	private static int total_worked=0;
	private static long startTime;
	private static long elapsedTime;
	private static ArrayList<Worker> workers;
	private static int work_num=4;

	public static void main(String[] argv) {
		System.out.println("Initialising Workfleet...");
	//	Integer n = Integer.parseInt(argv[0]);
		workers = new ArrayList<Worker>();
	//	System.out.println("n: " + n.toString());

/*		Worker worker1 = new Worker(1);
		try {
			worker1.listen();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/		
		
		for(int i=0;i<work_num;i++) {
			workers.add(new Worker(i+1));
		}
		
		try {
			for(Worker worker : workers) {
				worker.listen();
			}
		} catch (Exception e) {
			
		}
	}
	
	public static synchronized void incTotal() {
		total_worked++;
		System.out.println("Total="+total_worked);
		if(total_worked==1) {
			startTime = System.nanoTime();
		}
		if(total_worked==100) {
			//System.out.println("total_worked=" + total_worked);
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("FINAL TIME: " + String.valueOf(elapsedTime/ 1_000_000_000.0) + "s");
			for(Worker w: workers) {
				System.out.println(w.toString());
			}
			System.exit(0);
		}
	}
	
}
