package com.github.gervaisb.swagger.exporter.gui;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.List;

public class View extends JPanel {

    private final SpecificationOptionsPanel specificationOptionsPanel = new SpecificationOptionsPanel();
    private final ExportOptionsPanel exportOptionsPanel = new ExportOptionsPanel();
    private final JProgressBar progressBar = new JProgressBar();
    private final DefaultListModel<File> output = new DefaultListModel<>();
    private final Presenter presenter;

    public View() {
        super(new BorderLayout(0, 0));
        Box containers = Box.createVerticalBox();
        containers.add(titled("Specification", specificationOptionsPanel));
        containers.add(titled("Export", exportOptionsPanel));
        containers.add(Box.createVerticalStrut(8));
        containers.add(progressBar);
        containers.add(new OutputList(output));
        add(containers);
        presenter = new Presenter(this);

        exportOptionsPanel.addActionListener(e -> presenter.onExport());
        File[] fs = new File("/home/blaise").listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        for (File f : fs) {
            output.addElement(f);
        }
    }

    private JPanel titled(String title, JPanel panel) {
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    public void setProgressStarted() {
        progressBar.setIndeterminate(true);
    }

    public void setProgressFinished() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
    }

    public List<String> getOutputFormats() {
        return new ArrayList<>(exportOptionsPanel.getOutputFormats());
    }

    public String getSpecificationUrl() {
        return specificationOptionsPanel.getSwaggerUrl();
    }

    public Set<String> getExcludedClasses() {
        return exportOptionsPanel.getExcludedClasses();
    }

    public void setResults(File[] files) {
        for (File file : files) {
            output.addElement(file);
        }
    }

    private static class ExportOptionsPanel extends JPanel {

        private final JTextField exclusions = new JTextField();
        private final JButton export = new JButton("Export");
        private final JCheckBox uml = new JCheckBox("PlantUml");
        private final JCheckBox png = new JCheckBox("Png");
        private final JCheckBox csv = new JCheckBox("Csv");

        ExportOptionsPanel() {
            super(new GridBagLayout(), true);
            Insets insets = new Insets(8, 4, 2, 4);

            JLabel exclude = new JLabel("Exclude :");
            exclude.setLabelFor(exclusions);

            export.setEnabled(false);

            // Line 1
            add(exclude, new GridBagConstraints(
                    0, 0, 1, 1, 0.2, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0
            ));
            add(exclusions, new GridBagConstraints(
                    1, 0, 1, 1, 0.8, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(insets.top, insets.left, 0, insets.right), 0, 0
            ));
            // Line 2
            add(new Tooltip("Ignore named classes in export"), new GridBagConstraints(
                    1, 1, 1, 1, 0.8, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, insets.left, insets.bottom, insets.right), 0, 0
            ));
            // Line 3
            add(new JLabel("Formats :"), new GridBagConstraints(
                    0, 2, 1, 1, 0.2, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0
            ));
            add(new FormatsPanel(png, csv, uml), new GridBagConstraints(
                    1, 2, 1, 1, 0.2, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(insets.top, 0, insets.bottom, insets.right), 0, 0
            ));
            // Line 4
            add(export, new GridBagConstraints(
                    0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(16, insets.left, insets.bottom, insets.right), 0, 0
            ));

            uml.addItemListener(new AtLeastOneFormatConstraint(export));
            png.addItemListener(new AtLeastOneFormatConstraint(export));
            csv.addItemListener(new AtLeastOneFormatConstraint(export));
        }

