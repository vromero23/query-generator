/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator;

import java.util.List;
import querygenerator.ermodel.Attribute;
import querygenerator.ermodel.Cardinality;
import querygenerator.ermodel.ERModel;
import querygenerator.ermodel.Entity;
import querygenerator.ermodel.Relationship;
import querygenerator.ermodel.RelationshipEnd;
import querygenerator.mapping.MappingModel;
import querygenerator.mongoschema.DocumentType;
import querygenerator.mongoschema.ERMapping;
import querygenerator.mongoschema.EmbeddedField;
import querygenerator.mongoschema.FieldMapping;
import querygenerator.mongoschema.MongoSchema;
import querygenerator.mongoschema.SimpleField;

public class ModelSampleFunDep5 {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();

        Entity funcionario = new Entity("Funcionario");
        funcionario.addAttribute(new Attribute(funcionario, "id", "int", true));
        funcionario.addAttribute(new Attribute(funcionario, "cpf", "string", false));
        funcionario.addAttribute(new Attribute(funcionario, "nome_funcionario", "string", false));
        funcionario.addAttribute(new Attribute(funcionario, "sexo", "string", false));
        erModel.addERElement(funcionario);

        Entity departamento = new Entity("Departamento");
        departamento.addAttribute(new Attribute(departamento, "id", "int", true));
        departamento.addAttribute(new Attribute(departamento, "numero_dep", "string", false));
        departamento.addAttribute(new Attribute(departamento, "nome_departamento", "string", false));
        erModel.addERElement(departamento);

        Relationship gerencia = new Relationship("Gerencia");
        gerencia.addRelationshipEnd(new RelationshipEnd(funcionario, Cardinality.One));
        gerencia.addRelationshipEnd(new RelationshipEnd(departamento, Cardinality.One));
        gerencia.addAttribute(new Attribute(gerencia, "data_inicio", "string",false));
        erModel.addERElement(gerencia);

        MongoSchema mongoSchema = new MongoSchema();

        
        DocumentType docTypeDepartamento = new DocumentType("DocTypeDepartamento");
        docTypeDepartamento.addERMapping(new ERMapping(departamento, true));
        docTypeDepartamento.addERMapping(new ERMapping(funcionario, true));
        docTypeDepartamento.addERMapping(new ERMapping(gerencia, true));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "_id", "int", new FieldMapping(departamento.getAttribute("id"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fNumero_dep", "string", new FieldMapping(departamento.getAttribute("numero_dep"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fNome_departamento", "string", new FieldMapping(departamento.getAttribute("nome_departamento"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fFuncionarioId", "int", new FieldMapping(funcionario.getAttribute("id"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fCpf", "string", new FieldMapping(funcionario.getAttribute("cpf"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fNome_funcionario", "string", new FieldMapping(funcionario.getAttribute("nome_funcionario"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "fSexo", "string", new FieldMapping(funcionario.getAttribute("sexo"))));
        docTypeDepartamento.addField(new SimpleField(docTypeDepartamento, "data_inicio", "string", new FieldMapping(gerencia.getAttribute("data_inicio"))));

        mongoSchema.addDocumentType(docTypeDepartamento);

        DocumentType docTypeGerencia  = new DocumentType("DocTypeGerencia ");
        docTypeGerencia.addERMapping(new ERMapping(gerencia, true));
        docTypeGerencia.addERMapping(new ERMapping(funcionario, false));
        docTypeGerencia.addERMapping(new ERMapping(departamento, false));
        docTypeGerencia.addField(new SimpleField(docTypeGerencia, "_id", "int", null));
        docTypeGerencia.addField(new SimpleField(docTypeGerencia, "data_inicio", "string", new FieldMapping(gerencia.getAttribute("data_inicio"))));
        docTypeGerencia.addField(new SimpleField(docTypeGerencia, "fDepartamentoId", "int", new FieldMapping(departamento.getAttribute("id"))));
         docTypeGerencia.addField(new SimpleField(docTypeGerencia, "fFuncionarioId", "int", new FieldMapping(funcionario.getAttribute("id"))));
        mongoSchema.addDocumentType(docTypeGerencia);
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
