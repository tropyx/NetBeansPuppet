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

package com.tropyx.nb_puppet.hiera;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(mimeType = "text/x-yaml", 
        persistenceType = TopComponent.PERSISTENCE_NEVER, 
        displayName = "Hiera Eyaml", preferredID = "hieraeyaml", position = 250)
public class HieraEyamlMV implements MultiViewElement {
    private final Lookup lookup;
    private EyamlPanel panel;
    
    public HieraEyamlMV(Lookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public JComponent getVisualRepresentation() {
        if (panel == null) {
            panel = new EyamlPanel(lookup);
        }
        return panel;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return new JLabel("Executes 'eyaml decrypt --eyaml <file>'");
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
        getVisualRepresentation();
        panel.load();
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

}