        Set<String> getExcludedClasses() {
            return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(exclusions.getText().split("[,|;]"))));
        }

        Set<String> getOutputFormats() {
            HashSet<String> formats = new HashSet<>(3);
            if ( csv.isSelected() ) {
                formats.add("csv");
            }
            if ( png.isSelected() ) {
                formats.add("png");
            }
            if ( uml.isSelected() ) {
                formats.add("uml");
            }
            return Collections.unmodifiableSet(formats);
        }

        public void addActionListener(ActionListener lstnr) {
            export.addActionListener(lstnr);
        }

        private static final class FormatsPanel extends JPanel {
            FormatsPanel(JCheckBox... formats) {
                super(new FlowLayout(FlowLayout.LEADING, 0, 0));
                for (JCheckBox format : formats) {
                    add(format);
                }
            }
        }

        private final class AtLeastOneFormatConstraint implements ItemListener {

            private final JButton button;

            private AtLeastOneFormatConstraint(JButton button) {
                this.button = button;
            }

            @Override
            public void itemStateChanged(ItemEvent e) {
                button.setEnabled(!getOutputFormats().isEmpty());
            }
        }
    }

    static class SpecificationOptionsPanel extends JPanel {

        private final JTextField swaggerUrl = new JTextField();

        SpecificationOptionsPanel() {
            super(new GridBagLayout(), true);
            Insets insets = new Insets(8, 4, 2, 4);
            JLabel swagger = new JLabel("Swagger :");
            swagger.setLabelFor(swaggerUrl);

            JButton browse = new JButton("...");
            browse.setActionCommand("OpenFileChooser");
            browse.setToolTipText("Browse for file");
            browse.addActionListener(new OpenFileChooserAction());
            browse.setPreferredSize(swaggerUrl.getPreferredSize());
            // Line 1
            add(swagger, new GridBagConstraints(
                    0, 0, 1, 1, 0.2, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, insets, 0, 0
            ));
            add(swaggerUrl, new GridBagConstraints(
                    1, 0, 1, 1, 0.7, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, insets, 0, 0
            ));
            add(browse, new GridBagConstraints(
                    2, 0, 1, 1, 0.1, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(insets.top, insets.left, 0, insets.right), 0, 0
            ));
            // Line 2
            add(new Tooltip("Url or local file"), new GridBagConstraints(
                    1, 1, 1, 1, 0.7, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, insets.left, insets.bottom, insets.right), 0, 0
            ));
        }

        String getSwaggerUrl() {
            String url = swaggerUrl.getText();
            return url.startsWith("http")?url:"file://"+url;
        }

        void setSwaggerUrl(String url) {
            swaggerUrl.setText(url);
        }

        private void setSelectedFile(File file) {
            setSwaggerUrl(file.getAbsolutePath());
        }

        private final class OpenFileChooserAction implements ActionListener {
            private JFileChooser chooser;
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()) {
                    case "ApproveSelection": // From JFileChooser
                        setSelectedFile(getOrCreateFileChooser().getSelectedFile());
                        break;
                    case "CancelSelection": // From JFileChooser
                        // Nothing to do
                        break;
                    case "OpenFileChooser": // From SpecificationOptionsPanel#browse
                        getOrCreateFileChooser().showOpenDialog(SpecificationOptionsPanel.this);
                        break;
                    default:
                        throw new UnsupportedOperationException("Action command \""+e.getActionCommand()+"\" is not supported");
                }
            }

            private JFileChooser getOrCreateFileChooser() {
                if( chooser==null ) {
                    chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    chooser.addActionListener(this);
                }
                return chooser;
            }
        }

    }

    static class Tooltip extends JLabel {
        Tooltip(String text) {
            super(text);
            setForeground(Color.GRAY);
            setFont(getFont().deriveFont((float)(getFont().getSize()*0.8)));
        }
    }

    private static class OutputList extends JPanel {
        private OutputList(ListModel<File> model) {
            super(new BorderLayout(), true);
            setPreferredSize(new Dimension(200, 180));

            JList<File> list = new JList<>(model);
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setFixedCellHeight(80);
            list.setFixedCellWidth(64);
            list.setCellRenderer(new ListCellRenderer<File>() {
                JPanel stamp = new JPanel(null);
                JLabel name = new JLabel();
                JLabel icon = new JLabel();
                {
                    stamp.add(icon);
                    stamp.add(name);
                    stamp.setPreferredSize(new Dimension(list.getFixedCellWidth(), list.getFixedCellHeight()));
                    stamp.setMaximumSize(stamp.getPreferredSize());
                    stamp.setMinimumSize(stamp.getPreferredSize());
                    icon.setBounds(0, 0, 64, 64);
                    icon.setBorder(BorderFactory.createLineBorder(Color.GREEN));
                    name.setBounds(0, 64, 64, 80-64);
                    name.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
                }
                @Override
                public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected, boolean cellHasFocus) {
                    stamp.setBackground(isSelected?list.getSelectionBackground():list.getBackground());
                    stamp.setForeground(isSelected?list.getSelectionForeground():list.getForeground());
                    Icon fsIcon = FileSystemView.getFileSystemView().getSystemIcon(value);
                    System.out.println(value+";" +fsIcon);
                    icon.setIcon(fsIcon);
                    name.setText(value.getName());
                    return stamp;
                }
            });

            add(new JScrollPane(list));
        }
    }
}
