package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassesModel;
import com.github.gervaisb.swagger.exporter.model.ClassModel;
import com.github.gervaisb.swagger.exporter.model.FieldModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

class CsvTemplate implements Template {

    public void merge(ClassesModel model, File output) throws IOException {
        try (Writer writer = new FileWriter(output)) {
            writer.append("ClassName").append(';')
                    .append("FieldName").append(';')
                    .append("FieldType").append(';')
                    .append("Cardinality").append('\n');
            for (ClassModel clazz : model.getClasses()) {
                String className = clazz.getName();
                for (FieldModel field : clazz.getFields()) {
                    writer.append(className).append(';')
                            .append(field.getName()).append(';')
                            .append(field.getTypeName()).append(';')
                            .append(field.getCardinality()).append('\n');
                }
            }
            writer.flush();
        }
    }
}
