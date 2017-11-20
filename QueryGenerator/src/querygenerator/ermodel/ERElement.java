/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.ermodel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author daniellucredio
 */
public abstract class ERElement {
    String name;
    List<Attribute> attributes;

    public ERElement(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    public void addAttribute(Attribute attr) {
        if(attr.parent != this) {
            throw new RuntimeException("O pai do atributo "+attr.name+" deve ser "+name);
        }
                
        this.attributes.add(attr);
    }
    
    public Attribute getAttribute(String attrName) {
        for(Attribute a: attributes) {
            if(a.name.equals(attrName)) {
                return a;
            }
        }
        throw new RuntimeException("Atributo "+attrName+" não existe na entidade "+name);
    }    
}
