package com.tropyx.nb_puppet.palette.puppetfile;

import com.tropyx.nb_puppet.palette.puppetfile.*;
import com.tropyx.nb_puppet.palette.PuppetPaletteUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

public class PuppetFile implements ActiveEditorDrop {

    private String hostname = "";

    public PuppetFile() {
    }

    private String createBody() {
        hostname = getFileName();
        String snippet = "\n\nfile { \"/etc/sudoers\":"
                + "\n\t mode => 440,"
                + "\n\t owner => root,"
                + "\n\t group => root,"
                + "\n\t source => \"puppet:///modules/admin/sudoers\","
                + "\n\t backup => \".bak\""
                + "\n\t }\n";
        return snippet;
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        PuppetFileCustomizer c = new PuppetFileCustomizer(this, targetComponent);
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

    public String getFileName() {
        return hostname;
    }

    public void setFileName(String hostname) {
        this.hostname = hostname;
    }
}
