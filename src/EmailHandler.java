public abstract class EmailHandler<E> implements Runnable {

	EmailArrayList<Email> emails;

	@SuppressWarnings("unchecked")
	public EmailHandler(EmailArrayList<E> emails) {
		this.emails = (EmailArrayList<Email>) emails;
	}

	private final long PAUSE = 1500L;

	@Override
	public void run() {
		while (true) {
			try {
				checkStatus();
				Thread.sleep(PAUSE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
			} finally {
				try {
					Thread.sleep(PAUSE);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public abstract void handelEmail(Email email);



	private void checkStatus() {

		try {
			synchronized (emails) {
				for (Email email : emails) {
					if (email.isExpired()) {
						handelEmail(email);
					}
				}
			}
		} catch (Exception e) {
			emails.notifyAll();
		}

	}

}
