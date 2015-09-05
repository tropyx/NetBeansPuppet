/*
 * Copyright (C) 2014 mkleint
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tropyx.nb_puppet.lint;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class LintPanelUI extends javax.swing.JPanel {
    private AuxiliaryProperties prefs;
    private HashSet<Object> rakes;

    /**
     * Creates new form LintPanelUI
     */
    public LintPanelUI() {
        initComponents();
    }

    LintPanelUI(ProjectCustomizer.Category category, Project project) {
        this();
        category.setOkButtonListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }

        });

        rakes = new HashSet<>();
        FileObject fo = project.getProjectDirectory().getFileObject("Rakefile");
        if (fo != null) {
            try {
                String[] switches = new RakefileExtractor().getConfiguration(fo);
                rakes.addAll(Arrays.asList(switches));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        prefs = project.getLookup().lookup(AuxiliaryProperties.class);
        boolean readRakefile = RakefileExtractor.isUseRakefile(prefs);
        cbReadRakefile.setSelected(readRakefile);
        createCheckBoxes(readRakefile);
    }


    private void createCheckBoxes(boolean readRakeFile) {
        for (LintCheck v : LintCheck.values()) {
            String enable = prefs.get("lint." + v.name(), true);
            if (enable == null) {
                enable = "true";
            }
            boolean rake = readRakeFile && rakes.contains(v.getDisableParam());
            if (rake) {
                enable = "false";
            }
            final JCheckBox jCheckBox = new JCheckBox(v.getDisplayName(), Boolean.parseBoolean(enable));
            jCheckBox.putClientProperty("lint", v);
            jCheckBox.putClientProperty("wasEnabled", enable);
            if (rake) {
                jCheckBox.setEnabled(false);
            }
            plnChecks.add(jCheckBox);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbReadRakefile = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        plnChecks = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(cbReadRakefile, org.openide.util.NbBundle.getMessage(LintPanelUI.class, "LintPanelUI.cbReadRakefile.text")); // NOI18N
        cbReadRakefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbReadRakefileActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LintPanelUI.class, "LintPanelUI.jLabel1.text")); // NOI18N

        plnChecks.setLayout(new java.awt.GridLayout(LintCheck.values().length, 1));
        jScrollPane1.setViewportView(plnChecks);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbReadRakefile)
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbReadRakefile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbReadRakefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbReadRakefileActionPerformed
        plnChecks.removeAll();
        createCheckBoxes(cbReadRakefile.isSelected());
        plnChecks.revalidate();
        plnChecks.repaint();
    }//GEN-LAST:event_cbReadRakefileActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbReadRakefile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel plnChecks;
    // End of variables declaration//GEN-END:variables

    private void doSave() {
        prefs.put(RakefileExtractor.USE_RAKEFILE, cbReadRakefile.isSelected() ? null : "false", true);
        for (Component a : plnChecks.getComponents()) {
            if (a instanceof JCheckBox) {
                JCheckBox aa = (JCheckBox)a;
                if (!aa.isEnabled()) {
                    continue;
                }
                String wasEnabled = (String) aa.getClientProperty("wasEnabled");
                LintCheck en = (LintCheck) aa.getClientProperty("lint");
                if (!wasEnabled.equals(Boolean.toString(aa.isSelected()))) {
                    prefs.put("lint." + en.name(), Boolean.toString(aa.isSelected()), true);
                }
            }
        }
    }

}
