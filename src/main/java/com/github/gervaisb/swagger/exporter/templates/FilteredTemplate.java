package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassModel;
import com.github.gervaisb.swagger.exporter.model.ClassesModel;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

public class FilteredTemplate implements Template {

    private final Predicate<ClassModel> filter;
    private final Template template;

    public FilteredTemplate(Template template, Predicate<ClassModel> filter) {
        this.template = template;
        this.filter = filter;
    }

    @Override
    public void merge(ClassesModel model, File output) throws IOException {
        template.merge(new FilteredModel(model), output);
    }

    private class FilteredModel extends ClassesModel {
        private FilteredModel(ClassesModel model) {
            super(model.getName(), model.getVersion(),
                    model.getClasses().stream().filter(filter).collect(toSet()));
        }
    }
}
