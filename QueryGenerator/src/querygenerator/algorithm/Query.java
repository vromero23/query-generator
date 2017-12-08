/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author daniellucredio
 */
public class Query {

    private final List<Operation> operations;

    public Query() {
        operations = new ArrayList<>();
    }

    public void addOperation(Operation op) {
        this.operations.add(op);
    }

    public void addOperations(List<Operation> ops) {
        this.operations.addAll(ops);
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public ComputedEntity getCopyOfLastComputedEntity() {
        if (!operations.isEmpty()) {
            return ComputedEntity.createCopy(operations.get(operations.size() - 1).getResult());
        }
        return null;
    }

    @Override
    public String toString() {
        String ret = "";

        for (Operation o : operations) {
            ret += o.toString() + "\n";
        }

        return ret;
    }

    // TODO: Modificar equals para comparar as operações adequadamente
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Query)) {
            return false;
        }
        Query otherQ = (Query)o;
        if(this.operations.size() != otherQ.operations.size()) {
            return false;
        }
        for(int i=0;i<operations.size();i++) {
            Operation o1 = operations.get(i);
            Operation o2 = otherQ.operations.get(i);
            if(!o1.equals(o2)) {
                return false;
            }
        }
        return true;
    }
    
    public String generateQuery ()
    {
        String ret = "";
        for (Operation op: operations){
            ret += op.generateOperation() + "\n";
        }
        return ret;
    }
    

}
