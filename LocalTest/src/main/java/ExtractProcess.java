import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;

public class ExtractProcess {
    
    static List<ExtractEntity> entities;
    
    static final int BATCH = 5000;
    
    public static void run() {
	try {
	    XMLParser.loadSources();
	    XMLParser.loadDestinations();
	    loadEntities();
	    for( final ExtractEntity entity: entities) {
		new Thread(){
		    public void run() {
			      System.out.println("Thread Running");
			      try {
			      insertBatch(extractEntity(entity), entity);
			      } catch ( Exception e) {
				  
			      }
			    }
			  }.start();
	    }
	} catch (JDOMException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e ) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static void insertBatch(ResultSet results, ExtractEntity entity) throws SQLException, ClassNotFoundException {
	   Connection destinationCon = getConnection(entity.getDestination());
	   Connection sourceCon = getConnection(entity.getSource());
	   PreparedStatement prepDestStmt = destinationCon.prepareStatement(entity.getInsertStatement());
	   PreparedStatement prepSourceStmt = sourceCon.prepareStatement("update " + entity.getName() + " set is_extracted = 1 where ident = ?");
	   while(results.next()) {
	       mapInsertStatementParms(prepDestStmt, results, entity );
	       prepSourceStmt.setLong(1, results.getInt("ident"));
	       prepSourceStmt.addBatch();
	   }
	   	
	   prepDestStmt.executeBatch();
	   destinationCon.commit();
	   prepSourceStmt.executeBatch();
	   sourceCon.commit();
	   
    }

    private static void mapInsertStatementParms(PreparedStatement prepStmt, ResultSet results, ExtractEntity entity) throws SQLException {
	int parmIdx = 1;
	for(Map.Entry<String, Class> col: entity.getCols().entrySet()) {
	    // this is a fast update for the extraction value to the dw
	    if ( col.getKey().equals("is_extracted"))
		prepStmt.setInt(parmIdx, 1);
	    //normal datatype checking
	    if( col.getValue() == String.class)
		prepStmt.setString(parmIdx, results.getString(col.getKey()));
	    if( col.getValue() == Timestamp.class)
		prepStmt.setTimestamp(parmIdx, results.getTimestamp(col.getKey()));
	    if( col.getValue() == Long.class)
		prepStmt.setLong(parmIdx, results.getLong(col.getKey()));
	    if( col.getValue() == Integer.class)
		prepStmt.setInt(parmIdx, results.getInt(col.getKey()));
	    if( col.getValue() == Float.class)
		prepStmt.setFloat(parmIdx, results.getFloat(col.getKey()));
	    if( col.getValue() == Double.class)
		prepStmt.setDouble(parmIdx, results.getDouble(col.getKey()));
	    parmIdx++;
	}
	prepStmt.addBatch();
    }

    private static void loadEntities() throws JDOMException, IOException {
	entities = XMLParser.getEntities();
    }

    private static ResultSet extractEntity(ExtractEntity entity) throws ClassNotFoundException, SQLException {
	Connection con = getConnection(entity.getSource());
	Statement statement = con.createStatement();
	statement.setMaxRows(BATCH);
	return statement.executeQuery(entity.getExtractStatement());
    }

    public static Connection getConnection(String name) throws ClassNotFoundException, SQLException {
	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	HashMap<String, String> datasource = XMLParser.datasources.get(name);
	Connection con = DriverManager.getConnection(datasource.get("url"), datasource.get("username"),
		datasource.get("password"));
	return con;

    }

}
