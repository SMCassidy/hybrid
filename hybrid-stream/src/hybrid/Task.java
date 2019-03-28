package hybrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import hybrid.TaskFleet;
import org.json.*;


public class Task {

	  private static final String TASK_QUEUE_NAME = "task_queue";

	  private static String msg = "message";
	  private String type;
	  JSONObject obj;
	  String m;

	  
	  public Task(String type) {
		  this.type = type;
	  }
	  
	  public void send() throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    try (Connection connection = factory.newConnection();
	         Channel channel = connection.createChannel()) {
	        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
	        
	        buildMessage();
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
		  obj.put("resources", "");
		  obj.put("rescheduled_amount", 0);	  
		  JSONArray r = new JSONArray();
		  obj.put("resources",r);
		  obj.put("workers_rescheduled", r);
		  obj.put("result", r);
	  }
	  
	  public void setMessage(String s) {
		  msg = s;
	  }
	  
	  public String getMessage() {
		  return msg;
	  }

	
}
