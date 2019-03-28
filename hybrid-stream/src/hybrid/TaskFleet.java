package hybrid;

import hybrid.Task;

public class TaskFleet {
	private static int id=0;

	public static void main(String[] argv) throws Exception{
		Task taskA = new Task("A");
		Task taskB = new Task("B");
			for(int i=0;i<2;i++) {
			taskA.send();
			taskB.send();
			//task.setMessage(task.getMessage().concat("-"));
			//Thread.sleep(500);
		}
		
	}
	
	public static synchronized int stampID() {
		id++;
		return id;
	}
	
}
