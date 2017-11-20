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
public class Relationship extends ERElement {
    List<RelationshipEnd> ends;

    public Relationship(String name) {
        super(name);
        this.ends = new ArrayList<>();
    }

    public List<RelationshipEnd> getRelationshipEnds() {
        return ends;
    }

    public void addRelationshipEnd(RelationshipEnd re) {
        this.ends.add(re);
    }

    @Override
    public String toString() {
        String ret = "";

        ret += name;
        ret += " (";

        for (int i = 0; i < ends.size(); i++) {
            ret += ends.get(i).entity.name;
            if (i < ends.size() - 1) {
                ret += ", ";
            }
        }

        ret += ") ";

        for (int i = 0; i < ends.size(); i++) {
            ret += ends.get(i).cardinality == Cardinality.Many ? "N" : "1";
            if (i < ends.size() - 1) {
                ret += ":";
            }
        }
        ret += "\n";
        ret += "{";
        ret += "\n";
        for (Attribute a : attributes) {
            ret += "   " + a.toString();
            ret += "\n";
        }
        ret += "}";
        ret += "\n";

        return ret;
    }
}
