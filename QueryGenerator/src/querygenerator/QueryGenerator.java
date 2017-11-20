/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator;

import java.util.List;
import querygenerator.algorithm.MainAlgorithm;
import querygenerator.algorithm.Query;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.mapping.MappingModel;

/**
 *
 * @author daniellucredio
 */
public class QueryGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MappingModel mm = ModelSample2b.getModel();
       // System.out.println(mm.toString());
        
        MainAlgorithm ma = new MainAlgorithm(mm);

        Entity person = (Entity) mm.getERModel().findERElement("Person");
        Relationship registration = (Relationship) mm.getERModel().findERElement("Registration");
        Entity driversLicense = (Entity) mm.getERModel().findERElement("DriversLicense");

        List<Query> queries = ma.binaryJoin(person, registration, driversLicense);

        for (Query q : queries) {
            System.out.println("======================= Query =======================\n" + q.toString() + "\n");
        }

    }

}
