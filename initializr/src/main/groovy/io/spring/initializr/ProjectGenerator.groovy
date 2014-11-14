/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr

import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.Assert

import static io.spring.initializr.support.GroovyTemplate.template

/**
 * Generate a project based on the configured metadata.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @since 1.0
 */
@Slf4j
class ProjectGenerator {

	@Autowired
	InitializrMetadata metadata

	@Value('${TMPDIR:.}')
	String tmpdir

	final Set<ProjectGenerationListener> listeners = []

	private transient Map<String, List<File>> temporaryFiles = [:]

	/**
	 * Generate a Maven pom for the specified {@link ProjectRequest}.
	 */
	byte[] generateMavenPom(ProjectRequest request) {
		def model = initializeModel(request)
		def content = doGenerateMavenPom(model)
		invokeListeners(request)
		content
	}

	/**
	 * Generate a Gradle build file for the specified {@link ProjectRequest}.
	 */
	byte[] generateGradleBuild(ProjectRequest request) {
		def model = initializeModel(request)
		def content = doGenerateGradleBuild(model)
		invokeListeners(request)
		content
	}

	/**
	 * Generate a project structure for the specified {@link ProjectRequest}. Returns
	 * a directory containing the project.
	 */
	File generateProjectStructure(ProjectRequest request) {
		def model = initializeModel(request)

		def dir = File.createTempFile('tmp', '', new File(tmpdir))
		addTempFile(dir.name, dir)
		dir.delete()
		dir.mkdirs()

		if ('gradle'.equals(request.build)) {
			def gradle = new String(doGenerateGradleBuild(model))
			new File(dir, 'build.gradle').write(gradle)
		} else {
			def pom = new String(doGenerateMavenPom(model))
			new File(dir, 'pom.xml').write(pom)
		}

		def language = request.language

		def src = new File(new File(dir, "src/main/$language"), request.packageName.replace('.', '/'))
		src.mkdirs()
		write(src, "Application.$language", model)

		if (request.packaging == 'war') {
			write(src, "ServletInitializer.$language", model)
		}

		def test = new File(new File(dir, "src/test/$language"), request.packageName.replace('.', '/'))
		test.mkdirs()
		if (request.hasWebFacet()) {
			model.testAnnotations = '@WebAppConfiguration\n'
			model.testImports = 'import org.springframework.test.context.web.WebAppConfiguration;\n'
		} else {
			model.testAnnotations = ''
			model.testImports = ''
		}
		write(test, "ApplicationTests.$language", model)

		def resources = new File(dir, 'src/main/resources')
		resources.mkdirs()
		new File(resources, 'application.properties').write('')

		if (request.hasWebFacet()) {
			new File(dir, 'src/main/resources/templates').mkdirs()
			new File(dir, 'src/main/resources/static').mkdirs()
		}

		if (request.hasRaveFacet()) {
			def staticFolder = new File(dir, "src/main/resources/static")

			new File(staticFolder, "index.html").write(template("rave/index.html", model))
			new File(staticFolder, "package.json").write(template("rave/package.json", model))
			new File(staticFolder, "boot.js").write(template("rave/boot.js", model))

			def appFolder = new File(dir, "src/main/resources/static/app")
			appFolder.mkdirs()
			new File(appFolder, "main.js").write(template("rave/app/main.js", model))
		}
		invokeListeners(request)
		dir

	}

	/**
	 * Create a distribution file for the specified project structure
	 * directory and extension
	 */
	File createDistributionFile(File dir, String extension) {
		def download = new File(tmpdir, dir.name + extension)
		addTempFile(dir.name, download)
		download
	}

	/**
	 * Clean all the temporary files that are related to this root
	 * directory.
	 * @see #createDistributionFile
	 */
	void cleanTempFiles(File dir) {
		def tempFiles = temporaryFiles.remove(dir.name)
		if (tempFiles) {
			tempFiles.each { File file ->
				if (file.directory) {
					file.deleteDir()
				} else {
					file.delete()
				}
			}
		}
	}

	private void invokeListeners(ProjectRequest request) {
		listeners.each {
			it.onGeneratedProject(request)
		}
	}

	private Map initializeModel(ProjectRequest request) {
		Assert.notNull request.bootVersion, 'boot version must not be null'
		def model = [:]
		request.resolve(metadata)

		// request resolved so we can log what has been requested
		def dependencies = request.dependencies.collect { it.id }
		log.info("Processing request{type=$request.type, dependencies=$dependencies}")

		request.properties.each { model[it.key] = it.value }
		model
	}

	private byte[] doGenerateMavenPom(Map model) {
		template 'starter-pom.xml', model
	}


	private byte[] doGenerateGradleBuild(Map model) {
		template 'starter-build.gradle', model
	}

	def write(File src, String name, def model) {
		def tmpl = name.endsWith('.groovy') ? name + '.tmpl' : name
		def body = template tmpl, model
		new File(src, name).write(body)
	}

	private void addTempFile(String group, File file) {
		def content = temporaryFiles[group]
		if (!content) {
			content = []
			temporaryFiles[group] = content
		}
		content << file
	}

}
