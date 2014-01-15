public abstract class EmailHandler<E> implements Runnable {

    EmailArrayList<Email> emails;
    final String CRLF = "\r\n";
    SQLiteDBHelper db;

    @SuppressWarnings("unchecked")
    public EmailHandler(EmailArrayList<E> emails, SQLiteDBHelper db) {
        this.emails = (EmailArrayList<Email>) emails;
        this.db = db;
    }

    private final long PAUSE = 3000L;

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

        loadFromDatabase();
        synchronized (emails) {
            for (Email email : emails) {
                if (email.isExpired()||email.isComplete()) {
                    try {
                        handelEmail(email);
                    } catch (Exception e) {
                    }
                }
            }
        }


    }

    protected abstract void loadFromDatabase();


}
