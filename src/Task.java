import java.util.Calendar;

public class Task extends Email {
	

	public Task(String owner, Calendar creation_date, Calendar due_date,
			String recipient, String title, String data, boolean completed) {
		super(owner, creation_date, due_date, recipient, title, data);
		this.completed = completed;
	}



	
	public boolean isComplete(){
		return this.completed;
	}
	
	public void setCompleted(boolean status){
		this.completed= status;
	}
	
}
