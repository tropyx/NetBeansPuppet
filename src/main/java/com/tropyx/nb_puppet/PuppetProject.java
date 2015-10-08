/*
 * Copyright (C) Tropyx Technology Pty Ltd and Michael Lindner 2013
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tropyx.nb_puppet;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


public class PuppetProject implements Project {
    @StaticResource()
    public static final String PUPPET_ICON = "com/tropyx/nb_puppet/resources/puppet_icon.gif";

    public static final String PUPPET_PROJECT_TYPE = "com-tropyx-nb_puppet";
    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;
    private final boolean site;

    PuppetProject(FileObject dir, ProjectState state, boolean isSite) {
        this.projectDir = dir;
        this.state = state;
        this.site = isSite;
    }

    public boolean isSite() {
        return site;
    }
    public boolean isModule() {
        return !site;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lkp == null) {
            lkp = LookupProviderSupport.createCompositeLookup(
                    Lookups.fixed(new Object[]{
                this,
                new Info(),
                new PuppetProjectLogicalView(this),
                new PuppetCustomizerProvider(this),
                new RecoPrivTemplatesImpl(),
                GenericSources.genericOnly(this),
                new AuxPropsImpl(this)
            // new ReportsSubprojectProvider(this)
            }), "Projects/" + PUPPET_PROJECT_TYPE + "/Lookup");
        }
        return lkp;
    }

static final VisibilityQueryDataFilter INSTANCE = new VisibilityQueryDataFilter();

    static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter, DataFilter.FileBased {
        
        private final ChangeSupport changeSupport = new ChangeSupport( this );
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public @Override boolean acceptDataObject(DataObject obj) {
            return acceptFileObject(obj.getPrimaryFile());
        }
        
        public @Override void stateChanged(ChangeEvent e) {
            final Runnable r = new Runnable () {
                public @Override void run() {
                    changeSupport.fireChange();
                }
            };            
            SwingUtilities.invokeLater(r);            
        }        
    
        public @Override void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener( listener );
        }        
                        
        public @Override void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener( listener );
        }

        public @Override boolean acceptFileObject(FileObject fo) {
            return VisibilityQuery.getDefault().isVisible(fo);
        }
        
    }


    class PuppetProjectLogicalView implements LogicalViewProvider {

        private final PuppetProject project;

        public PuppetProjectLogicalView(PuppetProject project) {
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            Lookup lkp = createNodeLookup();
            return new ProjectNode(lkp, project);
        }
        private Lookup createNodeLookup() {
            if (!project.getProjectDirectory().isValid()) {
                return Lookups.fixed(project);
            }
            return Lookups.fixed(project, DataFolder.findFolder( project.getProjectDirectory() ), project.getProjectDirectory());
        }
        
        public Children createChilds(Lookup lkp) {
            DataFolder df = lkp.lookup(DataFolder.class);
            if (df != null) {
                return df.createNodeChildren(INSTANCE);
            }
            return Children.LEAF;
        }

        private final class ProjectNode extends AbstractNode {

            final PuppetProject project;

            public ProjectNode(Lookup lkp, PuppetProject project) {
                super(createChilds(lkp), lkp);
                this.project = project;
                setName(project.getProjectDirectory().toString());
            }

            @Override
            public Action[] getActions(boolean arg0) {
                return CommonProjectActions.forType(PUPPET_PROJECT_TYPE);
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.loadImage(PUPPET_ICON);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return getIcon(type);
            }

            @Override
            public String getDisplayName() {
                return project.getProjectDirectory().getName();
            }
        }

        @Override
        public Node findPath(Node node, Object target)
        {
            if (target instanceof FileObject)
            {
                FileObject fo = (FileObject) target;

                Node[] nodes = node.getChildren().getNodes(true);
                for (Node node1 : nodes)
                {
                    Node found = findNodeByFDObject(node1, fo);
                    if (found != null)
                    {
                        return found;
                    }
                }
            }

            return null;
        }

        private Node findNodeByFDObject(Node node, FileObject fo) {
            FileObject ndfo = node.getLookup().lookup(FileObject.class);
            if (ndfo == null) {
                DataObject dobj = node.getLookup().lookup(DataObject.class);
                if (dobj != null) {
                    ndfo = dobj.getPrimaryFile();
                }
            }
            if (ndfo != null) {
                if ((ndfo.equals(fo))) {
                    return node;
                } else if (FileUtil.isParentOf(ndfo, fo)) {
                    FileObject folder = fo.isFolder() ? fo : fo.getParent();
                    String relPath = FileUtil.getRelativePath(ndfo, folder);
                    List<String> path = new ArrayList<>();
                    StringTokenizer strtok = new StringTokenizer(relPath, "/"); // NOI18N
                    while (strtok.hasMoreTokens()) {
                        String token = strtok.nextToken();
                        path.add(token);
                    }
                    try {
                        Node folderNode = folder.equals(ndfo) ? node : NodeOp.findPath(node, Collections.enumeration(path));
                        if (fo.isFolder()) {
                            return folderNode;
                        } else {
                            Node[] childs = folderNode.getChildren().getNodes(true);
                            for (int j = 0; j < childs.length; j++)
                            {
                                DataObject dobj = childs[j].getLookup().lookup(DataObject.class);
                                if (dobj != null && dobj.getPrimaryFile().getNameExt().equals(fo.getNameExt()))
                                {
                                    return childs[j];
                                }
                            }
                        }
                    } catch (NodeNotFoundException e) {}
                }
            }
            return null;
        }
    }

    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(PUPPET_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return PuppetProject.this;
        }
    }
}
