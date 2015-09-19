/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tropyx.nb_puppet.lint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "Build",
        id = "com.tropyx.nb_puppet.lint.ExecutePuppetLintAction"
)
@ActionRegistration(
        displayName = "#CTL_ExecutePuppetLintAction"
)
@ActionReference(path = "Loaders/text/x-puppet-manifest/Actions", position = 150)
@Messages("CTL_ExecutePuppetLintAction=Execute Puppet Lint...")
public final class ExecutePuppetLintAction implements ActionListener
{

    private final DataObject context;
    private final RequestProcessor RP = new RequestProcessor(ExecutePuppetLintAction.class);

    public ExecutePuppetLintAction(DataObject context)
    {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev)
    {
        FileObject basedir = findBasedir(context.getPrimaryFile().getParent());
        ExternalProcessBuilder builder = new ExternalProcessBuilder("puppet-lint")
                .workingDirectory(FileUtil.toFile(basedir))
                .addArgument(FileUtil.getRelativePath(basedir, context.getPrimaryFile()))
                .addArgument("--relative") //TODO only for single modules?
                .addArgument("--with-filename");

        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true).controllable(true);

        final ExecutionService service = ExecutionService.newService(builder, descriptor, "puppet-lint");
        RP.post(new Runnable()
        {

            @Override
            public void run()
            {
                Future<Integer> task = service.run();
            }
        });
    }

    static FileObject findBasedir(FileObject folder) {
        Project prj = FileOwnerQuery.getOwner(folder);
        if (prj != null) {
            return prj.getProjectDirectory();
        }
        FileObject parent = folder;
        while (parent != null) {
            if ("manifests".equals(parent.getName())) {
                return parent.getParent().getParent();
            }
            parent = parent.getParent();
        }
        return folder;
    }
}
