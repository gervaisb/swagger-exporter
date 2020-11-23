package com.github.gervaisb.swagger.exporter.cli;

import com.github.gervaisb.swagger.exporter.SwaggerExporter;
import com.github.gervaisb.swagger.exporter.model.ClassModel;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;


/**
 * <b>Usage:</b>
 * <pre>
 * swagger-export &lt;input> [-exclude &lt;prefixes>] [-merged] -as &lt;formats>
 * </pre>
 */
public class Runner  {
    public static void main(String[] args) {
        Arguments arguments = Arguments.parse(args);
        SwaggerExporter exporter = new SwaggerExporter(arguments.formats);
        exporter.mergeCompositions(arguments.merged);

        try {
            exporter.export(arguments.input, new ClassNameFilter(arguments.exclude));

            System.out.println("Successfully exported model as "+
                    join(arguments.formats)+
                    " in file://" + arguments.input.getParent());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Failed to export model from "+arguments.input+".");
            e.printStackTrace(System.out);
            System.exit(-1);
        }
    }

    private static String join(List<String> strings) {
        if ( strings.size()<2 ) {
            return strings.get(0);
        }

        StringBuilder csv = new StringBuilder(String.join(", ", strings));
        int index = csv.lastIndexOf(",");
        return csv.replace(index, index+1, " and")
                .toString();
    }

    private static class ClassNameFilter implements Predicate<ClassModel> {
        private final Set<String> prefixes;
        private ClassNameFilter(List<String> prefixes) {
            this.prefixes = prefixes.stream()
                    .map(String::toLowerCase)
                    .collect(toSet());
        }

        @Override
        public boolean test(ClassModel model) {
            String name = model.getName().toLowerCase();
            for (String prefix : prefixes) {
                if ( name.startsWith(prefix) ) {
                    return false;
                }
            }
            return true;
        }
    }
}
