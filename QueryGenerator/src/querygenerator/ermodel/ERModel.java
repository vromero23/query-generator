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
public class ERModel {
    List<ERElement> erElements;

    public ERModel() {
        this.erElements = new ArrayList<>();
    }
    
    public void addERElement(ERElement e) {
        this.erElements.add(e);
    }

    public List<ERElement> getERElements() {
        return erElements;
    }
    
    
    
    public ERElement findERElement(String name) {
        for(ERElement e:erElements) {
            if(e.name.equals(name)) {
                return e;
            }
        }
        return null;
    }
    
    public List<Relationship> findAllRelationships(Entity e) {
        List<Relationship> ret = new ArrayList<>();
        for(ERElement erElement: erElements) {
            if(erElement instanceof Relationship) {
                Relationship r = (Relationship)erElement;
                for(RelationshipEnd re : r.ends) {
                    if(re.entity == e) {
                        ret.add(r);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "";
        for(ERElement e:erElements) {
            ret += e.toString();
            ret += "\n";
        }
        return ret;
    }

}
