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

package com.tropyx.nb_puppet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class AuxPropsImpl implements AuxiliaryProperties {
    private final Project project;
    private final Properties props = new Properties();
    private final AtomicBoolean reload = new AtomicBoolean(true);
    private final File propertiesFile;
    
    private final FileChangeListener listener = new FileChangeAdapter() {

        @Override
        public void fileDataCreated(FileEvent fe) {
            reload.set(true);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            reload.set(true);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reload.set(true);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reload.set(true);
        }
    };

    public AuxPropsImpl(Project p) {
        this.project = p;
        File f = FileUtil.toFile(p.getProjectDirectory());
        propertiesFile = FileUtil.normalizeFile(new File(f, "nb-project.properties"));
        FileUtil.addFileChangeListener(listener, propertiesFile);
    }
    
    @Override
    public synchronized String get(String key, boolean shared) {
        if (shared) {
            checkReload();
            return (String) props.get(key);
        }
        return null;   
    }

    @Override
    public synchronized void put(String key, String value, boolean shared) {
        if (shared) {
            checkReload();
            props.put(key, value);
            triggerSave();
        }
    }

    @Override
    public synchronized Iterable<String> listKeys(boolean shared) {
        if (shared) {
            checkReload();
            List<String> s = new ArrayList<>();
            for (Object p : props.keySet()) {
                if (p != null) {
                    s.add(p.toString());
                }
            }
            return s;
        }
        return Collections.emptyList();
    }

    private void checkReload() {
        assert Thread.holdsLock(this);
        if (reload.compareAndSet(true, false)) {
            props.clear();
            FileObject fo = FileUtil.toFileObject(propertiesFile);
            if (fo != null && fo.isValid()) {
                try {
                    try (InputStream in = fo.getInputStream()) {
                        props.load(in);
                    }
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } 
            }
        }
    }

    private void triggerSave() {
        //TODo coalsce multiple calls in one
        try {
            FileObject fo = FileUtil.toFileObject(propertiesFile);
            if (fo == null) {
                fo = project.getProjectDirectory().createData("nb-project.properties");
            }
            try (OutputStream outputStream = fo.getOutputStream()) {
                props.store(outputStream, "");
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
