package hybrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import hybrid.WorkFleet;
import java.util.ArrayList; 
import org.json.*;

public class Worker {

	  private static final String TASK_QUEUE_NAME = "task_queue";
	  private int id;
	  private ArrayList<String> resources;
	  private ArrayList<String> tasks_seen;
	  JSONObject jo;
	  
	  public Worker(int id) {
		  this.id = id;
		  tasks_seen = new ArrayList<String>();
		  resources = new ArrayList<String>();
	  }

	  public void listen() throws Exception {
	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    final Connection connection = factory.newConnection();
	    final Channel channel = connection.createChannel();

	    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
	    System.out.println(id + "- [*] Waiting for messages. To exit press CTRL+C");

	    channel.basicQos(1);

	    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	        String message = new String(delivery.getBody(), "UTF-8");

	        System.out.println(id +"- [x] Received '" + message + "'");
	        
	        //decide(message)
	  
	       	//Deserialize JSON
	    	jo = new JSONObject(message);
	        
	        if (decide()) {	//Accept Task
		        try {
		            doWork(message);
		        } finally {
		            System.out.println(id + "- [x] Done");
		            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		            WorkFleet.incTotal();
		        }
	        }
	        else {					//Reschedule Task
	        	
	        	
	        	//Add Task ID to tasks_seen array
	        	tasks_seen.add(jo.get("id").toString());
	        	
	        	//Increment Reschedule Amount
	        	jo.put("rescheduled_amount", (Integer)jo.get("rescheduled_amount")+1);
	        	
	        	//Add Worker ID to array
	        	JSONArray ja = new JSONArray(jo.get("workers_rescheduled").toString());
	        	ja.put(this.id);
	        	jo.put("workers_rescheduled", ja);
	        	
	        	message = jo.toString();    	
	        	System.out.println(id + "- [ ] Rescheduling - " + message);
	        	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	        	channel.basicPublish("", TASK_QUEUE_NAME,
	                    MessageProperties.PERSISTENT_TEXT_PLAIN,
	                    message.getBytes("UTF-8"));
	        }
	        
	        
	    };
	    channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
	  }

	  private static void doWork(String task) {
		
		  //Acquire resources task requires
		  //Incur sleep for each resource to acquire
		  //Sleep depending on task type
		  
	    for (char ch : task.toCharArray()) {
	        if (ch == '.') {
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException _ignored) {
	                Thread.currentThread().interrupt();
	            }
	        }
	    }
	  }
	  
	  private boolean decide(){
		  
		  //Logic for whether to accept task
		  
		  if(tasks_seen.contains(jo.get("id").toString())){
			  //If we have seen Task before, accept it
			  return true;
		  }

		  return false;
	  }
	 
}