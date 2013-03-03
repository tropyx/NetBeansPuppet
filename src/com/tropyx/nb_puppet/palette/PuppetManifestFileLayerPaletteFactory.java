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

package com.tropyx.nb_puppet.palette;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

public class PuppetManifestFileLayerPaletteFactory {

    private static PaletteController palette = null;

    @MimeRegistration(mimeType = "text/x-java", service = PaletteController.class)
    public static PaletteController createPalette() {
        try {
            if (null == palette) {
                return PaletteFactory.createPalette(
                //Folder:      
                "HTMLPalette", 
                //Palette Actions:
                new PaletteActions() {
                    @Override public Action[] getImportActions() {return null;}
                    @Override public Action[] getCustomPaletteActions() {return null;}
                    @Override public Action[] getCustomCategoryActions(Lookup lkp) {return null;}
                    @Override public Action[] getCustomItemActions(Lookup lkp) {return null;}
                    @Override public Action getPreferredAction(Lookup lkp) {return null;}
                }, 
                //Palette Filter:  
                null, 
                //Drag and Drop Handler:  
                new DragAndDropHandler(true) {
                    @Override public void customize(ExTransferable et, Lookup lkp) {}
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
