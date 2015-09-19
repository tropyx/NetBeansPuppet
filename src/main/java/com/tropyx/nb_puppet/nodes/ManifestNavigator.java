/*
 * Copyright (C) 2015 github.com/tropyx
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

package com.tropyx.nb_puppet.nodes;

import static com.tropyx.nb_puppet.nodes.Bundle.NAV_HINT;
import static com.tropyx.nb_puppet.nodes.Bundle.NAV_NAME;
import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
@NavigatorPanel.Registration(mimeType="text/x-puppet-manifest", position=200, displayName="#NAV_NAME")
@Messages("NAV_NAME=Module Manifests")
public class ManifestNavigator implements NavigatorPanel {
    private ManifestsPanel component;
    
    protected Lookup.Result<DataObject> selection;

    protected final LookupListener selectionListener = new LookupListener() {
        @Override
        public void resultChanged(LookupEvent ev) {
            if(selection == null) {
                return;
            }
            navigate(selection.allInstances());
        }
    };
    

    @Override
    public String getDisplayName() {
        return NAV_NAME();
    }

    @Override
    @Messages("NAV_HINT=View neighbouring manifests")
    public String getDisplayHint() {
        return NAV_HINT();
    }

    @Override
    public JComponent getComponent() {
        return getNavigatorUI();
    }
    
    private ManifestsPanel getNavigatorUI() {
        if (component == null) {
            component = new ManifestsPanel();
        }
        return component;
    }

    @Override
    public void panelActivated(Lookup context) {
        getNavigatorUI().showWaitNode();
        selection = context.lookupResult(DataObject.class);
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    @Override
    public void panelDeactivated() {
        getNavigatorUI().showWaitNode();
        if(selection != null) {
            selection.removeLookupListener(selectionListener);
            selection = null;
        }
        getNavigatorUI().release();
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
    /**
     * 
     * @param selectedFiles 
     */

    public void navigate(Collection<? extends DataObject> selectedFiles) {
        if(selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            getNavigatorUI().navigate(d);           
        }
    }
    

}
