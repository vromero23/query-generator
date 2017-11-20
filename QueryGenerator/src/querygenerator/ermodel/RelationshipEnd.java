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
public class RelationshipEnd {
    Entity entity;
    Cardinality cardinality;

    public RelationshipEnd(Entity entity, Cardinality cardinality) {
        this.entity = entity;
        this.cardinality = cardinality;
    }

    public Entity getEntity() {
        return entity;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
    
    
}
