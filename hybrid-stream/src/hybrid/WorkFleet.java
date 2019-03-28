package hybrid;

import java.util.concurrent.TimeUnit;

import hybrid.Worker;

import java.util.ArrayList;

public class WorkFleet {
	private static int total_worked=0;
	private static long startTime;
	private static long endTime;
	private static long elapsedTime;
	private static ArrayList<Worker> workers;
	private static int work_num=4;
	
	public void time() {
		long startTime = System.currentTimeMillis();
		//Apparently you need to use System.nanotime();
		
		long endTime = System.currentTimeMillis();

		long timeElapsed = endTime - startTime;

		System.out.println("Execution time in milliseconds: " + timeElapsed);
	}

	public static void main(String[] argv) {
		// TODO Auto-generated method stub
		System.out.println("Initialising Workfleet...");
		Integer n = Integer.parseInt(argv[0]);
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
/*
		Worker worker1 = new Worker(1);
		Worker worker2 = new Worker(2);
		Worker worker3 = new Worker(3);
		try {
			worker1.listen();
			worker2.listen();
			worker3.listen();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/	
	}
	
	public static synchronized void incTotal() {
		total_worked++;
		System.out.println("Total="+total_worked);
		if(total_worked==1) {
			startTime = System.nanoTime();
		}
		if(total_worked==9) {
			elapsedTime = System.nanoTime() - startTime;
			System.out.println("FINAL TIME: " + String.valueOf(elapsedTime/ 1_000_000_000.0) + "s");
			System.exit(0);
		}
	}
	
}
