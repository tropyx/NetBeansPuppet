package com.tropyx.nb_puppet.palette.apache_vhost;

import com.tropyx.nb_puppet.palette.apache_vhost.*;
import com.tropyx.nb_puppet.palette.PuppetPaletteUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

public class ApacheVhost implements ActiveEditorDrop {

    private String hostname = "";

    public ApacheVhost() {
    }

    private String createBody() {
        hostname = getHostName();
        String snippet = "\n\napache::vhost {'" + hostname + "':\n"
                + "\t\tport    => 80,\n"
                + "\t\tdocroot => '/var/www/personal',\n"
                + "\t\toptions => 'Indexes MultiViews',"
                + "\t\t}\n";
        return snippet;
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        ApacheVhostCustomizer c = new ApacheVhostCustomizer(this, targetComponent);
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

    public String getHostName() {
        return hostname;
    }

    public void setHostName(String hostname) {
        this.hostname = hostname;
    }
}