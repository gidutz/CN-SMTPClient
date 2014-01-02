import java.util.Calendar;

public class Task extends Email {
	
	private boolean completed;

	public Task(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data, String sender) {
		super(owner, creation_date, due_date, recipient, title, data, sender);
		this.completed = false;
	}



	
	public boolean isComplete(){
		return this.completed;
	}
	
	public void setCompleted(boolean status){
		this.completed= status;
	}
	
}
