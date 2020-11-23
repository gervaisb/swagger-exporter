package com.github.gervaisb.swagger.exporter.model;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class ClassModel {
    private final Schema schema;
    private final String name;

    ClassModel(String name, Schema schema) {
        this.name = name;
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return schema.getDescription();
    }

    public List<FieldModel> getFields() {
        return getProperties().entrySet().stream()
                .map(entry ->
                        new FieldModel(entry.getKey(), entry.getValue(), isRequired(entry.getKey()))
                ).collect(toList());
    }

    public String getSuperClass() {
        if ( !(schema instanceof ComposedSchema) )
            return null;

        ComposedSchema composed = ((ComposedSchema) schema);
        return ofNullable(composed.getAllOf()).map(List::stream)
                .orElse(Stream.empty())
                .map(Schema::get$ref)
                .filter(Objects::nonNull)
                .findFirst()
                .map(ref -> TypeModel.complex(ref).getName())
                .orElse(null);
    }

    private Boolean isRequired(String fieldName) {
        return getRequiredProperties().contains(fieldName);
    }

    private Set<String> getRequiredProperties() {
        if ( schema instanceof ComposedSchema ) {
            return getComposedRequiredProperties();
        } else if ( schema.getRequired()!=null ) {
            return new HashSet<>(schema.getRequired());
        } else
            return Collections.emptySet();
        }

    private Set<String> getComposedRequiredProperties() {
        ComposedSchema composed = (ComposedSchema) schema;
        if (composed.getAllOf() == null)
            return Collections.emptySet();

        Set<String> requirements = new HashSet<>();
        for (Schema component : composed.getAllOf()) {
            if (component.getRequired()!=null) {
                requirements.addAll(component.getRequired());
            }
        }
        return requirements;
    }

    private Map<String, Schema> getProperties() {
        if (schema instanceof ComposedSchema) {
            return getComposedProperties();
        } else if (schema.getProperties() != null) {
            return schema.getProperties();
        } else {
            // FIXME Handle enums
            return Collections.emptyMap();
        }
    }

    private Map<String, Schema> getComposedProperties() {
        ComposedSchema composed = (ComposedSchema) schema;
        if (composed.getAllOf() == null)
            return Collections.emptyMap();

        Map<String, Schema> properties = new HashMap<>();
        for (Schema component : composed.getAllOf()) {
            if (component instanceof ObjectSchema) {
                properties.putAll(component.getProperties());
            }
        }
        return properties;
    }
}
