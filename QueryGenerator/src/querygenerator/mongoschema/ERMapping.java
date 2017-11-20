/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mongoschema;

import querygenerator.ermodel.ERElement;

/**
 *
 * @author daniellucredio
 */
public class ERMapping {

    ERElement erElement;
    boolean main;

    public ERMapping(ERElement erElement, boolean main) {
        this.erElement = erElement;
        this.main = main;
    }

    public ERElement getERElement() {
        return erElement;
    }

    public boolean isMain() {
        return main;
    }

    @Override
    public String toString() {
        return erElement.getName() + " (main=" + main + ")";
    }

}
