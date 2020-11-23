package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassesModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class CompositeTemplate implements Template {
    private static final Map<String, Supplier<Template>> FORMATS_BY_EXTENSION = new HashMap<String, Supplier<Template>>(2){{
        put("uml", PlantUmlTemplate::new);
        put("puml", PlantUmlTemplate::new);
        put("plantuml", PlantUmlTemplate::new);
        put("csv", CsvTemplate::new);
        put("png", PngTemplate::new);
    }};

    private final List<Format> formats;
    private final String basename;

    public CompositeTemplate(List<String> formats, String basename) {
        this.formats = formats.stream()
                .map(this::asTemplate)
                .collect(toList());
        this.basename = basename;
    }

    private Format asTemplate(String extension) {
        if ( !FORMATS_BY_EXTENSION.containsKey(extension) ) {
            throw new IllegalArgumentException("Unsupported format \""+extension+"\" (Supported: "+String.join(", ", FORMATS_BY_EXTENSION.keySet())+")");
        }

        Supplier<Template> supplier = FORMATS_BY_EXTENSION.get(extension);
        return new Format(extension, supplier.get());
    }


    @Override
    public void merge(ClassesModel model, File directory) throws IOException {
        for (Format format : formats) {
            format.write(model, directory);
        }
    }

    private class Format {
        private Template template;
        private String extension;
        private Format(String extension, Template template) {
            this.extension = extension;
            this.template = template;
        }
        private void write(ClassesModel model, File directory) throws IOException {
            File output = new File(directory, basename+'.'+extension);
            template.merge(model, output);
        }
    }
}
