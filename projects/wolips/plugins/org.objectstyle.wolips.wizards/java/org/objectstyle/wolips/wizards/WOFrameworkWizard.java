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
package org.objectstyle.wolips.wizards;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.objectstyle.wolips.core.logging.WOLipsLog;
import org.objectstyle.wolips.core.project.ant.RunAnt;
import org.objectstyle.wolips.core.resources.IWOLipsModel;
import org.objectstyle.wolips.templateengine.TemplateDefinition;
import org.objectstyle.wolips.templateengine.TemplateEngine;
/**
 * @author mnolte
 * @author uli
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of
 * type comments go to Window>Preferences>Java>Code Generation.
 */
public class WOFrameworkWizard extends AbstractWOWizard {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectstyle.wolips.wizards.AbstractWOWizard#getWindowTitle()
	 */
	public String getWindowTitle() {
		return Messages.getString("WOFrameworkCreationWizard.title");
	}
	/**
	 * (non-Javadoc) Method declared on IWizard
	 * 
	 * @return
	 */
	public boolean performFinish() {
		boolean success = super.performFinish();
		if (success) {
			IProject project = super.getNewProject();
			String projectName = project.getName();
			String path = project.getLocation().toOSString();
			NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
			try {
				File src = new File(path + File.separator + "src");
				src.mkdirs();
				File bin = new File(path + File.separator + "bin");
				bin.mkdirs();
				File xcode = new File(path + File.separator + projectName
						+ ".xcode");
				xcode.mkdirs();
				project.close(nullProgressMonitor);
				TemplateEngine templateEngine = new TemplateEngine();
				try {
					templateEngine.init();
				} catch (Exception e) {
					WOLipsLog.log(e);
					throw new InvocationTargetException(e);
				}
				templateEngine.getWolipsContext().setProjectName(projectName);
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-.classpath.vm", path, ".classpath"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-.project.vm", path, ".project"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-build.xml.vm", path, "build.xml"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-build.properties.vm", path,
						"build.properties"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-CustomInfo.plist.vm", path,
						"CustomInfo.plist"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-Makefile.vm", path, "Makefile"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-Makefile.postamble.vm", path,
						"Makefile.postamble"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-Makefile.preamble.vm", path,
						"Makefile.preamble"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-PB.project.vm", path, "PB.project"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-Properties.vm", path, "Properties"));
				templateEngine.addTemplate(new TemplateDefinition(
						"woframework-project.pbxproj.vm", path + File.separator
								+ projectName + ".xcode", "project.pbxproj"));
				templateEngine.run();
				project.open(nullProgressMonitor);
				RunAnt runAnt = new RunAnt();
				runAnt.asAnt(path + File.separator + IWOLipsModel.DEFAULT_BUILD_FILENAME, null, null);
				project.refreshLocal(IResource.DEPTH_INFINITE,
						nullProgressMonitor);
			} catch (Exception e) {
				WOLipsLog.log(e);
				success = false;
			}
		}
		return success;
	}
}
