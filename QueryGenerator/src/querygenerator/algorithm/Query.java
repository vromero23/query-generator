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

}
