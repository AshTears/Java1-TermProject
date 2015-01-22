package cpd3314.project;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *  Code borrowed from the XML Serialization sample by Len Payne.
 * @author Ashika Shallow
 */
public class Products {
    @ElementList(inline = true, entry = "product")
    private List<Product> products;

    public Products() {
    }

    public Products(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
