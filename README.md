Puppet Configuration Editor
==============

[![Join the chat at https://gitter.im/tropyx/NetBeansPuppet](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tropyx/NetBeansPuppet?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a module for editing Puppet configurations.  It identifies a puppet config tree by the presence of a manifests/site.pp or manifests/init.pp file.
<br>
This lets you use IDE tools like inbuilt git/subversion/mercurial support etc. to edit your puppet files.

<dl>
   <dt>Features include:</dt>

   <dd><a href="https://github.com/tropyx/NetBeansPuppet/wiki/Editing-Puppet-files">Editor</a> - Puppet file coloring, Editor tabs with module name included</dd>
   <dd><a href="https://github.com/tropyx/NetBeansPuppet/wiki/Editing-Puppet-files">Hyperlinking</a> to variable, class definitions</dd>
   <dd><a href="https://github.com/tropyx/NetBeansPuppet/wiki/Puppet-Lint-errors-and-fixes">Puppet lint errors/warnings</a> in the editor, configuration read from Rakefile</dd>
   <dd><a href="https://github.com/tropyx/NetBeansPuppet/wiki/Puppet-Projects">Projects</a> - Create new Puppet module from template, recognize existing projects by manifests/site.pp or manifests/init.pp presence.</dd>
   <dd>Improved project support (Find, Select in projects work, version control actions on project popup)</dd>    
   <dd>.erb files have mimetype (coloring etc) based on previous extension (.sh.erb/yaml.erb/..)</dd>
   <dd>Works with NetBeans 8.0+</dd>
</dl>

 <a href="https://www.codeship.io/projects/41169"><img src="https://www.codeship.io/projects/fc108f80-35b9-0132-1d49-7a12fe8c1dfc/status">Codeship Status for NetBeansPuppet</a>


<dl>
  <dt>To Install:</dt>
  <dd>The latest version of the plugin should be available on NetBeans update centers for 8.0 and 8.1. It's listed on the <a href="http://plugins.netbeans.org/plugin/60170/?show=true">NetBeans Plugin portal</a> as well</dd>

  <dd>Alternatively Download the latest release <a href="https://github.com/tropyx/NetBeansPuppet/releases/tag/v1.3.0">nbpuppet-1.3.0</a> to your local machine
  add it to your NetBeans IDE with:</dd>
</dl>
<pre>
Tools -> Plugins -> Downloaded -> Add Plugins...
</pre>
<br>
![ScreenShot](https://raw.github.com/tropyx/NetBeansPuppet/master/screenshot-puppetfornetbeans.png)
<br>
*Screenshot showing the puppet manifest tree, pp files, and create file dialog box*
<br>
<h2>License details</h2>
*Copyright (C) Tropyx Technology Pty Ltd and Michael Lindner Febuary 20 2013*

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
