import java.util.List;

import dao.SimpleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAExtract {

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

}
