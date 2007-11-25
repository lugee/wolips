package org.objectstyle.wolips.eogenerator.core.runner;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.objectstyle.wolips.eogenerator.core.Activator;
import org.objectstyle.wolips.eogenerator.core.model.EOGeneratorModel;
import org.objectstyle.wolips.eogenerator.core.model.EOModelReference;
import org.objectstyle.wolips.eogenerator.core.model.IEOGeneratorRunner;
import org.objectstyle.wolips.eogenerator.core.model.EOGeneratorModel.Define;
import org.objectstyle.wolips.eomodeler.core.model.EOEntity;
import org.objectstyle.wolips.eomodeler.core.model.EOModel;
import org.objectstyle.wolips.eomodeler.core.model.EOModelGroup;
import org.objectstyle.wolips.eomodeler.core.model.EOModelVerificationFailure;
import org.objectstyle.wolips.thirdparty.velocity.resourceloader.ResourceLoader;

public class VelocityEOGeneratorRunner implements IEOGeneratorRunner {
	public static class ConsoleLogger implements LogSystem {
		public void init(RuntimeServices runtimeservices) throws Exception {
			// DO NOTHING
		}

		public void logVelocityMessage(int i, String s) {
			System.out.println("ConsoleLogger.logVelocityMessage: " + i + ", " + s);
		}
	}

	public boolean generate(EOGeneratorModel eogeneratorModel, StringBuffer results) throws Exception {
		boolean showResults = false;
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, org.apache.velocity.runtime.log.NullLogSystem.class.getName());
		// velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		// ConsoleLogger.class.getName());
		StringBuffer templatePaths = new StringBuffer();
		templatePaths.append(".");
		String templatePath = eogeneratorModel.getTemplateDir();
		String superclassTemplateName;
		String subclassTemplateName;
		if (templatePath != null && templatePath.length() > 0) {
			templatePaths.append(",");
			File templateFolder = new File(templatePath);
			if (!templateFolder.isAbsolute()) {
				templateFolder = new File(eogeneratorModel.getProject().getLocation().toFile(), templatePath);
			}
			templatePaths.append(templateFolder.getAbsolutePath());
			superclassTemplateName = eogeneratorModel.getJavaTemplate("_Entity.java");
			subclassTemplateName = eogeneratorModel.getSubclassJavaTemplate("Entity.java");
		}
		else {
			superclassTemplateName = "_Entity.java";
			subclassTemplateName = "Entity.java";
		}
		velocityEngine.setProperty("resource.loader", "file,wolips");
		velocityEngine.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
		velocityEngine.setProperty("file.resource.loader.path", templatePaths.toString());
		velocityEngine.setProperty("wolips.resource.loader.class", ResourceLoader.class.getName());
		velocityEngine.setProperty("wolips.resource.loader.bundle", Activator.getDefault().getBundle());
		// velocityEngine.setProperty("class.resource.loader.class",
		// EOGeneratorResourceLoader.class.getName());

		velocityEngine.init();
		VelocityContext context = new VelocityContext();

		List<EOModel> models = new LinkedList<EOModel>();
		EOModelGroup modelGroup = new EOModelGroup();
		modelGroup.setPrefix(eogeneratorModel.getPrefix());
		modelGroup.setEOGenericRecordClassName(eogeneratorModel.getDefineValueNamed("EOGenericRecord"));
		for (EOModelReference modelRef : eogeneratorModel.getModels()) {
			String modelPath = modelRef.getPath(null);
			File modelFile = new File(modelPath);
			if (!modelFile.isAbsolute()) {
				modelFile = new File(eogeneratorModel.getProject().getLocation().toFile(), modelPath);
			}
			EOModel model = modelGroup.loadModelFromURL(modelFile.toURL());
			models.add(model);
		}
		for (EOModelReference modelRef : eogeneratorModel.getRefModels()) {
			String modelPath = modelRef.getPath(null);
			File modelFile = new File(modelPath);
			if (!modelFile.isAbsolute()) {
				modelFile = new File(eogeneratorModel.getProject().getLocation().toFile(), modelPath);
			}
			modelGroup.loadModelFromURL(modelFile.toURL());
		}
		Set<EOModelVerificationFailure> failures = new HashSet<EOModelVerificationFailure>();
		modelGroup.resolve(failures);
		modelGroup.verify(failures);
		
		for (EOModelVerificationFailure failure : failures) {
			if (!failure.isWarning()) {
				results.append("Error: " + failure.getMessage() + "\n");
				showResults = true;
			}
		}

		File destination = new File(eogeneratorModel.getDestination());
		if (!destination.isAbsolute()) {
			destination = new File(eogeneratorModel.getProject().getLocation().toFile(), eogeneratorModel.getDestination());
		}
		if (!destination.exists()) {
			if (!destination.mkdirs()) {
				throw new IOException("Failed to create destination '" + destination + "'.");
			}
		}
		File subclassDestination = new File(eogeneratorModel.getSubclassDestination());
		if (!subclassDestination.isAbsolute()) {
			subclassDestination = new File(eogeneratorModel.getProject().getLocation().toFile(), eogeneratorModel.getSubclassDestination());
		}
		if (!subclassDestination.exists()) {
			if (!subclassDestination.mkdirs()) {
				throw new IOException("Failed to create subclass destination '" + subclassDestination + "'.");
			}
		}

