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
public abstract class Operation {

    protected final String text;
    protected final ComputedEntity result;

    public Operation(String text, ComputedEntity result) {
        this.text = text;
        this.result = result;
    }

    public String getText() {
        return text;
    }

    public ComputedEntity getResult() {
        return result;
    }
    
    
}
