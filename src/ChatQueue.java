import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ChatQueue {
	private final int TOTAL_MESSAGES = 30;
	Queue<ChatMessage> messages_queue;

	public ChatQueue() {
		this.messages_queue = new ArrayBlockingQueue<ChatMessage>(30);
	}

	/**
	 * Adds message to the queue if the queue is full, one message is popped out
	 * of the queue
	 * 
	 * @param message
	 */
	public void addMessage(ChatMessage message) {
		synchronized (messages_queue) {
			if (messages_queue.size() == TOTAL_MESSAGES) {
				messages_queue.poll();
			}
			messages_queue.add(message);
		}

	}

	public String echoMessagesHTML() {
		Object[] messages_array;
		synchronized (messages_queue) {
			messages_array =  messages_queue.toArray();
		}
		StringBuilder result = new StringBuilder();
		for (Object obj : messages_array) {
			ChatMessage message = (ChatMessage)obj;
			result.append("<p><b>");
			result.append(message.getUser());
			result.append(": </b>");
			result.append(message.getMessage());
			result.append("</p><br>");
		}
		return result.toString();

	}

}
