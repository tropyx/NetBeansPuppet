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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class)
public class PuppetProjectFactory implements ProjectFactory {

    public static final String PROJECT_FILE = "manifests/site.pp";
    public static final String MODULE_FILE = "manifests/init.pp";

    //Specifies when a project is a project, i.e.,
    //if a site.pp is present in a manigests folder
    //then it's probably a set of puppet configuration files:
    @Override
    public boolean isProject(FileObject projectDirectory) {
        return isSite(projectDirectory) || isModule(projectDirectory);
    }

    public static boolean isModule(FileObject projectDirectory) {
        return projectDirectory.getFileObject(MODULE_FILE) != null;
    }

    public static boolean isSite(FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_FILE) != null;
    }


    //Specifies when the project will be opened, i.e., if the project exists:
    @Override
    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
        return isProject(dir) ? new PuppetProject(dir, state, isSite(dir)) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        // leave unimplemented for the moment
    }

}