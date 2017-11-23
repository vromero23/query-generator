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
public class QueryGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MappingModel mm = ModelSampleFunDep1.getModel();
        System.out.println(mm.toString());

        MainAlgorithm ma = new MainAlgorithm(mm);

        Entity funcionario = (Entity) mm.getERModel().findERElement("Funcionario");
        Relationship gerencia = (Relationship) mm.getERModel().findERElement("Gerencia");
        Entity departamento = (Entity) mm.getERModel().findERElement("Departamento");

        List<Attribute> queryAttributes = new ArrayList<>();
        //queryAttributes.add(funcionario.getAttribute("id"));
        //queryAttributes.add(funcionario.getAttribute("cpf"));
        queryAttributes.add(funcionario.getAttribute("nome_funcionario"));
       // queryAttributes.add(funcionario.getAttribute("sexo"));
        queryAttributes.add(gerencia.getAttribute("data_inicio"));
        //queryAttributes.add(departamento.getAttribute("id"));
        queryAttributes.add(departamento.getAttribute("numero_dep"));
        queryAttributes.add(departamento.getAttribute("nome_departamento"));
        
 

        List<Query> queries = ma.binaryJoin(funcionario, gerencia, departamento, queryAttributes);

        for (Query q : queries) {
            System.out.println("======================= Query =======================\n" + q.toString() + "\n");
        }
        for (Query q : queries) {
            System.out.println("======================= Query Mongo =======================\n" + q.generateQuery() + "\n");
        }
        

    }

}
