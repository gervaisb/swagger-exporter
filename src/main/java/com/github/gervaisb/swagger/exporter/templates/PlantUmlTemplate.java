package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassModel;
import com.github.gervaisb.swagger.exporter.model.ClassesModel;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.gervaisb.swagger.exporter.model.FieldModel;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class PlantUmlTemplate implements Template {
    private final Mustache mustache;
    private final Set<String> excludedRelations = new HashSet<>();

    PlantUmlTemplate(){
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("/uml.mustache"))) {
            MustacheFactory mf = new DefaultMustacheFactory();
            mustache = mf.compile(reader, "uml.mustache");
        } catch (IOException cause) {
            ExceptionInInitializerError error = new ExceptionInInitializerError("Cannot load uml template");
            error.initCause(cause);
            throw error;
        }
        excludedRelations.add("(.*)\\.ref");

    }

    @Override
    public void merge(ClassesModel model, File output) throws IOException {
        try (Writer writer = new FileWriter(output)){
            mustache.execute(writer, new UmlModel(model));
            writer.flush();
        }
    }

    private boolean isSimpleField(FieldModel field) {
        return  field.getType()==null ||
                field.getType().isSimple();
    }

    private boolean isRelation(FieldModel field, ClassModel owner) {
        return  !isSimpleField(field) &&
                !isExcludedRelation(owner.getName()+"."+field.getName());
    }

    private boolean isExcludedRelation(String relation) {
        for (String pattern : excludedRelations) {
            if ( relation.matches(pattern) ) {
                return true;
            }
        }
        return false;
    }

    private class UmlModel {
        private ClassesModel model;
        private UmlModel(ClassesModel model) {
            this.model = model;
        }

        public List<UmlClassModel> getClasses() {
            return model.getClasses().stream()
                    .map(UmlClassModel::new)
                    .collect(toList());
        }

        public String getModelTitle() {
            return model.getName();
        }

        public String getModelVersion() {
            return model.getVersion();
        }

    }

    private class UmlClassModel {
        private ClassModel model;
        private UmlClassModel(ClassModel model) {
            this.model = model;
        }

        public String getKind() {
            return "class";
        }

        public String getName() {
            return model.getName();
        }

        public List<FieldModel> getFields() {
            return model.getFields().stream()
                    .filter(PlantUmlTemplate.this::isSimpleField)
                    .collect(toList());
        }

        public List<RelationModel> getRelations() {
            return model.getFields().stream()
                    .filter(field -> isRelation(field, model))
                    .map(field -> new RelationModel(model, field))
                    .collect(toList());
        }

        public String getSuperClass() {
            return model.getSuperClass();
        }
    }

    private static class RelationModel {
        private final ClassModel owner;
        private final FieldModel child;
        private RelationModel(ClassModel owner, FieldModel child) {
            this.owner = owner;
            this.child = child;
        }

        public String getChildName() {
            return child.getName();
        }

        public String getChildTypeName() {
            return child.getTypeName();
        }

        public String getOwnerTypeName() {
            return owner.getName();
        }

        public String getOwnerCardinality() {
            return "1";
        }

        public String getChildCardinality() {
            return child.getCardinality();
        }
    }
}
