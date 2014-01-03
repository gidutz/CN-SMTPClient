import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDBHelper {
	Connection c;

	public void openDaatabase() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CCREATE TABLE IF NOT EXISTS COMPANY "
					+ "(ID INT PRIMARY KEY     NOT NULL,"
					+ " NAME           TEXT    NOT NULL, "
					+ " AGE            INT     NOT NULL, "
					+ " ADDRESS        CHAR(50), " + " SALARY         REAL)";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}

	/**
	 * create the three tables the i need
	 * 
	 * @param tableName
	 * @param fields
	 */
	public void createTable(String tableName, HashMap<String, String> fields) {
		if (c != null) {
			Statement stmt = null;
			try {

				stmt = c.createStatement();
				StringBuilder sql = new StringBuilder();
				sql.append("CREATE TABLE IF NOT EXISTS" + tableName);
				sql.append("(");
				for (Map.Entry<String, String> entry : fields.entrySet()) {
					sql.append(entry.getKey() + " " + entry.getValue());
				}

				sql.append(")");

				stmt.executeUpdate(sql.toString());
				stmt.close();
				c.close();
				System.out.println("Table Created!");
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				System.exit(0);
			}
		}
	}

	public void createTables() {
		if (c != null) {
			Statement stmt = null;
			try {

				stmt = c.createStatement();
				String sql = "CREATE TABLE reminders "
						+ "(ID INT PRIMARY KEY     NOT NULL,"
						+ " recp      TEXT    NOT NULL, "
						+ " date      TEXT    NOT NULL, "
						+ " title     TEXT, "
						+ " timestap  REAL)";
				stmt.executeUpdate(sql);

				stmt.executeUpdate(sql);
				stmt.close();
				c.close();
				System.out.println("Table Created!");
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				System.exit(0);
			}
		}
	}
}
