package com.github.gervaisb.swagger.exporter.model;

import java.util.*;

public class ClassesModel {

    private final Set<ClassModel> classes = new TreeSet<>((left, right) ->
            left.getName().compareToIgnoreCase(right.getName())
    );
    private final String version;
    private final String name;

    public ClassesModel(String name, String version, Collection<ClassModel> classes) {
        this.version = version;
        this.name = name;
        this.classes.addAll(classes);
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Set<ClassModel> getClasses() {
        return Collections.unmodifiableSet(classes);
    }
}
