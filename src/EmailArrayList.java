import java.util.*;

public class EmailArrayList<E> extends ArrayList<Email> {
    SQLiteDBHelper dbHelper;

    public SQLiteDBHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * @param dbHelper
     */
    public EmailArrayList(SQLiteDBHelper dbHelper) {
        super();
        this.dbHelper = dbHelper;
    }

    /**
     * adds an email only to the memory without making a disk access
     *
     * @param arg0
     * @return
     */
    public synchronized boolean loadFromDisk(Email arg0) {
        synchronized (dbHelper) {
            boolean arraySuccess = super.add(arg0);

            return arraySuccess;
        }
    }

    /**
     * adds an email both to the disk and memory
     *
     * @param arg0
     * @return
     */
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
     * returns a referance the email whose id is given
     *
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

    /**
     * removes the email from the list and deletes from the memory
     *
     * @param arg0
     * @return
     */
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

    /**
     * removes an email from the data structure and deletes it form the databse
     * @param id
     * @return
     */
    public synchronized Email remove(int id) {
        int dbSuccess = 0;
        boolean arraySuccess = false;
        Email email = null;
        synchronized (dbHelper) {

            for (Email temp : this) {
                if (temp.getId() == id) {
                    arraySuccess = super.remove(temp);
                    dbSuccess = dbHelper.remove(temp);
                    email = temp;
                    break;
                }
            }
        }
        return email;

    }

    /**
     * updates the email by removing it from the memory and re putting it,
     * in addition to making the necessary disk changes
     *
     * @param arg0
     * @return true if the update was successful
     */
    public synchronized boolean update(Object arg0) {
        boolean arraySuccess = false;
        synchronized (dbHelper) {
            super.remove(arg0);
            int dbSuccess = 0;
            if (arg0 instanceof Email) {
                Email email = (Email) arg0;
                arraySuccess = this.loadFromDisk(email);
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
