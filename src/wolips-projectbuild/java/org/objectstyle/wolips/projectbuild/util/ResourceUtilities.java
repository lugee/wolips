/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
 
package org.objectstyle.wolips.projectbuild.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Harald Niesche
 *
 */
public class ResourceUtilities {

  /** 
   * create a Folder recursively 
   * @param f the Folder to be created
   * @param m a ProgressMonitor
   */
  public static void createFolder (IFolder f, IProgressMonitor m) 
    throws CoreException
  {
    if (f.exists()) {
      return;
    }
    IContainer parent = f.getParent ();
    if (!f.getParent().exists()) {
      if (parent instanceof IFolder) {
        createFolder((IFolder)parent, m);
      }
    }
    f.create(true, true, m);
  }


  /** 
   * check if a folder exists under a path, create it if necessary
   * @param path the path to the folder to be created (relative to the workspace root or absolute)
   * @param m a ProgressMonitor
   */
  public static boolean checkDir (IPath path, IProgressMonitor m) 
    throws CoreException 
  {
    boolean result = true;
    
    IFolder f = getWorkspaceRoot().getFolder (path);
    if (!f.exists()) {
      createFolder (f, m);
      result = false;
    }
    
    return (result);
  }

  /**
   * checks if a path is fit to be used as destination for a copy operation
   * if not, the destination is prepared to be used as destination
   * (i.e., existing files and folders are deleted, the parent path is created,
   * if necessary)
   * @param path the candidate destination path
   * @param m a ProgressMonitor
   */
  public static void checkDestination (IPath path, IProgressMonitor m) throws CoreException {
    if (checkDir (path.removeLastSegments(1), m)) {
      IResource res  = getWorkspaceRoot().findMember(path);
      if (null != res && res.exists()) {
        res.delete(true, m);
        //res.refreshLocal(IResource.DEPTH_ONE, m);
      }
    }
  }

  public static IWorkspace getWorkspace () {
    return ResourcesPlugin.getWorkspace();
  }

  public static IWorkspaceRoot getWorkspaceRoot () {
    return ResourcesPlugin.getWorkspace().getRoot();
  }
  
  protected ResourceUtilities () {}
}