		context.put("eogeneratorModel", eogeneratorModel);
		for (Define define : eogeneratorModel.getDefines()) {
			context.put(define.getName(), define.getValue());
		}
		for (EOModel model : models) {
			System.out.println("Generating " + model.getName() + " ...");
			context.put("model", model);

			for (EOEntity entity : model.getEntities()) {
				System.out.println("Generating " + model.getName() + "." + entity.getName() + " ...");
				context.put("entity", entity);

				String classNameWithPackage = entity.getClassName();
				if (classNameWithPackage != null && !"EOGenericRecord".equals(classNameWithPackage) && !"com.webobjects.control.EOGenericRecord".equals(classNameWithPackage)) {
					String prefixClassNameWithPackage = entity.getPrefixClassName();

					String subclassFilePath = classNameWithPackage.replace('.', '/') + ".java";
					File subclassFile = new File(subclassDestination, subclassFilePath);

					String superclassFilePath = prefixClassNameWithPackage.replace('.', '/') + ".java";
					File superclassFile = new File(destination, superclassFilePath);

					context.put("className", classNameWithPackage);
					context.put("prefixClassName", prefixClassNameWithPackage);
					context.put("packageName", entity.getPackageName());
					context.put("classNameWithoutPackage", entity.getClassNameWithoutPackage());
					context.put("prefixClassNameWithoutPackage", entity.getPrefixClassNameWithoutPackage());
					File superclassFolder = superclassFile.getParentFile();
					if (!superclassFolder.exists()) {
						if (!superclassFolder.mkdirs()) {
							throw new IOException("Unable to make superclass folder '" + superclassFolder + "'.");
						}
					}
					VelocityEOGeneratorRunner.writeTemplate(velocityEngine, context, superclassTemplateName, superclassFile);

					File subclassFolder = subclassFile.getParentFile();
					if (!subclassFolder.exists()) {
						if (!subclassFolder.mkdirs()) {
							throw new IOException("Unable to make subclass folder '" + superclassFolder + "'.");
						}
					}
					if (!subclassFile.exists()) {
						VelocityEOGeneratorRunner.writeTemplate(velocityEngine, context, subclassTemplateName, subclassFile);
					}
				}
			}
		}
		return showResults;
	}

	public static void writeTemplate(VelocityEngine engine, VelocityContext context, String templateName, File outputFile) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
		Template template;
		try {
			template = engine.getTemplate(templateName);
		}
		catch (ResourceNotFoundException e) {
			throw new Exception("Failed to load the template '" + templateName + "'.  Check your model's eogen file to make sure that it specifies the correct template folder and template names.");
		}
		if (!outputFile.getParentFile().exists()) {
			if (!outputFile.getParentFile().mkdirs()) {
				throw new IOException("Unable to create the folder " + outputFile.getParentFile() + ".");
			}
		}

		boolean templateChanged = true;
		if (!outputFile.exists()) {
			FileWriter newFileWriter = new FileWriter(outputFile);
			BufferedWriter newFileBufferedWriter = new BufferedWriter(newFileWriter);
			try {
				template.merge(context, newFileBufferedWriter);
			} finally {
				newFileBufferedWriter.close();
			}
		} else {
			ByteArrayOutputStream newFileContentsStream = new ByteArrayOutputStream();
			Writer newFileContentsWriter = new OutputStreamWriter(newFileContentsStream);
			try {
				template.merge(context, newFileContentsWriter);
			} finally {
				newFileContentsWriter.close();
			}
			
			byte[] newFileContents = newFileContentsStream.toByteArray();
			if (outputFile.exists()) {
				FileInputStream fis = new FileInputStream(outputFile);
				int bytesRemaining = (int) outputFile.length();
				if (bytesRemaining == newFileContents.length) {
					byte[] oldFileContents;
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream((int) outputFile.length());
						byte[] buf = new byte[4096];
						while (bytesRemaining > 0) {
							int bytesRead = fis.read(buf, 0, Math.min(buf.length, bytesRemaining));
							bos.write(buf, 0, bytesRead);
							bytesRemaining -= bytesRead;
						}
						oldFileContents = bos.toByteArray();
					} finally {
						fis.close();
					}
					
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					byte[] oldMD5 = md5.digest(oldFileContents);
					md5.reset();
					byte[] newMD5 = md5.digest(newFileContents);
					md5.reset();

					if (oldMD5.length == newMD5.length) {
						templateChanged = false;
						for (int i = 0; !templateChanged && i < oldMD5.length; i++) {
							if (oldMD5[i] != newMD5[i]) {
								templateChanged = true;
							}
						}
					}
				}
			}

			if (templateChanged) {
				FileOutputStream newFileStream = new FileOutputStream(outputFile);
				BufferedOutputStream newFileBufferedStream = new BufferedOutputStream(newFileStream);
				try {
					newFileBufferedStream.write(newFileContents);
				} finally {
					newFileBufferedStream.close();
				}
			} else {
				System.out.println("EOGenerator.writeTemplate: skipping " + outputFile);
			}
		}
	}
}