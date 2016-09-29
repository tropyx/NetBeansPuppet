package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.PuppetProject;
import static com.tropyx.nb_puppet.lint.ExecutePuppetLintAction.findBasedir;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint 
 */
@MimeRegistration(mimeType=PPConstants.MIME_TYPE, service=UpToDateStatusProviderFactory.class)
public final class StatusProvider implements UpToDateStatusProviderFactory {

    private static final String LAYER_ERRORS = "puppet-plugin-errors"; //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(StatusProvider.class); //NOI18N
    private static final Logger LOG = Logger.getLogger(StatusProvider.class.getName());

    @Override
    public UpToDateStatusProvider createUpToDateStatusProvider(Document document) {
        return new StatusProviderImpl(document);
    }

    static class StatusProviderImpl extends UpToDateStatusProvider {
        private final Document document;
        private final AtomicBoolean dirty = new AtomicBoolean(true);
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final Project project;

        StatusProviderImpl(Document doc) {
            this.document = doc;
            FileObject fo = NbEditorUtilities.getFileObject(document);
            project = FileOwnerQuery.getOwner(fo);
            fo.addFileChangeListener(new FileChangeAdapter() {

                @Override
                public void fileChanged(FileEvent fe)
                {
                    dirty.set(true);
                }
                
            });
        }

        @Override
        public UpToDateStatus getUpToDate() {
            final FileObject fo = NbEditorUtilities.getFileObject(document);
            //TODO do this in other thread??
            if (fo.isValid() && dirty.compareAndSet(true, false)) {
                if (running.compareAndSet(false, true)) {
                    RP.post(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            HintsController.setErrors(document, LAYER_ERRORS, checkErrors(fo));
                            running.set(false);
                        }
                    });
                } 
                return UpToDateStatus.UP_TO_DATE_PROCESSING;
            }
            return UpToDateStatus.UP_TO_DATE_OK; 
        }

        private List<ErrorDescription> checkErrors(FileObject fo) {
            FileObject basedir = findBasedir(fo.getParent());
            ArrayList<ErrorDescription> toRet = new ArrayList<>();
            final File folder = FileUtil.toFile(basedir);
            if (folder == null) {
                //is in zip file?
                return toRet;
            }
            ExternalProcessBuilder builder = new ExternalProcessBuilder(findLint())
                .workingDirectory(folder)
                .redirectErrorStream(true)
                .addArgument(FileUtil.getRelativePath(basedir, fo))
                .addArgument("--log-format")
                .addArgument("%{line}||%{kind}||%{check}||%{message}");
            
            for (String skip : skipChecks()) {
                builder = builder.addArgument(skip);
            }
            
            Process process;
            InputStream in = null;
            try
            {
                process = builder.call();
                process.waitFor();
                InputStream os = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(os));
                String line = br.readLine();
                while (line != null) {
                    String[] vals = line.split("\\|\\|");
                    if (vals.length == 4) {
                        int lineNum = Integer.parseInt(vals[0]);
                        Severity level = "warning".equals(vals[1]) ? Severity.WARNING : Severity.ERROR;
                        String type = vals[2];
                        String message = vals[3];
                        ErrorDescription err = ErrorDescriptionFactory.createErrorDescription(level, message, findFixesForType(type, document, lineNum), document, lineNum);
                        toRet.add(err);
                    }
                    line = br.readLine();
                }
            } catch (IOException | InterruptedException ex)
            {
                Exceptions.printStackTrace(ex);
            } finally {
                if (in != null) {
                    try
                    {
                        in.close();
                    } catch (IOException ex)
                    {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            return toRet;
        }

        private Set<String> skipChecks()
        {
            Set<String> toRet = new HashSet<>();
            //global prefs
            Preferences nd = NbPreferences.forModule(StatusProvider.class).node("lint");
            AuxiliaryProperties p = null;
            if (project != null) {
                p = project.getLookup().lookup(AuxiliaryProperties.class);
                final PuppetProject pp = project.getLookup().lookup(PuppetProject.class);
                if (pp != null && pp.isModule()) {
                    toRet.add("--relative");
                }
                if (RakefileExtractor.isUseRakefile(p)) {
                    FileObject fo = project.getProjectDirectory().getFileObject("Rakefile");
                    if (fo != null) {
                        try {
                            toRet.addAll(Arrays.asList(RakefileExtractor.getConfiguration(fo)));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            for (LintCheck lc : LintCheck.values()) {
                if (p != null && "false".equals(p.get("lint." + lc.name(), true))) {
                    toRet.add(lc.getDisableParam());
                }
                else if (!nd.getBoolean(lc.name(), true)) {
                    toRet.add(lc.getDisableParam());
                }
            }
            return toRet;
        }


    }
    
    static String findLint() {
        if (org.openide.util.Utilities.isMac()) {
            //mkleint: tired of tweaking the path of desktop apps to find
            // this location somehow on mac.
            if (new File("/usr/local/bin/puppet-lint").exists()) {
                return "/usr/local/bin/puppet-lint";
            }
        }
        return "puppet-lint";
    }

    private static List<Fix> findFixesForType(String type, final Document document, int lineNum) {
        System.out.println("type:" + type);
        final int startindex = Utilities.getRowStartFromLineOffset((BaseDocument) document, lineNum - 1);
        final int endindex = Utilities.getRowStartFromLineOffset((BaseDocument) document, lineNum);
        if ("double_quoted_strings".equals(type)) {
            return Collections.<Fix>singletonList(new DoubleQuotedStringsFix(document, startindex, endindex));
        }
        if ("only_variable_string".equals(type)) {
            return Collections.<Fix>singletonList(new OnlyVariableStringFix(document, startindex, endindex));
        }
        if ("single_quote_string_with_variables".equals(type)) {
            return Collections.<Fix>singletonList(new SingleQuotedStringsFix(document, startindex, endindex));
        }
//        if ("variables_not_enclosed".equals(type)) {
//            return Collections.<Fix>singletonList(new VariablesNotEnclosedFix(document, startindex, endindex));
//        }
        if ("trailing_whitespace".equals(type)) {
            return Collections.<Fix>singletonList(new TrailingWhitespaceFix(document, startindex, endindex));
        }
        if ("arrow_alignment".equals(type)) {
            return Collections.<Fix>singletonList(new ArrowAlignmentFix(document, startindex, endindex));
        }
        return Collections.emptyList();
    }


}
