
package com.tropyx.nb_puppet.completion;

import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class GlobalVarsFromPalette {
    
    public static List<String> get() {
        List<String> toRet = new ArrayList<>();
        FileObject dir = FileUtil.getConfigFile("PuppetPalette/Puppet Variables");
        for (FileObject ch : dir.getChildren()) {
            String name = ch.getName();
            toRet.add(name);
        }
        return toRet;
    }
}
