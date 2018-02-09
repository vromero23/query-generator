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


public class ModelSample4d {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();

        Entity person = new Entity("Person");
        person.addAttribute(new Attribute(person, "id", "int", true));
        person.addAttribute(new Attribute(person, "name", "string", false));
        person.addAttribute(new Attribute(person, "address", "string", false));
        erModel.addERElement(person);

        Entity driversLicense = new Entity("DriversLicense");
        driversLicense.addAttribute(new Attribute(driversLicense, "id", "int", true));
        driversLicense.addAttribute(new Attribute(driversLicense, "number", "int", false));
        driversLicense.addAttribute(new Attribute(driversLicense, "date", "date", false));
        erModel.addERElement(driversLicense);

        Relationship registration = new Relationship("Registration");
        registration.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        registration.addRelationshipEnd(new RelationshipEnd(driversLicense, Cardinality.One));
        registration.addAttribute(new Attribute(registration, "observation", "string",false));
        erModel.addERElement(registration);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypePerson = new DocumentType("DocTypePerson");
        docTypePerson.addERMapping(new ERMapping(person, true));
        docTypePerson.addField(new SimpleField(docTypePerson, "_id", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        
        DocumentType docTypeRegistration = new DocumentType("DocTypeRegistration");
        docTypeRegistration.addERMapping(new ERMapping(registration, false));
        docTypeRegistration.addField(new SimpleField(docTypeRegistration, "observation", "string", new FieldMapping(registration.getAttribute("observation"))));
        docTypePerson.addField(new EmbeddedField(docTypePerson, "data_Registration", docTypeRegistration));
        
        
        DocumentType docTypeDriversLicense= new DocumentType("DocTypeDriversLicense");
        docTypeDriversLicense.addERMapping(new ERMapping(driversLicense, false));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fDriversLicenseId", "int", new FieldMapping(driversLicense.getAttribute("id"))));
       // docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fNumber", "int", new FieldMapping(driversLicense.getAttribute("number"))));
        docTypeDriversLicense.addField(new SimpleField(docTypeDriversLicense, "fDate", "date", new FieldMapping(driversLicense.getAttribute("date"))));        
        docTypePerson.addField(new EmbeddedField(docTypePerson, "data_DriversLicense", docTypeDriversLicense));
        
        
        mongoSchema.addDocumentType(docTypePerson);
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
