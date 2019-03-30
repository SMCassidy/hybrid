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
	  private char policy; 							//'b' baseline; 'r' resource; 'p' preference
	  private int id;								//Worker ID
	  private int worked;							//Number of tasks processed
	  private ArrayList<String> resources;			//Array of resources
	  private ArrayList<String> tasks_rejected; 	//Array of Task IDs
	  private ArrayList<String> tasks_accepted;	//Array of Task Types
	  private JSONObject jo;						//JSON Buffer for incoming task
	  private JSONObject r;							//JSON Buffer for resource array
	  String message;
	  
	  public Worker(int id, char policy) {
		  this.id = id;
		  this.policy = policy;
		  worked = 0;
		  tasks_rejected = new ArrayList<String>();
		  resources = new ArrayList<String>();
		  jo = new JSONObject();
		  r = new JSONObject();
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
	        message = new String(delivery.getBody(), "UTF-8");
	        System.out.println(id +"- [x] Received '" + message + "'");
	        
	        //Deserialize JSON
	    	jo = new JSONObject(message);
	        
	        if (decide()) {	
	        	//Accept Task
		        try {

		      //  	System.out.println(r.get("res").toString());
		     //		System.out.println("RES:" + resources.toString());

		        	
		        	try {
		            doWork(message);
		        	}
		        	catch (InterruptedException e){
		                Thread.currentThread().interrupt();
		        	}
		        } finally {
		            System.out.println(id + "- [x] Done");
		            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		          //  tasks_accepted.add((String)jo.get("type"));
		            this.worked++;
		            WorkFleet.incTotal();
		        }
	        }
	        else {					
	        	
	        	//Reschedule Task
	        	
	        	//Add Task ID to tasks_rejected array
	        	tasks_rejected.add(jo.get("id").toString());
	        	
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

	  private void doWork(String task) throws InterruptedException{
		
		  //Acquire resources task requires
		  //Incur sleep for each resource to acquire
		  //Sleep depending on task type
		  
      	//Acquire Resources
      	
      //	JSONObject r = new JSONObject();
      	r.put("res", jo.get("resources"));
      	String RString = new String(r.get("res").toString());
      	for (String s : RString.split("\"")) {
      		if(s.charAt(0) == 'r') {
      			if(!resources.contains(s)) {
      					resources.add(s);
      					Thread.sleep(1000); //Sleep while acquiring resources
      			}
      		}
      	}
      	
      	Thread.sleep(250);	//Sleep to process task

	  }
	  
	  public String toString() {
		  return "ID: " + this.id + " Worked: " + this.worked + " R: " + this.resources.toString();
	  }
	  
	  private boolean decide(){
		  
		  //Logic for whether to accept task
		  
		  if(this.policy == 'b') {
			  //if Baseline policy, workers must accept all tasks
			  return true;
		  }
		  
		  if(tasks_rejected.contains(jo.get("id").toString())){
			  //If we have rejected the Task before, accept it
			  return true;
		  }

		  try {
	        	JSONArray re = new JSONArray(jo.get("resources"));

	        	for (int i = 0; i < re.length(); i++) {
	        		//If Task requires any resources already acquired, accept it
	        		  if (resources.contains(re.getJSONObject(i).toString())) {
	        			  return true;
	        		  }
	        		}
	        	
	        	}
	        	catch(JSONException e) {
	        		
	        	}


		  return false;
	  }
	 
}
