
public abstract class EmailHandler<E> implements Runnable {
	
	EmailArrayList<Email> emails;
	@SuppressWarnings("unchecked")
	public EmailHandler(EmailArrayList<E> emails) {
		this.emails = (EmailArrayList<Email>) emails;
	}



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
