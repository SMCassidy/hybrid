package gen;

import java.io.IOException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;

public class TaskSetReader {

	public static void main (String[] argv) throws IOException{
		
		//FileReader filereader = new FileReader("task_set.txt");
		
		//filereader.close();
		
		
		Scanner in = new Scanner(new FileReader("task_set.txt"));
		while(in.hasNext()) {
			String[] s = in.next().split(",");
			for(int i=0;i<s.length;i++) {
				if(i==0) {
					System.out.println("TYPE:" + s[i]);
				}
				else {
					System.out.println("RES:" + s[i]);
				}
				
			}
	//		for(String str : s) {
	//			System.out.println(str);
	//		}
		//	System.out.println('#');
		}
		in.close();
		
	}
	
}
