/*
 * Copyright 2012-2018 the original author or authors.
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

package io.spring.initializr.service.extension;

import io.spring.initializr.generator.ProjectRequest;
import io.spring.initializr.generator.ProjectRequestPostProcessor;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.stereotype.Component;

/**
 * A {@link ProjectRequestPostProcessor} that automatically adds "jackson-module-scala"
 * when Scala is used and a dependency has the "json" facet.
 *
 * @author Maciej Kowalski
 */
@Component
class JacksonScalaRequestPostProcessor implements ProjectRequestPostProcessor {

	@Override
	public void postProcessAfterResolution(ProjectRequest request,
			InitializrMetadata metadata) {
		if (request.getFacets().contains("json")
				&& "scala".equals(request.getLanguage())) {
			request.getResolvedDependencies().add(
                Dependency.withId("jackson-module-scala",
			    "com.fasterxml.jackson.module",
                        metadata.getConfiguration().getEnv().getScala().getArtifactId("jackson-module-scala")
                )
            );
		}
	}

}
