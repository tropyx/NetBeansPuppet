package com.tropyx.nb_puppet.nodes;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import com.tropyx.nb_puppet.PuppetProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@NodeFactory.Registration(projectType = "com-tropyx-nb_project", position = 10)
public class ManifestsNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        PuppetProject p = project.getLookup().lookup(PuppetProject.class);
        assert p != null;
        return new ManifestsNodeList(p);
    }

    private class ManifestsNodeList implements NodeList<Node> {

        PuppetProject project;

        public ManifestsNodeList(PuppetProject project) {
            this.project = project;
        }

        @Override
        public List<Node> keys() {
            FileObject manifestsFolder = project.getProjectDirectory().getFileObject("manifests");
            List<Node> result = new ArrayList<Node>();
            if (manifestsFolder != null) {
                for (FileObject manifestsFolderFile : manifestsFolder.getChildren()) {
                    try {
                        result.add(DataObject.find(manifestsFolderFile).getNodeDelegate());
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return result;
        }

        @Override
        public Node node(Node node) {
            return new FilterNode(node);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }
    }
}
