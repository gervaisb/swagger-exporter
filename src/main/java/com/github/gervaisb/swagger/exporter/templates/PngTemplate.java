package com.github.gervaisb.swagger.exporter.templates;

import com.github.gervaisb.swagger.exporter.model.ClassesModel;
import net.sourceforge.plantuml.Run;

import java.io.*;
import java.util.Arrays;

public class PngTemplate implements Template {

    @Override
    public void merge(ClassesModel model, File output) throws IOException {
        File input = locateOrGenerateUml(model, output);
        convert(input, output);
    }

    private File locateOrGenerateUml(ClassesModel model, File relative) throws IOException {
        File uml = locateUml(relative);
        if (uml != null) {
            return uml;
        } else {
            return generateUml(model, relative);
        }
    }

    private File generateUml(ClassesModel model, File relative) throws IOException {
        File output = new File(relative.getParent(),
                relative.getName().substring(0, relative.getName().lastIndexOf('.')) + ".plantuml");
        output.deleteOnExit();
        PlantUmlTemplate template = new PlantUmlTemplate();
        template.merge(model, output);
        return output;
    }

    private File locateUml(File relative) throws FileNotFoundException {
        File[] files = relative.getParentFile().listFiles(new PlantUmlFilter());
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }

    private void convert(File plantUml, File output) throws IOException {
        try {
            Run.main(new String[]{"-tpng", "-o", output.getParent(), "-I", plantUml.getAbsolutePath()});
        } catch (InterruptedException e) {
            throw new IOException("Failed to export " + plantUml.getName().replace(".uml", "") + " as png. Note that 'dot' binary is required", e);
        }
    }

    private static class PlantUmlFilter implements FileFilter {
        private static final char[] HEADER = "@startuml".toCharArray();

        @Override
        public boolean accept(File pathname) {
            return isValidExtension(pathname.getName()) &&
                    isValidHeader(pathname);
        }

        private boolean isValidHeader(File file) {
            try (FileReader reader = new FileReader(file)) {
                char[] buffer = new char[HEADER.length];
                int reads = reader.read(buffer);
                return reads == HEADER.length &&
                        Arrays.equals(buffer, HEADER);
            } catch (IOException e) {
                return false;
            }
        }

        private boolean isValidExtension(String name) {
            return name.toLowerCase().endsWith("uml");
        }
    }
}
