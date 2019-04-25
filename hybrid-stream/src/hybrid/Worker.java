package hybrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import hybrid.WorkFleet;
import java.util.ArrayList; 
import java.util.HashMap;
import org.json.*;

public class Worker {

	  private char policy; 							//'b' baseline; 'r' resource; 'p' preference
	  private int id;								//Worker ID
	  private int worked;							//Number of tasks processed
	  private int res_seen;							//Resource seen counter
	  private int type_count_max;					//Highest number in type_counts
	  private ArrayList<String> resources;			//Array of resources
	  private ArrayList<String> tasks_rejected; 	//Array of Task IDs
	  private HashMap<String, Integer> type_counts;	//Map of task type to number of times that type accepted
	  private HashMap<Integer,String> worker_prefs; //Map of known worker preferences
	  private JSONObject jo;						//JSON Buffer for incoming task
	  private JSONObject r;							//JSON Buffer for resource array
	  private static final String TASK_QUEUE_NAME = "task_queue";
	  String message;
	  String preference;							//Workers type preference
	  
	  
	  public Worker(int id, char policy) {
		  this.id = id;
		  this.policy = policy;
		  worked = 0;
		  res_seen=0;
		  type_count_max=0;
		  resources = new ArrayList<String>();
		  tasks_rejected = new ArrayList<String>();
		  type_counts = new HashMap<String,Integer>();
		  worker_prefs = new HashMap<Integer,String>();
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
		        	
		        	try {
		            doWork(message);
		        	}
		        	catch (InterruptedException e){
		                Thread.currentThread().interrupt();
		        	}
		        } finally {
		            System.out.println(id + "- [x] Done");
		            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);		
		            
		            if(policy == 'p') {
		            	
		            	String msg_type = jo.get("type").toString();
		            	
		            	 if (!type_counts.containsKey(msg_type)) {
		            		 //if task type not in hashmap, add it
		            		 type_counts.put(msg_type, 0);
		            	 }
		            	 else {
		            		 //increment type count for each accepted task
		            		 type_counts.put(msg_type, type_counts.get(msg_type)+1);
		            	 }

		            	 //update preference
		            	 if(type_counts.get(msg_type) > type_count_max) {
		            		type_count_max = type_counts.get(msg_type);
		            		preference = msg_type;
		            	 }
		            	 
		            }
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
		  
		  //we should go through and increment sleep 
		  //for every bit of sleep time needed
		  
		  int sleep = 250;				//Each task takes 250ms to complete
		  
		  
      	//Acquire Resources
		  
		 String msg_type = jo.get("type").toString();
      	
      //	JSONObject r = new JSONObject();
      	r.put("res", jo.get("resources"));
      	String RString = new String(r.get("res").toString());
      	for (String s : RString.split("\"")) {
      		if(s.charAt(0) == 'r') {
      			if(!resources.contains(s)) {
      					resources.add(s);
      					sleep += 1000; //Add 1000ms per resource to be acquired
      					res_seen++;
      			}
      		}
      	}
      	
      	if(policy == 'p') {
      		if (msg_type != preference) {
      			sleep += 500;		//Incur penalty for working non-preferred task types
      		}
      	}
      	
      	Thread.sleep(sleep);	//Sleep to process task

	  }
	  
	  public String toString() {
		  return "ID: " + this.id + " Worked: " + this.worked + " R: (" + this.res_seen + ") " + this.resources.toString();
	  }
	  
	  public void updatePrefs(int id, String new_pref) {
		  if (id == this.id) {
			  this.preference = new_pref;
		  }
		  else {
			  worker_prefs.put(id, new_pref);
		  }
		  
	  }
	  
	  private void announceNewPref() {
		  //Call the coordinator to tell it we have changed our preference
		  WorkFleet.prefChange(this.id, this.preference);
	  }
	  
	  private boolean decide(){
		  
		  
		  //Logic for whether to accept task
		  
		  //Baseline Policy
		  if(tasks_rejected.contains(jo.get("id").toString())){
			  //If we have rejected the Task before, accept it
			  return true;
		  }
		  
		  //Resource Policy
		  if(this.policy == 'r') {  
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
		}
		
		//Preference Policy
		if(this.policy == 'p') {
			//if there exists
			//worker in WorkerPreferences Array
			//with same type
			if(!worker_prefs.containsValue(jo.get("type").toString())) {
				return true;
			}
		}

		  return false;
	  }
	  
	 
}
