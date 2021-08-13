import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCExtract {

    public static void runJDBCpersist() {
	Connection oltp = null;
	Connection dw = null;
	try {
	    oltp = getOLTPJDBCConnection();
	    dw = getDWJDBCConnection();
	    Statement oltpStmt = oltp.createStatement();
	    Statement dwStmt = dw.createStatement();
	    ResultSet rs = oltpStmt.executeQuery("select * from simple_table");
	    while (rs.next())
		dwStmt.executeUpdate("insert into simple_table values(" + rs.getInt(1) + ",\'" + rs.getTimestamp(2)
			+ "\',\'" + rs.getString(3) + "\')");
	    dw.commit();
	} catch (Exception e) {
	    System.out.println(e);
	} finally {
	    cleanUp(oltp);
	}
    }

    private static void cleanUp(Connection con) {
	try {
	    if (con != null && !con.isClosed())
		con.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    public static void runJDBCBatchpersist(int batchSize) {
	Connection oltp = null;
	Connection dw = null;
	try {
	    oltp = getOLTPJDBCConnection();
	    dw = getDWJDBCConnection();
	    Statement oltpStmt = oltp.createStatement();
	    ResultSet rs = oltpStmt.executeQuery("select * from simple_table");
	    PreparedStatement prepStmt = dw.prepareStatement("insert into simple_table values(?,?,?)");
	    int resultIdx = 0;
	    while (rs.next()) {
		prepStmt.setInt(1, rs.getInt(1));
		prepStmt.setTimestamp(2, rs.getTimestamp(2));
		prepStmt.setString(3, rs.getString(3));
		prepStmt.addBatch();
		resultIdx++;
		if (resultIdx % batchSize == 0)
		    prepStmt.executeBatch();
	    }
	    prepStmt.executeBatch();
	    dw.commit();
	} catch (Exception e) {
	    System.out.println(e);
	} finally {
	    cleanUp(oltp);
	}
    }

    public static Connection getOLTPJDBCConnection() throws ClassNotFoundException, SQLException {
	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	Connection con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;DatabaseName=TestDB", "dpsuser",
		"htGIKSx3QBDO3Cs!");
	return con;

    }

    public static Connection getDWJDBCConnection() throws ClassNotFoundException, SQLException {
	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	Connection con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;DatabaseName=TestDW", "dpsuser",
		"htGIKSx3QBDO3Cs!");
	return con;

    }
}
