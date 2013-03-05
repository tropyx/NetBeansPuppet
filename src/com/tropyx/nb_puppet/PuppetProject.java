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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class PuppetProject implements Project {

    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;

    PuppetProject(FileObject dir, ProjectState state) {
        this.projectDir = dir;
        this.state = state;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

@Override
public Lookup getLookup() {
    if (lkp == null) {
        lkp = Lookups.fixed(new Object[]{
                    this,
                    new Info(),
                    new PuppetProjectLogicalView(this),
                    new PuppetCustomizerProvider(this),
                   // new ReportsSubprojectProvider(this)
                });
    }
    return lkp;
}

    class PuppetProjectLogicalView implements LogicalViewProvider {

        @StaticResource()
        public static final String PUPPET_ICON = "com/tropyx/nb_puppet/resources/puppet_icon.gif";
        private final PuppetProject project;

        public PuppetProjectLogicalView(PuppetProject project) {
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            try {
                //Obtain the project directory's node:
                FileObject projectDirectory = project.getProjectDirectory();
                DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
                Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
                //Decorate the project directory's node:
                return new ProjectNode(nodeOfProjectFolder, project);
            } catch (DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);
                //Fallback-the directory couldn't be created -
                //read-only filesystem or something evil happened
                return new AbstractNode(Children.LEAF);
            }
        }

        private final class ProjectNode extends FilterNode {

            final PuppetProject project;

            public ProjectNode(Node node, PuppetProject project) throws DataObjectNotFoundException {
                super(node,
                        //NodeFactorySupport.createCompositeChildren(
                        //project,
                        //"Projects/com-tropyx-nb_puppet/Nodes"),
                         new FilterNode.Children(node),
                        new ProxyLookup(
                        new Lookup[]{
                            Lookups.singleton(project),
                            node.getLookup()
                        }));
                this.project = project;
            }

            @Override
            public Action[] getActions(boolean arg0) {
                return new Action[]{
                            CommonProjectActions.newFileAction(),
                            CommonProjectActions.copyProjectAction(),
                            CommonProjectActions.deleteProjectAction(),
                            //CommonProjectActions.customizeProjectAction(),
                            CommonProjectActions.closeProjectAction()

                        };
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
        public Node findPath(Node root, Object target) {
            //leave unimplemented for now
            return null;
        }
    }

    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String PUPPET_ICON = "com/tropyx/nb_puppet/resources/puppet_icon.gif";

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
