package hybrid;

import java.io.FileReader;
import java.util.Scanner;

import hybrid.Task;

public class TaskFleet {
	private static int id=0;
	private static int spawned=0;

	public static void main(String[] argv) throws Exception{
		
		System.out.println("SPAWNING...");
	
		Task task = new Task();
		
		Scanner in = new Scanner(new FileReader("task_set.txt"));
		while(in.hasNext()) {
			String[] s = in.next().split(",");
			task.setTask(s);
			task.send();
		}
		in.close();
		
		//read from file 
		//on every line
		//task.setTask(String[] s)
		//task.send()
		
		
	//	Task taskA = new Task("A");
	//	Task taskB = new Task("B");
	//	Task taskC = new Task("C");
	//		for(int i=0;i<50;i++) {
	//		taskA.send();
	//		taskB.send();
	//		taskC.send();
			//task.setMessage(task.getMessage().concat("-"));
			//Thread.sleep(1000);
	//	}
		
	}
	
	public static synchronized int stampID() {
		id++;
		return id;
	}
	
}
