import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import dao.SimpleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HubRunner {

    public static void main(String[] args) {
	resetDW();
	Instant start = Instant.now();
	runJPAPersist();
	Instant finish = Instant.now();
	System.out.println("JPA Persist milliseconds: " + Duration.between(start, finish).toMillis());

	start = Instant.now();
	runJPAMerge();
	finish = Instant.now();
	System.out.println("JPA Merge w/Records milliseconds: " + Duration.between(start, finish).toMillis());

	// reset DW
	resetDW();

	start = Instant.now();
	runJPAMerge();
	finish = Instant.now();
	System.out.println("JPA Merge with no Records milliseconds: " + Duration.between(start, finish).toMillis());

	start = Instant.now();
	runJDBCpersist();
	finish = Instant.now();
	System.out.println("JDBC persist milliseconds: " + Duration.between(start, finish).toMillis());
	
	resetDW();
	
	start = Instant.now();
	runJDBCBatchpersist(5000);
	finish = Instant.now();
	System.out.println("JDBC batch persist milliseconds: " + Duration.between(start, finish).toMillis());
    }

    private static void resetDW() {
	EntityManagerFactory entityManagerFactoryDW = Persistence.createEntityManagerFactory("DW");
	EntityManager entityManagerDW = entityManagerFactoryDW.createEntityManager();
	entityManagerDW.getTransaction().begin();
	entityManagerDW.createQuery("delete from SimpleEntity t").executeUpdate();
	entityManagerDW.getTransaction().commit();
	entityManagerDW.close();
	entityManagerFactoryDW.close();
    }

    public static void runJPAPersist() {

	EntityManagerFactory entityManagerFactoryOLTP = Persistence.createEntityManagerFactory("OLTP");
	EntityManager entityManagerOLTP = entityManagerFactoryOLTP.createEntityManager();

	EntityManagerFactory entityManagerFactoryDW = Persistence.createEntityManagerFactory("DW");
	EntityManager entityManagerDW = entityManagerFactoryDW.createEntityManager();

	List<SimpleEntity> results = entityManagerOLTP.createQuery("Select t from SimpleEntity t").getResultList();

	entityManagerDW.getTransaction().begin();

	for (SimpleEntity result : results)
	    entityManagerDW.persist(result);

	// teardown
	entityManagerDW.getTransaction().commit();
	entityManagerDW.close();
	entityManagerFactoryDW.close();
	entityManagerOLTP.close();
	entityManagerFactoryOLTP.close();
    }

    public static void runJPAMerge() {

	EntityManagerFactory entityManagerFactoryOLTP = Persistence.createEntityManagerFactory("OLTP");
	EntityManager entityManagerOLTP = entityManagerFactoryOLTP.createEntityManager();

	EntityManagerFactory entityManagerFactoryDW = Persistence.createEntityManagerFactory("DW");
	EntityManager entityManagerDW = entityManagerFactoryDW.createEntityManager();

	List<SimpleEntity> results = entityManagerOLTP.createQuery("Select t from SimpleEntity t").getResultList();

	entityManagerDW.getTransaction().begin();

	for (SimpleEntity result : results)
	    entityManagerDW.merge(result);

	// teardown
	entityManagerDW.getTransaction().commit();
	entityManagerDW.close();
	entityManagerFactoryDW.close();
	entityManagerOLTP.close();
	entityManagerFactoryOLTP.close();
    }

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
		resultIdx ++;
		if ( resultIdx % batchSize == 0)
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
