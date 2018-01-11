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


public class ModelSampleOneToMany {

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
        
        Entity garage = new Entity("Garage");
        garage.addAttribute(new Attribute(garage, "id", "int", true));
        garage.addAttribute(new Attribute(garage, "name", "string", false));
        garage.addAttribute(new Attribute(garage, "address", "string", false));
        erModel.addERElement(garage);

        Relationship drives = new Relationship("Drives");
        drives.addRelationshipEnd(new RelationshipEnd(person, Cardinality.One));
        drives.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        erModel.addERElement(drives);
        
        Relationship repairs = new Relationship("Repairs");
        repairs.addRelationshipEnd(new RelationshipEnd(car, Cardinality.Many));
        repairs.addRelationshipEnd(new RelationshipEnd(garage, Cardinality.Many));
        repairs.addAttribute(new Attribute(repairs, "date", "date",false));
        repairs.addAttribute(new Attribute(repairs, "fix", "string",false));
        erModel.addERElement(repairs);

        MongoSchema mongoSchema = new MongoSchema();

        DocumentType docTypePerson = new DocumentType("DocTypePerson");
        docTypePerson.addERMapping(new ERMapping(person, true));
        docTypePerson.addField(new SimpleField(docTypePerson, "_id", "int", new FieldMapping(person.getAttribute("id"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fName", "string", new FieldMapping(person.getAttribute("name"))));
        docTypePerson.addField(new SimpleField(docTypePerson, "fAddress", "string", new FieldMapping(person.getAttribute("address"))));
        DocumentType docTypeCar2 = new DocumentType("DocTypeCar2");
        docTypeCar2.addERMapping(new ERMapping(car, true));
        docTypeCar2.addField(new SimpleField(docTypeCar2, "fCarId", "int", new FieldMapping(car.getAttribute("id"))));
        docTypeCar2.addField(new SimpleField(docTypeCar2, "fPlate", "string", new FieldMapping(car.getAttribute("plate"))));
        docTypeCar2.addField(new SimpleField(docTypeCar2, "fColor", "string", new FieldMapping(car.getAttribute("color"))));
        docTypePerson.addField(new EmbeddedField(docTypePerson, "data_Car", docTypeCar2));
        
        mongoSchema.addDocumentType(docTypePerson);
         
        DocumentType docTypeCar = new DocumentType("DocTypeCar");
        docTypeCar.addERMapping(new ERMapping(car, true));
        docTypeCar.addField(new SimpleField(docTypeCar, "_id", "int", new FieldMapping(car.getAttribute("id"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fPlate", "string", new FieldMapping(car.getAttribute("plate"))));
        docTypeCar.addField(new SimpleField(docTypeCar, "fColor", "string", new FieldMapping(car.getAttribute("color"))));
        mongoSchema.addDocumentType(docTypeCar);
        
        
        DocumentType docTypeDrives = new DocumentType("DocTypeDrives");
        docTypeDrives.addERMapping(new ERMapping(drives, true));
       // docTypeDrives.addERMapping(new ERMapping(person, false));
        //docTypeDrives.addERMapping(new ERMapping(car, false));
       // docTypeDrives.addField(new SimpleField(docTypeDrives, "fPersonId", "int", new FieldMapping(person.getAttribute("id"))));
       // docTypeDrives.addField(new SimpleField(docTypeDrives, "fCarId", "int", new FieldMapping(car.getAttribute("id"))));
        mongoSchema.addDocumentType(docTypeDrives);
        
        
        List<String> violations = mongoSchema.validate();
        if(violations.size() > 0) {
            for(String v:violations) {
                System.out.println(v);
            }
        }

        return new MappingModel(erModel, mongoSchema);
    }
}
