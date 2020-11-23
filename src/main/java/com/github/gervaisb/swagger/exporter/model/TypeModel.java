package com.github.gervaisb.swagger.exporter.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public interface TypeModel {

    static TypeModel.Simple simple(String name) {
        return new TypeModel.Simple(name);
    }

    static TypeModel.Complex complex(String ref) {
        return new TypeModel.Complex(ref);
    }

    String getName();

    boolean isSimple();

    class Simple implements TypeModel {
        private final String name;

        private Simple(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSimple() {
            return true;
        }
    }

    class Complex implements TypeModel {
        private final String ref;

        private Complex(String ref) {
            this.ref = ref;
        }

        public Schema resolve(OpenAPI specification) {
            return specification.getComponents().getSchemas().get(getName());
        }

        @Override
        public String getName() {
            return ref.substring(ref.lastIndexOf('/')+1);
        }

        @Override
        public boolean isSimple() {
            return false;
        }
    }
}
