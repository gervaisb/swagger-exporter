package com.github.gervaisb.swagger.exporter.model;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

public class ModelParser {
    public static class Builder {
        private Function<OpenAPI, List<ClassModel>> transformation = new OneToOneTransformation();

        public Builder mergingAttributes() {
            transformation = new MergingTransformation();
            return this;
        }

        public ModelParser build() {
            return new ModelParser(transformation);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    // ~ ----------------------------------------------------------------- ~ //

    private final Function<OpenAPI, List<ClassModel>> transformation;
    private final OpenAPIParser parser = new OpenAPIParser();

    private ModelParser(final Function<OpenAPI, List<ClassModel>> transformation) {
        this.transformation = transformation;
    }

    public ClassesModel parse(File input) {
        SwaggerParseResult result = parseSwagger(input.getAbsoluteFile());
        return transform(result.getOpenAPI());
    }

    private SwaggerParseResult parseSwagger(File input) {
        return parser.readLocation(input.getAbsolutePath(), emptyList(), new ParseOptions());
    }

    private ClassesModel transform(OpenAPI specification) {
        List<ClassModel> classes = transformation.apply(specification);
        return new ClassesModel(
                specification.getInfo().getTitle(),
                specification.getInfo().getVersion(),
                classes
        );
    }

    private static class OneToOneTransformation implements Function<OpenAPI, List<ClassModel>> {
        @Override
        public List<ClassModel> apply(OpenAPI specification) {
            return specification.getComponents().getSchemas().entrySet().stream()
                    .map(entry -> new ClassModel(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Merge "ComposedSchema" in one "Schema"
     */
    private static class MergingTransformation implements Function<OpenAPI, List<ClassModel>> {
        @Override
        public List<ClassModel> apply(OpenAPI specification) {
            Map<String, Schema> schemas = specification.getComponents().getSchemas();
            List<ClassModel> models = new ArrayList<>(schemas.size());
            for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
                Schema schema = maybeMerge(entry.getValue(), specification);
                models.add(new ClassModel(entry.getKey(), schema));
            }
            return models;
        }

        private Schema maybeMerge(Schema schema, OpenAPI specification) {
            if (schema instanceof ComposedSchema) {
                ComposedSchema composite = ((ComposedSchema) schema);
                List<Schema> components = composite.getAllOf();
                if (components != null) {
                    return merge(components, specification);
                } // else is a OneOf
            }
            return schema;
        }

        private Schema merge(List<Schema> schemas, OpenAPI spec) {
            Schema result = new Schema();
            result.setProperties(new HashMap<>());
            result.setRequired(new ArrayList<>());
            for (Schema schema : schemas) {
                Schema resolved = resolve(schema, spec);
                Schema merged = maybeMerge(resolved, spec);
                ofNullable(merged.getProperties()).orElse(emptyMap()).forEach(new BiConsumer<String, Schema>() {
                    @Override
                    public void accept(String key, Schema property) {
                        result.addProperties(key, property);
                    }
                });
                ofNullable(merged.getRequired()).orElse(emptyList()).forEach(new Consumer<String>() {
                    @Override
                    public void accept(String required) {
                        result.addRequiredItem(required);
                    }
                });
            }
            return result;
        }

        private Schema resolve(Schema possibleRef, OpenAPI spec) {
            Schema resolved = possibleRef;
            if (possibleRef.get$ref() != null) {
                TypeModel.Complex type = TypeModel.complex(possibleRef.get$ref());
                resolved = type.resolve(spec);
            }
            return resolved;
        }
    }

}
