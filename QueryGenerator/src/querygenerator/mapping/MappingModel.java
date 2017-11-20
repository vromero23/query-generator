/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querygenerator.mapping;

import java.util.List;
import querygenerator.ermodel.ERModel;
import querygenerator.mongoschema.MongoSchema;

/**
 *
 * @author daniellucredio
 */
public class MappingModel {

    ERModel erModel;
    MongoSchema mongoSchema;

    public MappingModel(ERModel erModel, MongoSchema mongoSchema) {
        this.erModel = erModel;
        this.mongoSchema = mongoSchema;
    }

    public ERModel getERModel() {
        return erModel;
    }

    public MongoSchema getMongoSchema() {
        return mongoSchema;
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "================";
        ret += "\n";
        ret += "==  ER Model  ==";
        ret += "\n";
        ret += "================";
        ret += "\n\n";

        ret += erModel.toString();

        ret += "====================";
        ret += "\n";
        ret += "==  Mongo Schema  ==";
        ret += "\n";
        ret += "====================";
        ret += "\n\n";

        ret += mongoSchema.toString();

        List<String> violations = mongoSchema.validate();
        for (String s : violations) {
            ret += "Aviso: " + s + "\n";
        }

        return ret;
    }
}
