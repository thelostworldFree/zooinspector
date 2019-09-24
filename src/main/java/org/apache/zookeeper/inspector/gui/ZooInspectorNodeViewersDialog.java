/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.inspector.gui;

import org.apache.zookeeper.inspector.gui.nodeviewer.ZooInspectorNodeViewer;
import org.apache.zookeeper.inspector.logger.LoggerFactory;
import org.apache.zookeeper.inspector.manager.ZooInspectorManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link JDialog} for configuring which {@link ZooInspectorNodeViewer}s to
 * show in the application
 */
public class ZooInspectorNodeViewersDialog extends JDialog implements
        ListSelectionListener {

    private final JList viewersList;
    private final JFileChooser fileChooser = new JFileChooser(new File("."));
    private final Map<Button, JButton> buttons = new HashMap<Button, JButton>();
    /**
     * @param frame
     *            - the Frame from which the dialog is displayed
     * @param currentViewers
     *            - the {@link ZooInspectorNodeViewer}s to show
     * @param listeners
     *            - the {@link NodeViewersChangeListener}s which need to be
     *            notified of changes to the node viewers configuration
     * @param manager
     *            - the {@link ZooInspectorManager} for the application
     * 
     */
    public ZooInspectorNodeViewersDialog(Frame frame,
            final List<ZooInspectorNodeViewer> currentViewers,
            final Collection<NodeViewersChangeListener> listeners,
            final ZooInspectorManager manager,
            final IconResource iconResource) {
        super(frame);
        final List<ZooInspectorNodeViewer> newViewers = new ArrayList<ZooInspectorNodeViewer>(
                currentViewers);
        this.setLayout(new BorderLayout());
        this.setIconImage(iconResource.get(IconResource.ICON_ChangeNodeViewers,"")
                .getImage());
        this.setTitle("Change Node viewers");
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setResizable(true);
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        viewersList = new JList();
        DefaultListModel model = new DefaultListModel();
        for (ZooInspectorNodeViewer viewer : newViewers) {
            model.addElement(viewer);
        }
        viewersList.setModel(model);
        viewersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) value;
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setText(viewer.getTitle());
                return label;
            }
        });
        viewersList.setDropMode(DropMode.INSERT);
        viewersList.enableInputMethods(true);
        viewersList.setDragEnabled(true);
        viewersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        viewersList.getSelectionModel().addListSelectionListener(this);
        viewersList.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport info) {
                // we only import NodeViewers
                if (!info
                        .isDataFlavorSupported(ZooInspectorNodeViewer.nodeViewerDataFlavor)) {
                    return false;
                }

                JList.DropLocation dl = (JList.DropLocation) info
                        .getDropLocation();
                if (dl.getIndex() == -1) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean importData(TransferSupport info) {
                JList.DropLocation dl = (JList.DropLocation) info
                        .getDropLocation();
                DefaultListModel listModel = (DefaultListModel) viewersList
                        .getModel();
                int index = dl.getIndex();
                boolean insert = dl.isInsert();
                // Get the string that is being dropped.
                Transferable t = info.getTransferable();
                String data;
                try {
                    data = (String) t
                            .getTransferData(ZooInspectorNodeViewer.nodeViewerDataFlavor);
                } catch (Exception e) {
                    return false;
                }
                try {
                    ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) Class
                            .forName(data).newInstance();
                    if (listModel.contains(viewer)) {
                        listModel.removeElement(viewer);
                    }
                    if (insert) {
                        listModel.add(index, viewer);
                    } else {
                        listModel.set(index, viewer);
                    }
                    return true;
                } catch (Exception e) {
                    LoggerFactory.getLogger().error(
                            "Error instantiating class: " + data, e);
                    return false;
                }

            }

            @Override
            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                JList list = (JList) c;
                ZooInspectorNodeViewer value = (ZooInspectorNodeViewer) list
                        .getSelectedValue();
                return value;
            }
        });
        JScrollPane scroller = new JScrollPane(viewersList);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.gridwidth = 3;
        c1.gridheight = 3;
        c1.weightx = 0;
        c1.weighty = 1;
        c1.anchor = GridBagConstraints.CENTER;
        c1.fill = GridBagConstraints.BOTH;
        c1.insets = new Insets(5, 5, 5, 5);
        c1.ipadx = 0;
        c1.ipady = 0;
        panel.add(scroller, c1);

        final JTextField newViewerTextField = new JTextField();

        for(Button button : Button.values()) {
            JButton jbutton = button.createJButton(iconResource);
            buttons.put(button, jbutton);
        }
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 3;
        c2.gridy = 0;
        c2.gridwidth = 1;
        c2.gridheight = 1;
        c2.weightx = 0;
        c2.weighty = 0;
        c2.anchor = GridBagConstraints.NORTH;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(5, 5, 5, 5);
        c2.ipadx = 0;
        c2.ipady = 0;
        panel.add(buttons.get(Button.up), c2);
        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 3;
        c3.gridy = 2;
        c3.gridwidth = 1;
        c3.gridheight = 1;
        c3.weightx = 0;
        c3.weighty = 0;
        c3.anchor = GridBagConstraints.NORTH;
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.insets = new Insets(5, 5, 5, 5);
        c3.ipadx = 0;
        c3.ipady = 0;
        panel.add(buttons.get(Button.down), c3);
        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridx = 3;
        c4.gridy = 1;
        c4.gridwidth = 1;
        c4.gridheight = 1;
        c4.weightx = 0;
        c4.weighty = 0;
        c4.anchor = GridBagConstraints.NORTH;
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.insets = new Insets(5, 5, 5, 5);
        c4.ipadx = 0;
        c4.ipady = 0;
        panel.add(buttons.get(Button.remove), c4);
        GridBagConstraints c5 = new GridBagConstraints();
        c5.gridx = 0;
        c5.gridy = 3;
        c5.gridwidth = 3;
        c5.gridheight = 1;
        c5.weightx = 0;
        c5.weighty = 0;
        c5.anchor = GridBagConstraints.CENTER;
        c5.fill = GridBagConstraints.BOTH;
        c5.insets = new Insets(5, 5, 5, 5);
        c5.ipadx = 0;
        c5.ipady = 0;
        panel.add(newViewerTextField, c5);
        GridBagConstraints c6 = new GridBagConstraints();
        c6.gridx = 3;
        c6.gridy = 3;
        c6.gridwidth = 1;
        c6.gridheight = 1;
        c6.weightx = 0;
        c6.weighty = 0;
        c6.anchor = GridBagConstraints.CENTER;
        c6.fill = GridBagConstraints.BOTH;
        c6.insets = new Insets(5, 5, 5, 5);
        c6.ipadx = 0;
        c6.ipady = 0;
        panel.add(buttons.get(Button.add), c6);
        buttons.get(Button.up).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultListModel listModel = (DefaultListModel) viewersList
                        .getModel();
                ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) viewersList
                        .getSelectedValue();
                int index = viewersList.getSelectedIndex();
                if (listModel.contains(viewer)) {
                    listModel.removeElementAt(index);
                    listModel.insertElementAt(viewer, index - 1);
                    viewersList.setSelectedValue(viewer, true);
                }
            }
        });
        buttons.get(Button.down).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultListModel listModel = (DefaultListModel) viewersList
                        .getModel();
                ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) viewersList
                        .getSelectedValue();
                int index = viewersList.getSelectedIndex();
                if (listModel.contains(viewer)) {
                    listModel.removeElementAt(index);
                    listModel.insertElementAt(viewer, index + 1);
                    viewersList.setSelectedValue(viewer, true);
                }
            }
        });
        buttons.get(Button.remove).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultListModel listModel = (DefaultListModel) viewersList
                        .getModel();
                ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) viewersList
                        .getSelectedValue();
                int index = viewersList.getSelectedIndex();
                if (listModel.contains(viewer)) {
                    listModel.removeElement(viewer);
                    viewersList
                            .setSelectedIndex(index == listModel.size() ? index - 1
                                    : index);
                }
            }
        });
        buttons.get(Button.add).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String className = newViewerTextField.getText();
                if (className == null || className.length() == 0) {
                    JOptionPane
                            .showMessageDialog(
                                    ZooInspectorNodeViewersDialog.this,
                                    "Please enter the full class name for a Node Viewer and click the add button",
                                    "Input Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        DefaultListModel listModel = (DefaultListModel) viewersList
                                .getModel();
                        ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) Class
                                .forName(className).newInstance();
                        if (listModel.contains(viewer)) {
                            JOptionPane
                                    .showMessageDialog(
                                            ZooInspectorNodeViewersDialog.this,
                                            "Node viewer already exists.  Each node viewer can only be added once.",
                                            "Input Error",
                                            JOptionPane.ERROR_MESSAGE);
                        } else {
                            listModel.addElement(viewer);
                        }
                    } catch (Exception ex) {
                        LoggerFactory
                                .getLogger()
                                .error(
                                        "An error occurred while instaniating the node viewer. ",
                                        ex);
                        JOptionPane.showMessageDialog(
                                ZooInspectorNodeViewersDialog.this,
                                "An error occurred while instaniating the node viewer: "
                                        + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        GridBagConstraints c7 = new GridBagConstraints();
        c7.gridx = 0;
        c7.gridy = 4;
        c7.gridwidth = 1;
        c7.gridheight = 1;
        c7.weightx = 1;
        c7.weighty = 0;
        c7.anchor = GridBagConstraints.WEST;
        c7.fill = GridBagConstraints.VERTICAL;
        c7.insets = new Insets(5, 5, 5, 5);
        c7.ipadx = 0;
        c7.ipady = 0;
        panel.add(buttons.get(Button.save), c7);
        GridBagConstraints c8 = new GridBagConstraints();
        c8.gridx = 1;
        c8.gridy = 4;
        c8.gridwidth = 1;
        c8.gridheight = 1;
        c8.weightx = 0;
        c8.weighty = 0;
        c8.anchor = GridBagConstraints.WEST;
        c8.fill = GridBagConstraints.VERTICAL;
        c8.insets = new Insets(5, 5, 5, 5);
        c8.ipadx = 0;
        c8.ipady = 0;
        panel.add(buttons.get(Button.load), c8);
        GridBagConstraints c9 = new GridBagConstraints();
        c9.gridx = 2;
        c9.gridy = 4;
        c9.gridwidth = 1;
        c9.gridheight = 1;
        c9.weightx = 0;
        c9.weighty = 0;
        c9.anchor = GridBagConstraints.WEST;
        c9.fill = GridBagConstraints.VERTICAL;
        c9.insets = new Insets(5, 5, 5, 5);
        c9.ipadx = 0;
        c9.ipady = 0;
        panel.add(buttons.get(Button.setDefaults), c9);
        buttons.get(Button.save).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int result = fileChooser
                        .showSaveDialog(ZooInspectorNodeViewersDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    int answer = JOptionPane.YES_OPTION;
                    if (selectedFile.exists()) {
                        answer = JOptionPane
                                .showConfirmDialog(
                                        ZooInspectorNodeViewersDialog.this,
                                        "The specified file already exists.  do you want to overwrite it?",
                                        "Confirm Overwrite",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE);
                    }
                    if (answer == JOptionPane.YES_OPTION) {
                        DefaultListModel listModel = (DefaultListModel) viewersList
                                .getModel();
                        List<String> nodeViewersClassNames = new ArrayList<String>();
                        Object[] modelContents = listModel.toArray();
                        for (Object o : modelContents) {
                            nodeViewersClassNames
                                    .add(((ZooInspectorNodeViewer) o)
                                            .getClass().getCanonicalName());
                        }
                        try {
                            manager.saveNodeViewersFile(selectedFile,
                                    nodeViewersClassNames);
                        } catch (IOException ex) {
                            LoggerFactory
                                    .getLogger()
                                    .error(
                                            "Error saving node viewer configuration from file.",
                                            ex);
                            JOptionPane.showMessageDialog(
                                    ZooInspectorNodeViewersDialog.this,
                                    "Error saving node viewer configuration from file: "
                                            + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        buttons.get(Button.load).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int result = fileChooser
                        .showOpenDialog(ZooInspectorNodeViewersDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        List<String> nodeViewersClassNames = manager
                                .loadNodeViewersFile(fileChooser
                                        .getSelectedFile());
                        List<ZooInspectorNodeViewer> nodeViewers = new ArrayList<ZooInspectorNodeViewer>();
                        for (String nodeViewersClassName : nodeViewersClassNames) {
                            ZooInspectorNodeViewer viewer = (ZooInspectorNodeViewer) Class
                                    .forName(nodeViewersClassName)
                                    .newInstance();
                            nodeViewers.add(viewer);
                        }
                        DefaultListModel model = new DefaultListModel();
                        for (ZooInspectorNodeViewer viewer : nodeViewers) {
                            model.addElement(viewer);
                        }
                        viewersList.setModel(model);
                        panel.revalidate();
                        panel.repaint();
                    } catch (Exception ex) {
                        LoggerFactory
                                .getLogger()
                                .error(
                                        "Error loading node viewer configuration from file.",
                                        ex);
                        JOptionPane.showMessageDialog(
                                ZooInspectorNodeViewersDialog.this,
                                "Error loading node viewer configuration from file: "
                                        + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        buttons.get(Button.setDefaults).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane
                        .showConfirmDialog(
                                ZooInspectorNodeViewersDialog.this,
                                "Are you sure you want to save this configuration as the default?",
                                "Confirm Set Defaults",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    DefaultListModel listModel = (DefaultListModel) viewersList
                            .getModel();
                    List<String> nodeViewersClassNames = new ArrayList<String>();
                    Object[] modelContents = listModel.toArray();
                    for (Object o : modelContents) {
                        nodeViewersClassNames.add(((ZooInspectorNodeViewer) o)
                                .getClass().getCanonicalName());
                    }
                    try {
                        manager
                                .setDefaultNodeViewerConfiguration(nodeViewersClassNames);
                    } catch (IOException ex) {
                        LoggerFactory
                                .getLogger()
                                .error(
                                        "Error setting default node viewer configuration.",
                                        ex);
                        JOptionPane.showMessageDialog(
                                ZooInspectorNodeViewersDialog.this,
                                "Error setting default node viewer configuration: "
                                        + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZooInspectorNodeViewersDialog.this.dispose();
                DefaultListModel listModel = (DefaultListModel) viewersList
                        .getModel();
                newViewers.clear();
                Object[] modelContents = listModel.toArray();
                for (Object o : modelContents) {
                    newViewers.add((ZooInspectorNodeViewer) o);
                }
                currentViewers.clear();
                currentViewers.addAll(newViewers);
                for (NodeViewersChangeListener listener : listeners) {
                    listener.nodeViewersChanged(currentViewers);
                }
            }
        });
        buttonsPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZooInspectorNodeViewersDialog.this.dispose();
            }
        });
        buttonsPanel.add(cancelButton);
        this.add(panel, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.pack();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
     * .ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
        JButton removeButton = buttons.get(Button.remove);
        JButton upButton = buttons.get(Button.up);
        JButton downButton = buttons.get(Button.down);
        int index = viewersList.getSelectedIndex();

        if (index == -1) {
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
            if (index == 0) {
                upButton.setEnabled(false);
            } else {
                upButton.setEnabled(true);
            }
            if (index == ((DefaultListModel) viewersList.getModel()).getSize()) {
                downButton.setEnabled(false);
            } else {
                downButton.setEnabled(true);
            }
        }
    }

    public static enum Button {
        up("Move currently selected node viewer up",IconResource.ICON_UP,false),
        down("Move currently selected node viewer down",IconResource.ICON_DOWN,false),
        add("Add node viewer",IconResource.ICON_ADD,true),
        remove("Remove currently selected node viewer",IconResource.ICON_REMOVE,false),
        save("Save current node viewer configuration to file","Save"),
        load("Load node viewer configuration from file","Load"),
        setDefaults("Set current configuration asd defaults","Set as defaults");

        private String toolTip;
        private String icon;
        private boolean enabled;

        Button(String toolTip, String icon, boolean enabled) {
            this.toolTip = toolTip;
            this.icon = icon;
            this.enabled = enabled;
        }

        Button(String toolTip, String icon) {
            this(toolTip, icon, true);
        }

        public JButton createJButton(IconResource iconResource) {
            ImageIcon imageIcon = iconResource.get(icon, toolTip);
            JButton jbutton;

            if(imageIcon == null) {
                jbutton = new JButton(icon);
            } else {
                jbutton = new JButton(imageIcon);
            }

            jbutton.setEnabled(enabled);
            jbutton.setToolTipText(toolTip);
            return jbutton;
        }
    }
}
