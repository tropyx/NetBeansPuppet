package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.PuppetProject;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import static com.tropyx.nb_puppet.lint.ExecutePuppetLintAction.findBasedir;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
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
@MimeRegistration(mimeType=PLanguageProvider.MIME_TYPE, service=UpToDateStatusProviderFactory.class)
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
            ExternalProcessBuilder builder = new ExternalProcessBuilder("puppet-lint")
                .workingDirectory(FileUtil.toFile(basedir))
                .redirectErrorStream(true)
                .addArgument(FileUtil.getRelativePath(basedir, fo))
                .addArgument("--log-format")
                .addArgument("%{linenumber}||%{kind}||%{check}||%{message}");
            
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
                        ErrorDescription err = ErrorDescriptionFactory.createErrorDescription(level, message, document, lineNum);
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
                if (project.getLookup().lookup(PuppetProject.class).isModule()) {
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

}
