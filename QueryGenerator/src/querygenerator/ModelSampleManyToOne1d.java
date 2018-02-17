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


public class ModelSampleManyToOne1d {

    public static MappingModel getModel() {
        ERModel erModel = new ERModel();

        Entity person = new Entity("Person");
        person.addAttribute(new Attribute(person, "id", "int", true));
        person.addAttribute(new Attribute(person, "name", "string", false));
        person.addAttribute(new Attribute(person, "address", "string", false));
        erModel.addERElement(person);

        Entity car = new Entity("Car");
        car.addAttribute(new Attribute(car, "id", "int", true));
        car.addAttribute(new Attribute(car, "plate", "string", false));
        car.addAttribute(new Attribute(car, "color", "string", false));
        erModel.addERElement(car);

        Relationship drives = new Relationship("Drives");
        drives.addAttribute(new Attribute(drives, "observation", "string", false));
        drives.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        drives.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        erModel.addERElement(drives);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypeCar = new DocumentType("DocTypeCar");
        docTypeCar.addERMapping(new ERMapping(car, true));
        docTypeCar.addERMapping(new ERMapping(drives, true));
        docTypeCar.addERMapping(new ERMapping(person, false));
        docTypeCar.addField(new SimpleField(docTypeCar, "_id", "int", new FieldMapping(car.getAttribute("id"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fPlate", "string", new FieldMapping(car.getAttribute("plate"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fColor", "string", new FieldMapping(car.getAttribute("color"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fObservation", "string", new FieldMapping(drives.getAttribute("observation"))));       
        
        DocumentType docTypePerson2 = new DocumentType("DocTypePerson2");
        docTypePerson2.addERMapping(new ERMapping(person, true));
        docTypePerson2.addField(new SimpleField(docTypePerson2, "fPersonId", "int", new FieldMapping(person.getAttribute("id"))));
        //docTypePerson2.addField(new SimpleField(docTypePerson2, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        //docTypePerson2.addField(new SimpleField(docTypePerson2, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
       
        docTypeCar.addField(new EmbeddedField(docTypeCar, "data_Person", docTypePerson2));
        mongoSchema.addDocumentType(docTypeCar);
        
        DocumentType docTypePerson = new DocumentType("DocTypePerson");
        docTypePerson.addERMapping(new ERMapping(person, true));
        docTypePerson.addField(new SimpleField(docTypePerson, "_id", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        
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
