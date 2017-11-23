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

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImpossibleOperation)) {
            return false;
        }
        ImpossibleOperation io = (ImpossibleOperation)obj;
        return this.text.equals(io.text);
    }

    @Override
    public String generateOperation() {
        return  toString();
    }

    
}
