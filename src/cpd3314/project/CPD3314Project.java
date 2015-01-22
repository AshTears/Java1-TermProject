package cpd3314.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Final Term Project for CPD-3314 Java Development I
 *
 * @author Ashika Shallow
 */
public class CPD3314Project {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Create a list to hold the products.
        List<Product> products = null;
        try {
            products = LoadOriginals();
        } catch (Exception ex) {
            Logger.getLogger(CPD3314Project.class.getName()).log(Level.SEVERE, null, ex);
        }
        String format = "";
        String fileName = "";
        String sortType = "";
        int limit = 0;
        String date = "";
        int id = 0;
        String findWhat = "";
        String finalList = "";
        
        // If the argument array is empty, the output file name is "CPD3314.xml".
        if (args.length == 0) {
            String output = buildXML(products);
            try {
                PrintWriter xmlFile = new PrintWriter("CPD3314.xml");
                xmlFile.println(output);
                xmlFile.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CPD3314Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {                     // If the argument array is not empty, determine parameters.
            for (String arg : args) {
                if (arg.contains("-format=")) {
                    format = (arg.split("=")[1]).toLowerCase();
                } else {
                    format = "xml";
                }
                if (arg.contains("-o=")) {
                    fileName = arg.split("=")[1];
                }
                if (arg.contains("-sort=")) {
                    sortType = arg.split("=")[1];
                }
                if (arg.contains("-limit=")) {
                    limit = Integer.parseInt(arg.split("=")[1]);
                }
                if (arg.contains("-getDate=")) {
                    date = arg.split("=")[1];
                }
                if (arg.contains("-getID=")) {
                    String pid = arg.split("=")[1];
                    id = Integer.parseInt(pid);
                }
                if (arg.contains("-find=")) {
                    findWhat = (arg.split("=")[1]).toLowerCase();
                }
            }
            
            //Send the list along with the arguments to the RenderList function
            finalList = RenderList(products, id, date, findWhat, sortType, limit, format);
            
            // Create the output file.
            String file = "";
            if (!fileName.isEmpty()) {
                file = fileName + "." + format;
            } else {
                file = "CPD3314." + format;
            }
            try {
                PrintWriter outputFile = new PrintWriter(file);
                outputFile.println(finalList);
                outputFile.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CPD3314Project.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * The LoadOriginals function. Loads an external XML file into a list.
     * @return A Product list.
     * @throws Exception 
     */
    public static List<Product> LoadOriginals() throws Exception {

        Serializer sam = new Persister();
        List<Product> products = null;
        File file = new File("ORIGINALS.xml");

        products = sam.read(Products.class, file).getProducts();
        return products;
    }

    /**
     * The RenderList function which modifies the format and size of the list.
     * @param products A Product list
     * @param id The ID parameter
     * @param date The date parameter
     * @param search The find a word(s) parameter.
     * @param sort The sort parameter
     * @param limit The limit parameter
     * @param format The file format parameter
     * @return String output. The modified and formatted list.
     */
    public static String RenderList(List<Product> products, int id, String date,
            String search, String sort, int limit, String format) {

        // Create a new Product list.
        List<Product> newList = products;
        
        String output = "";                         // Initialize the String output.
        
        // Filter the list if there is an ID parameter.
        if (id != 0) {
            List toDelete = new ArrayList();
            products.stream().filter((product) -> (product.getId() != id)).forEach((product) -> {
                toDelete.add(product);
            });
            newList.removeAll(toDelete);
        }
        
        // Determine if there's a date parameter, then filter the list.
        if (!date.isEmpty()) {
            List toDate = new ArrayList();
            products.stream().filter((product) -> (product.getDateAdded().matches(date))).forEach((product) -> {
                toDate.add(product);
            });
            newList = toDate;
        }
        
        // Determine the string to locate in the list then filter the list.
        if (!search.isEmpty()) {
            List toDescribe = new ArrayList();
            products.stream().filter((product) -> (product.getName().contains(search) 
                    || product.getDescription().contains(search))).forEach((product) -> {
                toDescribe.add(product);
            });
            newList = toDescribe;
        }
        // Determine the sort order of the file.
        if (!sort.isEmpty()) {
            switch (sort) {
                case "A":
                    products.sort(AToZByName);
                    break;
                case "I":
                    products.sort(ByID);
                    break;
                case "D":
                    products.sort(ByDate);
                    break;
            }
        }
        // Determine the file limit.
        if (limit != 0) {
            for (int i = newList.size() - 1; i >= limit; i--) {
               newList.remove(i);               
            }
        }
        
        // Section to check and build the format of the output file.
        if (format.matches("xml")) {
            output = buildXML(newList);
        } else if (format.matches("yaml")) {
            output = buildYAML(newList);
        } else if (format.matches("json")) {
            output = buildJSON(newList);
        } else if (format.matches("html")) {
            output = buildHTML(newList);
        } else {
            output = buildSQL(newList);
        }
        
        // Return the string to the main function.
        return output;
    }

    /**
     * The buildXML function to output a list in XML format
     * @param list A Product list of objects.
     * @return The XML string.
     */
    public static String buildXML(List<Product> list) {
        String xml = "";
        xml += "<products>\n";
        for (Product item : list) {
            xml += item.toXML();
        }
        xml += "</products>";
        return xml;
    }

    /**
     * The buildJSON function which builds a JSON formatted string.
     * @param list A Product list of objects.
     * @return The JSON string.
     */
    public static String buildJSON(List<Product> list) {
        String json = "";
        int i;
        json += "{ \"products\" : [ ";
        for (i = 0; i < list.size() - 1; i++) {
            json += list.get(i).toJSON() + ", ";
        }
        json += list.get(i).toJSON();
        json += " ] }";
        return json;
    }

    /**
     * The buildYAML function which builds a YAML formatted string.
     * @param list A Product list of objects.
     * @return The YAML string.
     */
    public static String buildYAML(List<Product> list) {
        String yaml = "";
        for (Product item : list) {
            yaml += item.toYAML();
        }
        yaml+="---";
        return yaml;
    }

    /**
     * The buildHTML function which builds a string in HTML format
     * @param list A Product list of objects.
     * @return The HTML string.
     */
    public static String buildHTML(List<Product> list) {
        String html = "";
        html += "<!DOCTYPE html>\n<html>\n<head>\n</head>\n<body>\n";
        for (Product item : list) {
            html += item.toHTML();
        }
        html += "</body>\n</html>";
        return html;
    }

    /**
     * The buildSQL function which builds a SQL string.
     * @param list A Product list of objects.
     * @return The SQL string.
     */
    public static String buildSQL(List<Product> list) {
        String sql = "";
        sql += "CREATE TABLE Products (id INT, name VARCHAR(50), description TEXT, "
                + "quantity INT, dateAdded DATE);\n";
        for (int i = 0; i < list.size(); i++) {
            sql += list.get(i).toSQL() + "\n";
        }
        return sql;
    }
    
    /**
     * Interface to sort the list by Name ascending.
     * Taken from the XML Serialization sample.
     */
    private static Comparator<Product> AToZByName = new Comparator<Product>() {
        @Override
        public int compare(Product o1, Product o2) {
            return (o1.getName().compareTo(o2.getName()));
        }
    };

    // Interface to sort the list by ID ascending.
    private static Comparator<Product> ByID = new Comparator<Product>() {
        @Override
        public int compare(Product o1, Product o2) {
            return (o1.getId() < o2.getId() ? 1 : 0);
        }
    };

    // Interface to sort the list by date ascending.
    private static Comparator<Product> ByDate = new Comparator<Product>() {
        @Override
        public int compare(Product o1, Product o2) {
            return (o1.getDateAdded().compareTo(o2.getDateAdded()));
        }
    };

}
