package drfoliberg.network;

import java.io.Serializable;

import drfoliberg.task.Task;

public class Message implements Serializable  {
	
	private static final long serialVersionUID = -483657531000641905L;
	
	private int code ;
	private Task task;
	
	public Message(int code) {
		this.code = code;
	}
	
	public Message(Task t){
		this.code = ClusterProtocol.TASK_REQUEST;
		this.task = t;
	}

	public int getCode(){
		return code;
	}
	
	public Task getTask(){
		return task;
	}
	
	
	
}
