/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

/**
 *
 * @author daniellucredio
 */
public class ImpossibleOperation extends Operation {

    public ImpossibleOperation(String text) {
        super(text, null);
    }

    @Override
    public String toString() {
        return "(impossible) " + text;
    }

}
