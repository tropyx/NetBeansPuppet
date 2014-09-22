/*
 * Copyright (C) 2014 mkleint
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

package com.tropyx.nb_puppet.lexer;

import com.tropyx.nb_puppet.PuppetProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = PuppetProject.PUPPET_PROJECT_TYPE)
public class POH extends ProjectOpenedHook {
    private final Project project;
    private ClassPath cp;

    public POH(Project p) {
        this.project = p;
    }

    
    @Override
    protected void projectOpened() {
        System.out.println("POHOpned");
        if (cp == null) {
            cp = project.getLookup().lookup(ClassPathProvider.class).findClassPath(project.getProjectDirectory(), ClassPath.SOURCE);
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {cp});
        }
    }

    @Override
    protected void projectClosed() {
        if (cp != null) {
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {cp});
            cp = null;
        }
    }

}
