import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLParser {
    private static final String ENEITY_FILENAME = "src/main/resources/entities.xml";
    private static final String DATASOURCE_FILENAME = "src/main/resources/datasources.xml";
    
    public static HashMap<String, HashMap<String,String>> datasources = new HashMap<String, HashMap<String,String>> (); 
    
    
    public static void loadSources() throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File(DATASOURCE_FILENAME));
        Element rootNode = doc.getRootElement();
        List<Element> sources = rootNode.getChildren("source");
        for ( Element source : sources ) {
            HashMap<String, String> newSource = new HashMap<String, String>();
            newSource.put("url", source.getChildText("url"));
            newSource.put("username", source.getChildText("username"));
            newSource.put("password", source.getChildText("password"));
            String sourceName = source.getAttribute("name").getValue();
            datasources.put(sourceName, newSource);
        }
    }  
    
    public static void loadDestinations() throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File(DATASOURCE_FILENAME));
        Element rootNode = doc.getRootElement();
        List<Element> sources = rootNode.getChildren("destination");
        for ( Element source : sources ) {
            HashMap<String, String> newSource = new HashMap<String, String>();
            newSource.put("url", source.getChildText("url"));
            newSource.put("username", source.getChildText("username"));
            newSource.put("password", source.getChildText("password"));
            String sourceName = source.getAttribute("name").getValue();
            datasources.put(sourceName, newSource);
        }
    }  

    public static List<ExtractEntity> getEntities() throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File(ENEITY_FILENAME));
        Element rootNode = doc.getRootElement();
        return mapEntities(rootNode.getChildren("entity"));
    }

    private static List<ExtractEntity> mapEntities(List<Element> entities) {
	List<ExtractEntity> enties = new ArrayList<ExtractEntity>();
	for ( Element entity : entities)
	    enties.add(mapEntity(entity));
	return enties; 
	
    }

    private static ExtractEntity mapEntity(Element inputNode) {
	ExtractEntity entity = new ExtractEntity();
	entity.setName(inputNode.getChildText("name"));
	entity.setExtractStatement(inputNode.getChildText("extractStatement"));
	entity.setInsertStatement(inputNode.getChildText("insertStatement"));
	entity.setSource(inputNode.getChildText("source"));
	entity.setDestination(inputNode.getChildText("destination"));
	mapExtractEntityCols(inputNode, entity);
	return entity;
    }

    private static void mapExtractEntityCols(Element inputNode, ExtractEntity entity) {
	List<Element> cols = inputNode.getChild("columns").getChildren();
	for (Element column : cols ) {
	    String name = column.getAttributeValue("name");
	    Attribute attr = column.getAttribute("class");
	    Class dataType = getDataType(attr);
	    entity.addColumn(name, dataType );
	}
    }

    private static Class getDataType(Attribute attr) {
	if (attr.getValue().equals("String"))
	    return String.class;
	if (attr.getValue().equals("Long"))
	    return Long.class;
	if (attr.getValue().equals("Timestamp"))
	    return Timestamp.class;
	return null;
    }
}
