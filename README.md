Puppet Configuration Editor
==============

This is a module for editing Puppet configurations.  It identifies a puppet config tree by the presence of a manifests/site.pp file.
<br>
This lets you use IDE tools like inbuilt git/subversion/mercurial support etc. to edit your puppet files.

<dl>
    <dt>New in 1.1-mkleint version (in mkleint/NetBeansPuppet fork for now)</dt>

   <dd>Puppet file coloring</dd>
   <dd>Hyperlinking to variable, class definitions</dd>
   <dd>Puppet link errors/warnings in the editor</dd>
   <dd>Improved project support (Find, Select in projects work, version control actions on project popup)</dd>    
   <dd>Editor tabs with module name included</dd>
   <dd>Works with NetBeans 8.0+</dd>
   <dd><a href="https://www.codeship.io/projects/41169"><img src="https://www.codeship.io/projects/fc108f80-35b9-0132-1d49-7a12fe8c1dfc/status">Codeship Status for mkleint/NetBeansPuppet</a>
</dl>

<dl>
  <dt>To Install:</dt>
  <dd>Download the latest release <a href="https://github.com/mkleint/NetBeansPuppet/releases/tag/v1.2.1">nbpuppet-1.2.1-mkleint</a> to your local machine
      
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
