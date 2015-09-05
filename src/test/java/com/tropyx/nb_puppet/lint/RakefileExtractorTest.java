package com.tropyx.nb_puppet.lint;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class RakefileExtractorTest {

    public RakefileExtractorTest() {
    }

    @Test
    public void testGetConfiguration() throws Exception {
        String rakefile = "require 'puppetlabs_spec_helper/rake_tasks'\n"
                + "require 'puppet-lint/tasks/puppet-lint'\n"
                + "\n"
                + "PuppetLint.configuration.fail_on_warnings = true\n"
                + "PuppetLint.configuration.send('relative')\n"
                + "PuppetLint.configuration.send('disable_80chars')\n"
                + "PuppetLint.configuration.send('disable_class_inherits_from_params_class')\n"
                + "PuppetLint.configuration.send('disable_documentation')\n"
                + "PuppetLint.configuration.send('disable_single_quote_string_with_variables')\n"
                + "PuppetLint.configuration.ignore_paths = [\"spec/**/*.pp\", \"pkg/**/*.pp\"]";
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject good = fs.getRoot().createData("good.mf");
        ByteArrayInputStream ba = new ByteArrayInputStream(rakefile.getBytes("UTF-8"));
        final OutputStream outputStream = good.getOutputStream();
        FileUtil.copy(ba, outputStream);
        outputStream.close();
        RakefileExtractor ext = new RakefileExtractor();
        String[] result = ext.getConfiguration(good);
        assertArrayEquals(new String[]{
            "--relative",
            "--no-80chars-check",
            "--no-class_inherits_from_params_class-check",
            "--no-documentation-check",
            "--no-single_quote_string_with_variables-check"},
                result);
    }

}
