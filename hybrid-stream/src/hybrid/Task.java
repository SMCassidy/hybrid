package hybrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import hybrid.TaskFleet;
import org.json.*;


public class Task {

	  private static final String TASK_QUEUE_NAME = "task_queue";

	  private static String msg = "message";
	  private String type;
	  JSONObject obj;
	  String m;

	  
	  public Task() {
		  this.type = "A";
	  }
	  
	  public void send() throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    try (Connection connection = factory.newConnection();
	         Channel channel = connection.createChannel()) {
	        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
	        
	       // buildMessage();
	        m = obj.toString();

	      //  String message = String.join(" ", msg);
	        channel.basicPublish("", TASK_QUEUE_NAME,
	                 MessageProperties.PERSISTENT_TEXT_PLAIN,
	                 m.getBytes("UTF-8"));

	      //  channel.basicPublish("", TASK_QUEUE_NAME,
	       //         MessageProperties.PERSISTENT_TEXT_PLAIN,
	        //        message.getBytes("UTF-8"));
	     //   System.out.println(" [x] Sent '" + message + "'");
	    }
	  }
	  
	  public void buildMessage() {
		  
		  obj = new JSONObject();
		  obj.put("id", TaskFleet.stampID());
		  obj.put("type", this.type);
		 // obj.put("resources", "");
		  obj.put("rescheduled_amount", 0);	  
		  JSONArray result = new JSONArray();
		  JSONArray w_r = new JSONArray();
		  JSONArray res = new JSONArray();
		  obj.put("workers_rescheduled", w_r);
		  obj.put("result", result);
		  
		  //Generate task resource requirements 
		  //Randomly select between 0-3 of 10 resources r1 to r10
		  //Assign it to task resource JSON array
		  ArrayList<Integer> copy = new ArrayList<Integer>();
		  int k = ThreadLocalRandom.current().nextInt(0, 3 + 1);
		  for(int i=0;i<k;i++) {
			  Integer l = ThreadLocalRandom.current().nextInt(1,11);
			  //Check we're not adding the same resource twice
			  if(!copy.contains(l)) {
			  res.put("r".concat(l.toString()));
			  copy.add(l);
			  }
			  
		  }
		  
		  obj.put("resources",res);

	  }
	  
	  public void setTask(String[] setup) {
		  obj = new JSONObject();
		  obj.put("id", TaskFleet.stampID());
		  obj.put("rescheduled_amount", 0);	
		  JSONArray res = new JSONArray();
		  JSONArray w_r = new JSONArray();
	
		  for(int i=0;i<setup.length;i++) {
				if(i==0) {
	//				System.out.println("TYPE:" + setup[i]);
					obj.put("type", setup[i]);
				}
				else {
//					System.out.println("RES:" + setup[i]);
					res.put(setup[i]);
				}
		  }
		  
		  obj.put("workers_rescheduled", w_r);
		  obj.put("resources",res);
	  }
	  
	  public void setMessage(String s) {
		  msg = s;
	  }
	  
	  public String getMessage() {
		  return msg;
	  }

	
}
