package com.github.gervaisb.swagger.exporter;

import com.github.gervaisb.swagger.exporter.model.ClassModel;
import com.github.gervaisb.swagger.exporter.model.ModelParser;
import com.github.gervaisb.swagger.exporter.templates.CompositeTemplate;
import com.github.gervaisb.swagger.exporter.templates.FilteredTemplate;
import com.github.gervaisb.swagger.exporter.templates.Template;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SwaggerExporter {

    private final List<String> formats;
    private ModelParser parser = ModelParser.builder()
            .build();

    public SwaggerExporter(List<String> formats) {
        this.formats = new ArrayList<>(formats);
    }

    public void mergeCompositions(boolean shouldMerge) {
        if ( shouldMerge ) {
            parser = ModelParser.builder()
                    .mergingAttributes()
                    .build();
        } else {
            parser = ModelParser.builder()
                    .build();
        }
    }

    public void export(File input, Predicate<ClassModel> filter) throws IOException {

        String basename = input.getName().substring(0, input.getName().lastIndexOf('.'));
        File output = input.getParentFile();

        Template template = new FilteredTemplate(
                new CompositeTemplate(formats, basename), filter);

        template.merge(parser.parse(input), output);
    }

}
