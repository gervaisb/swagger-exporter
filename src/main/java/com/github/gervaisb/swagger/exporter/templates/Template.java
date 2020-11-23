package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassesModel;

import java.io.File;
import java.io.IOException;

public interface Template {

    void merge(ClassesModel model, File output) throws IOException;
}
