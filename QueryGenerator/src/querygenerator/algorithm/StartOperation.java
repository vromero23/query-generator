/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

/**
 *
 * @author Vivi
 */
public class StartOperation extends Operation{

    public StartOperation(String text, ComputedEntity result) {
        super(text,result);
    }
      
    @Override
    public String generateOperation() {
         return "";
    }
    
    
}
