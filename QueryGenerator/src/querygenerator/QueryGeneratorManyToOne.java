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
public class QueryGeneratorManyToOne {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MappingModel mm = ModelSampleManyToOne1b.getModel();
        System.out.println(mm.toString());

        MainAlgorithm ma = new MainAlgorithm(mm);

        Entity car = (Entity) mm.getERModel().findERElement("Car");
        Relationship drives = (Relationship) mm.getERModel().findERElement("Drives");
        Entity person = (Entity) mm.getERModel().findERElement("Person");

        List<Attribute> queryAttributes = new ArrayList<>();
        queryAttributes.add(person.getAttribute("name"));
        queryAttributes.add(car.getAttribute("plate"));
        queryAttributes.add(car.getAttribute("color"));

        List<Query> queries = ma.binaryJoin(car, drives, person, queryAttributes);

        for (Query q : queries) {
            System.out.println("======================= Query =======================\n" + q.toString() + "\n");
            //}
            // for (Query q : queries) {
            System.out.println("======================= Query Mongo =======================\n" + q.generateQuery() + "\n");
        }

    }

}
