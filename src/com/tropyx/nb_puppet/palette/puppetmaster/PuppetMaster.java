package com.tropyx.nb_puppet.palette.puppetmaster;

import com.tropyx.nb_puppet.palette.PuppetPaletteUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

public class PuppetMaster implements ActiveEditorDrop {

    private String puppetmaster = "";

    public PuppetMaster() {
    }

    private String createBody() {
        puppetmaster = getPuppetMaster();
        String Pm = "$puppetmaster=\'" + puppetmaster + "\'"
                + "\n";
        //String Pm = "$puppetmaster=\'";

        return Pm;
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        PuppetMasterCustomizer c = new PuppetMasterCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            String body = createBody();
            try {
                PuppetPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        return accept;
    }

    public String getPuppetMaster() {
        return puppetmaster;
    }

    public void setPuppetMaster(String puppetmaster) {
        this.puppetmaster = puppetmaster;
    }

}