/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.ermodel;

/**
 *
 * @author daniellucredio
 */
public class Attribute {
    String name, type;
    boolean identifier;
    ERElement parent;

    public Attribute(ERElement parent, String name, String type, boolean identifier) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.identifier = identifier;
    }

    public ERElement getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }

    public boolean isIdentifier() {
        return identifier;
    }
    
    
    
    @Override
    public String toString() {
        return name + " : " + type;
    }    
}
