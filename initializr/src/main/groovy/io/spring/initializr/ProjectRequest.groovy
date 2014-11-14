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

/**
 * A request to generate a project.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @since 1.0
 */
@Slf4j
class ProjectRequest {

	def style = []

	String name
	String type
	String description
	String groupId
	String artifactId
	String version
	String bootVersion
	String packaging
	String language
	String packageName
	String javaVersion

	def dependencies = []
	def facets = []
	def build

	/**
	 * Resolve this instance against the specified {@link InitializrMetadata}
	 */
	void resolve(InitializrMetadata metadata) {
		style = style ?: []
		if (!style.class.isArray() && !(style instanceof Collection)) {
			style = [style]
		}
		dependencies = style.collect {
			def dependency = metadata.getDependency(it)
			if (dependency == null) {
				if (it.contains(':')) {
					throw new InvalidProjectRequestException("Unknown dependency '$it' check project metadata")
				}
				log.warn("No known dependency for style '$it' assuming spring-boot-starter")
				dependency = new InitializrMetadata.Dependency()
				dependency.asSpringBootStarter(it)
			}
			dependency
		}
		dependencies.each {
			it.facets.each {
				if (!facets.contains(it)) {
					facets.add(it)
				}
			}
		}

		if (this.type) {
			InitializrMetadata.Type type = metadata.getType(this.type)
			if (!type) {
				throw new InvalidProjectRequestException("Unknown type '${this.type}' check project metadata")
			}
			String buildTag = type.tags['build']
			if (buildTag) {
				this.build = buildTag
			}
		}
		afterResolution(metadata)
	}

	/**
	 * Update this request once it has been resolved with the specified {@link InitializrMetadata}.
	 */
	protected afterResolution(InitializrMetadata metadata) {
		if (packaging == 'war' && !hasWebFacet()) {
			// Need to be able to bootstrap the web app
			dependencies << metadata.getDependency('web')
			facets << 'web'
		}
		if (dependencies.isEmpty()) {
			addDefaultDependency()
		}
	}

	/**
	 * Add a default dependency if the project does not define any
	 * dependency
	 */
	protected addDefaultDependency() {
		def root = new InitializrMetadata.Dependency()
		root.id = 'root_starter'
		root.asSpringBootStarter('')
		dependencies << root

	}

	/**
	 * Specify if this request has the web facet enabled.
	 */
	boolean hasWebFacet() {
		hasFacet('web')
	}

	/**
	 * Specify if this request has the JavaScript facet enabled
	 */
	boolean hasRaveFacet() {
		hasFacet('rave')
	}

	/**
	 * Specify if this request has the specified facet enabled
	 */
	boolean hasFacet(String facet) {
		facets.contains(facet)
	}

}
