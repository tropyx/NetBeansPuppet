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

package com.tropyx.nb_puppet.indexer;

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.PuppetProject;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

@ProjectServiceProvider(service = ClassPathProvider.class, projectType = PPConstants.PUPPET_PROJECT_TYPE)
public class CpProvider implements ClassPathProvider {
    private final Project project;
    private final AtomicReference<ClassPath> CP = new AtomicReference<>(null);

    public CpProvider(Project p) {
        this.project = p;
    }

    
    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath cp = CP.get();
            if (cp == null) {
                FileObject fo = project.getProjectDirectory().getFileObject("manifests");
                if (fo != null) {
                    CP.compareAndSet(null, ClassPathSupport.createClassPath(fo, getBootstrapPuppet()));
                    cp = CP.get();
                }
            }
            return cp;
        }
        return ClassPath.EMPTY;
    }

    FileObject getBootstrapPuppet()  {
        File f = InstalledFileLocator.getDefault().locate("modules/puppet42", "com.tropyx.nb_puppet", false);
        return FileUtil.toFileObject(f);
    }

}
