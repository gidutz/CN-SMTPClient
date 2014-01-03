import java.util.ArrayList;

public abstract class EmailHandler implements Runnable {
	ArrayList<Email> emails = new ArrayList<Email>();
	private final long PAUSE = 500L;

	@Override
	public void run() {
		while (true) {
			checkStatus();
			try {
				this.wait(PAUSE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract void handelEmail(Email email);

	private void checkStatus() {

		synchronized (emails) {
			for (Email email : emails) {
				if (email.isExpired()) {
					handelEmail(email);
				}
			}
		}

	}

}
