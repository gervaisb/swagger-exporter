package com.github.gervaisb.swagger.exporter.gui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Set;

public class Model {

    public interface OptionChangesListener extends EventListener {}

    private final EventListenerList listeners = new EventListenerList();

    public void addOptionChangesListener(OptionChangesListener lstnr) {
        listeners.add(OptionChangesListener.class, lstnr);
    }

    String getSwaggerFile() {
        return null;
    }

    Set<String> getExcludedClasses() {
        return Collections.emptySet();
    }

    Set<String> getOutputFormats() {
        return Collections.emptySet();
    }
}
