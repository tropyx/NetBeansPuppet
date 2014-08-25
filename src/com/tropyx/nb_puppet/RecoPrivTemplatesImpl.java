package com.tropyx.nb_puppet;

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

public final class RecoPrivTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

    private static final String[] PRIVILEGED_NAMES = {
        "Templates/Puppet/PuppetManifestTemplate.pp",
        "Templates/Other/Folder",
    };
    private static final String[] TYPES = {
        "puppet",
        "XML",
        "simple-files"
    };

    
    public RecoPrivTemplatesImpl() {
    }
    
    @Override 
    public String[] getRecommendedTypes() {
        return TYPES.clone();
    }
    
    @Override 
    public String[] getPrivilegedTemplates() {
        return PRIVILEGED_NAMES;
    }

}
