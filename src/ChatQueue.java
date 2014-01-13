import java.util.*;
import java.util.concurrent.*;

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
            messages_array = messages_queue.toArray();
        }
        StringBuilder result = new StringBuilder();
        result.append("<html><head> <meta http-equiv=\"refresh\" content=\"5\" ></head><body>");
        for (int i = messages_array.length - 1; i >= 0; i--) {
            ChatMessage message = (ChatMessage) messages_array[i];
            result.append("<p><b>");
            result.append(message.getUser());
            result.append(": </b>");
            result.append(message.getMessage());
            result.append("</p>");
        }
        result.append("</body></html>");

        return result.toString();

    }

}
