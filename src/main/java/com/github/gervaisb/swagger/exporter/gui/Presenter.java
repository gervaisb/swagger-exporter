package com.github.gervaisb.swagger.exporter.gui;

import com.github.gervaisb.swagger.exporter.SwaggerExporter;
import com.github.gervaisb.swagger.exporter.model.ClassModel;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Presenter {


    private final View view;

    Presenter(View view) {
        this.view = view;
    }

    private File getSpecificationFile() {
        String url = view.getSpecificationUrl();
        if (url.startsWith("file://")) {
            return new File(url.replaceFirst("file://", ""));
        } else {
            throw new UnsupportedOperationException("Getting remote spec is not yet supported");
        }
    }

    void onExport() {
        view.setProgressStarted();
        (new SwingWorker<File[], Void>(){

            @Override
            protected File[] doInBackground() throws Exception {
                File specification = getSpecificationFile();
                SwaggerExporter exporter = new SwaggerExporter(view.getOutputFormats());
                exporter.export(specification, getExcludedClassesFilter());
                return specification.getParentFile().listFiles(new FilenameFilter() {
                    final String prefix = specification.getName().substring(0, specification.getName().lastIndexOf('.'));
                    @Override
                    public boolean accept(File dir, String name) {
                        System.out.println(name+" - "+prefix);
                        return name.startsWith(prefix);
                    }
                });
            }

            @Override
            protected void done() {
                try {
                    view.setResults(get());
                    view.setProgressFinished();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).execute();
    }

    private Predicate<ClassModel> getExcludedClassesFilter() {
        Set<String> excluded = view.getExcludedClasses().stream().map(String::toLowerCase)
                .collect(Collectors.toSet());
        return classModel -> {
            String className = classModel.getName().toLowerCase();
            return excluded.contains(className);
        };
    }
}
