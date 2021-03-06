package gen;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TaskSetGen {
	
	static ArrayList<Character> types;
	static ArrayList<Integer> copy;
	static Integer c;
	static Integer r;
	static Integer d;
	

	public static void main(String[] argv) throws IOException {
		
		types = new ArrayList<Character>();
		types.add('A');
		types.add('B');
		types.add('C');
		types.add('D');
	//	types.add('E');
	//	types.add('F');
	//	types.add('G');
	//	types.add('H');		
	try {
		    FileWriter fileWriter = new FileWriter("task_set.txt");
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    for(int j=0;j<40;j++) {
		    c = ThreadLocalRandom.current().nextInt(0,4);		//how many task types
		    printWriter.print(types.get(c)+",");
			copy = new ArrayList<Integer>(); 
			printWriter.print("r" + c.toString()+",");			//Every A has an r1, every B an r2 etc.
		    d = ThreadLocalRandom.current().nextInt(0, 4 + 1);	//how many resources get assigned to task
		    for(int i=0;i<d;i++) {
		    	r = ThreadLocalRandom.current().nextInt(1,11);	//how many possible resources exist, (1,11) = ten resources
				  if(!copy.contains(r)) {
					  printWriter.print("r" + r.toString()+",");
					  copy.add(r);
					 }
		    }
		    printWriter.println();
		    }
		  //  printWriter.printf("Product name is %s and its price is %d $", "iPhone", 1000);
		    printWriter.close();
	}
	catch(IOException e) {
		
	}
	System.exit(0);
	}
	
}
;