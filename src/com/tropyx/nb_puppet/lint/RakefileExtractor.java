
package com.tropyx.nb_puppet.lint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;

public class RakefileExtractor {
    public static final String USE_RAKEFILE = "useRakefile";

    public static boolean isUseRakefile(AuxiliaryProperties prefs) {
        String rr = prefs.get(RakefileExtractor.USE_RAKEFILE, true);
        if (rr == null) {
            rr = "true";
        }
        return Boolean.parseBoolean(rr);
    }

    private static final Pattern PATTERN1 = Pattern.compile("PuppetLint.configuration.send\\('disable_(.*)'\\)");
    private static final Pattern PATTERN2 = Pattern.compile("PuppetLint.configuration.send\\('(.*)'\\)");
    public static String[] getConfiguration(FileObject fo) throws IOException {
        List<String> toRet = new ArrayList<>();
        for (String line : fo.asLines("UTF-8")) {
            Matcher matcher = PATTERN1.matcher(line);
            if (matcher.find()) {
                toRet.add("--no-" + matcher.group(1) + "-check");
            } else {
                matcher = PATTERN2.matcher(line);
                if (matcher.find()) {
                    toRet.add("--" + matcher.group(1));
                }
            }
        }
        return toRet.toArray(new String[0]);
    }
}
