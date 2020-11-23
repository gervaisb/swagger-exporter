package com.github.gervaisb.swagger.exporter.model;

import io.swagger.v3.oas.models.media.Schema;

import java.math.BigDecimal;

import static java.util.Optional.ofNullable;

public class FieldModel {
    private final Boolean required;
    private final Schema schema;
    private final String name;
    private final TypeModel type;

    public FieldModel(String name, Schema schema) {
        this(name, schema, null);
    }

    public FieldModel(String name, Schema schema, Boolean required) {
        this.required = required;
        this.schema = schema;
        this.name = name;
        this.type = resolveType();
    }

    private TypeModel resolveType() {
        if (schema.getType() != null) {
            return TypeModel.simple(schema.getType());
        } else if (schema.get$ref() != null) {
            return TypeModel.complex(schema.get$ref());
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getCardinality() {
        if ( isRequired() ) {
            return "1";
        }
        String max = ofNullable(schema.getMaximum())
                .map(BigDecimal::toString)
                .orElse("n");
        String min = ofNullable(schema.getMinimum())
                .map(BigDecimal::toString)
                .orElse("0");
        return min+ ".." +max;
    }

    public boolean isRequired() {
        return required!=null && required;
    }

    public TypeModel getType() {
        return type;
    }

    public String getTypeName() {
        return ofNullable(getType()).map(TypeModel::getName)
                .orElse("");
    }
}
