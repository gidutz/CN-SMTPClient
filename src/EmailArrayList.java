import java.util.*;

public class EmailArrayList<E> extends ArrayList<Email> {
    SQLiteDBHelper dbHelper;

    public EmailArrayList(SQLiteDBHelper dbHelper) {
        super();
        this.dbHelper = dbHelper;
    }

    public  synchronized  boolean loadFromDisk(Email arg0){
        synchronized (dbHelper) {
            boolean arraySuccess = super.add(arg0);

            return  arraySuccess;
        }
    }
    @Override
    public synchronized boolean add(Email arg0) {

        synchronized (dbHelper) {
            boolean arraySuccess = super.add(arg0);
            int dbSuccess = 0;
            if (arg0 instanceof Email) {
                Email email = (Email) arg0;
                dbSuccess = dbHelper.add(email);
            }
            return ((dbSuccess == 1) && arraySuccess);
        }

    }

    /**
     * @param id
     * @return The email with the specifed id, or null if not found
     */
    public Email getId(int id) {
        for (Email email : this) {
            if (email.getId() == id) {
                return email;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean remove(Object arg0) {
        synchronized (dbHelper) {
            boolean arraySuccess = super.remove(arg0);
            int dbSuccess = 0;
            if (arg0 instanceof Email) {
                Email email = (Email) arg0;
                dbSuccess = dbHelper.remove(email);
            }
            return ((dbSuccess == 1) && arraySuccess);
        }
    }

    public synchronized Email remove(int id) {
        int dbSuccess = 0;
        boolean arraySuccess = false;
        Email email = null;
        synchronized (dbHelper) {

            for (Email temp : this) {
                if (email.getId() == id) {
                    arraySuccess = super.remove(temp);
                    dbSuccess = dbHelper.remove(temp);
                    email = temp;
                    break;
                }
            }
        }
        return email;

    }

    public synchronized boolean update(Object arg0) {
        synchronized (dbHelper) {
            boolean arraySuccess = super.remove(arg0);
            int dbSuccess = 0;
            if (arg0 instanceof Email) {
                Email email = (Email) arg0;
                dbSuccess = dbHelper.updateEmail(email);
            }
            return ((dbSuccess == 1) && arraySuccess);
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = (long) (144563 * Math.random());

}
