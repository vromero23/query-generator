/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator;

import java.util.ArrayList;
import java.util.List;
import querygenerator.algorithm.MainAlgorithm;
import querygenerator.algorithm.Query;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.mapping.MappingModel;

/**
 *
 * @author daniellucredio
 */
public class QueryGenerator2a {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MappingModel mm = ModelSample1a.getModel();
        System.out.println(mm.toString());

        MainAlgorithm ma = new MainAlgorithm(mm);

        Entity person = (Entity) mm.getERModel().findERElement("Person");
        Relationship registration = (Relationship) mm.getERModel().findERElement("Registration");
        Entity driversLicense = (Entity) mm.getERModel().findERElement("DriversLicense");

        List<Attribute> queryAttributes = new ArrayList<>();
        queryAttributes.add(person.getAttribute("name"));
        queryAttributes.add(registration.getAttribute("observation"));
        queryAttributes.add(driversLicense.getAttribute("number"));

        
 

        List<Query> queries = ma.binaryJoin(person, registration, driversLicense, queryAttributes);

        for (Query q : queries) {
            System.out.println("======================= Query =======================\n" + q.toString() + "\n");
        //}
       // for (Query q : queries) {
            System.out.println("======================= Query Mongo =======================\n" + q.generateQuery() + "\n");
        }
        

    }

}
