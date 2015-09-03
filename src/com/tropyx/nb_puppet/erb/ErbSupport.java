
package com.tropyx.nb_puppet.erb;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = MIMEResolver.class)
public class ErbSupport extends MIMEResolver {

    private Map<String, String> ext2Mime;

    public ErbSupport() {
        //TODO listen on changes in "Services/MIMEResolver" and reload map
    }

    @Override
    public String findMIMEType(FileObject fo) {
        if ("erb".equals(fo.getExt())) {
            String name = fo.getName();
            int ind = name.lastIndexOf(".");
            if (ind > 0) {
                String ext = name.substring(ind + 1);
                return getMimeForExt(ext);
            }
        }
        return null;
    }

    private synchronized String getMimeForExt(String ext) {
        if (ext2Mime == null) {
            ext2Mime = createExt2MimeMap();
        }
        return ext2Mime.get(ext);
    }

    //HACK, no other simple way to find all extensions registered
    private Map<String, String> createExt2MimeMap() {
        FileObject mmr = FileUtil.getConfigFile("Services/MIMEResolver");
        Enumeration<? extends FileObject> enumer = mmr.getData(true);
        Map<String, String> toRet = new HashMap<>();
        while (enumer.hasMoreElements()) {
            FileObject fo = enumer.nextElement();
            if ("org.openide.filesystems.MIMEResolver".equals(fo.getAttribute("instanceClass"))) {
                String mime = (String) fo.getAttribute("mimeType");
                int count = 0;
                String ext = (String) fo.getAttribute("ext." + count);
                while (ext != null) {
                    toRet.put(ext, mime);
                    count++;
                    ext = (String) fo.getAttribute("ext." + count);
                }
            }
        }
        return toRet;
    }
}
