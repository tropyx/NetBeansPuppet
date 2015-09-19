/*
 * Copyright (C) Tropyx Technology Pty Ltd and Michael Lindner 2013
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
package com.tropyx.nb_puppet;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.lookup.Lookups;

public class PuppetCustomizerProvider implements CustomizerProvider {

    public final PuppetProject project;
    public static final String CUSTOMIZER_FOLDER_PATH =
            "Projects/com-tropyx-nb_puppet/Customizer";

    public PuppetCustomizerProvider(PuppetProject project) {
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                //Path to layer folder:
                CUSTOMIZER_FOLDER_PATH,
                //Lookup, which must contain, at least, the Project:
                Lookups.fixed(project),
                //Preselected category:
                "",
                //OK button listener:
                new OKOptionListener(),
                //HelpCtx for Help button of dialog:
                null);
        dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());
        dialog.setVisible(true);
    }

    private class OKOptionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            StatusDisplayer.getDefault().setStatusText("OK button clicked for "
                    + project.getProjectDirectory().getName() + " customizer!");
        }

    }

}
