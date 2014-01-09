import java.util.*;

public class EmailArrayList<E> extends ArrayList<Email> {
    SQLiteDBHelper dbHelper;

    public EmailArrayList(SQLiteDBHelper dbHelper) {
        super();
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean add(Email arg0) {

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

    @Override
    public boolean remove(Object arg0) {
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
     *
     */
    private static final long serialVersionUID = 3183656292450348037L;

}
