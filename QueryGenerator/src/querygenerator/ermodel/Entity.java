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
public class Entity extends ERElement {

    public Entity(String name) {
        super(name);
    }

    @Override
    public String toString() {
        String ret = "";
        ret = name + "\n";
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
