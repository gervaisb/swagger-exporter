package com.github.gervaisb.swagger.exporter.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

class Arguments {

    // swagger-export &lt;input> [-exclude &lt;prefixes>] -merged -as &lt;formats>
    static Arguments parse(String[] args) {
        if ( args.length<3 ) {
            throw new IllegalArgumentException("input file and export format required");
        }

        File input = new File(args[0]);
        boolean merged = false;
        List<String> exclude = new ArrayList<>(0);
        List<String> formats = new ArrayList<>(0);
        for (int i = 1; i < args.length; i++) {
            if ( "-exclude".equals(args[i]) ) {
                exclude = split(args[i+1]);
            } else if ( "-as".equals(args[i]) ) {
                formats = split(args[i+1]);
            } else if ( "-merged".equals(args[i])) {
                merged = true;
            }
        }
        return new Arguments(input, exclude, formats, merged);
    }

    private static List<String> split(String csv) {
        return unmodifiableList(
                Arrays.stream(csv.split(","))
                    .map(item -> item.trim().toLowerCase())
                    .collect(Collectors.toList())
        );
    }

    // ~ ----------------------------------------------------------------- ~ //

    final List<String> exclude;
    final List<String> formats;
    final File input;
    final boolean merged;

    private Arguments(File input, List<String> exclude, List<String> formats, boolean merged) {
        this.exclude = exclude;
        this.formats = formats;
        this.input = input;
        this.merged = merged;
    }

}
