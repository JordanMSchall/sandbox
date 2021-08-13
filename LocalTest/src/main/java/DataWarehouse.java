import java.time.Duration;
import java.time.Instant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DataWarehouse {
 
    public static void runAnalysis() {
	DataWarehouse.resetDW();
	Instant start = Instant.now();
	JPAExtract.runJPAPersist();
	Instant finish = Instant.now();
	System.out.println("JPA Persist milliseconds: " + Duration.between(start, finish).toMillis());

	start = Instant.now();
	JPAExtract.runJPAMerge();
	finish = Instant.now();
	System.out.println("JPA Merge w/Records milliseconds: " + Duration.between(start, finish).toMillis());

	// reset DW
	DataWarehouse.resetDW();

	start = Instant.now();
	JPAExtract.runJPAMerge();
	finish = Instant.now();
	System.out.println("JPA Merge with no Records milliseconds: " + Duration.between(start, finish).toMillis());

	start = Instant.now();
	JDBCExtract.runJDBCpersist();
	finish = Instant.now();
	System.out.println("JDBC persist milliseconds: " + Duration.between(start, finish).toMillis());
	
	DataWarehouse.resetDW();
	
	start = Instant.now();
	JDBCExtract.runJDBCBatchpersist(5000);
	finish = Instant.now();
	System.out.println("JDBC batch persist milliseconds: " + Duration.between(start, finish).toMillis());
    }
    
    public static void resetDW() {
	EntityManagerFactory entityManagerFactoryDW = Persistence.createEntityManagerFactory("DW");
	EntityManager entityManagerDW = entityManagerFactoryDW.createEntityManager();
	entityManagerDW.getTransaction().begin();
	entityManagerDW.createQuery("delete from SimpleEntity t").executeUpdate();
	entityManagerDW.getTransaction().commit();
	entityManagerDW.close();
	entityManagerFactoryDW.close();
    }

}
