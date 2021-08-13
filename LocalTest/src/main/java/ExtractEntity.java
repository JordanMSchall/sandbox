import java.util.HashMap;
import java.util.LinkedHashMap;

public class ExtractEntity {

    private String name;
    private String extractStatement;
    private String insertStatement;
    private String source;
    private String destination;

    // HashMap<columnName, datatype>
    private LinkedHashMap<String, Class> cols;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getExtractStatement() {
	return extractStatement;
    }

    public void setExtractStatement(String extractStatement) {
	this.extractStatement = extractStatement;
    }

    public String getInsertStatement() {
	return insertStatement;
    }

    public void setInsertStatement(String insertStatement) {
	this.insertStatement = insertStatement;
    }

    public HashMap<String, Class> getCols() {
	return cols;
    }

    public void setCols(LinkedHashMap<String, Class> cols) {
	this.cols = cols;
    }

    public String getSource() {
	return source;
    }

    public void setSource(String source) {
	this.source = source;
    }

    public String getDestination() {
	return destination;
    }

    public void setDestination(String destination) {
	this.destination = destination;
    }

    public ExtractEntity() {
	super();
	this.cols = new LinkedHashMap<String, Class>();
    }

    public void addColumn(String colName, Class datatype) {
	this.cols.put(colName, datatype);
    }

    @Override
    public String toString() {
	return "ExtractEntity [name=" + name + ", extractStatement=" + extractStatement + ", insertStatement="
		+ insertStatement + ", source=" + source + ", destination=" + destination + ", cols=" + cols + "]";
    }

}
