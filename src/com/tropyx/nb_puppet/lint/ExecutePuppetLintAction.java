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
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
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
        ExternalProcessBuilder builder = new ExternalProcessBuilder("puppet-lint")
                .workingDirectory(FileUtil.toFile(context.getPrimaryFile().getParent()))
                .addArgument(context.getPrimaryFile().getNameExt())
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
}
