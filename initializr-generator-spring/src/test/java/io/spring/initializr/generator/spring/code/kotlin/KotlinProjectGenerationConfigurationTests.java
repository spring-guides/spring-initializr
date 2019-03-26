/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spring.code.kotlin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.kotlin.KotlinLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link KotlinProjectGenerationConfiguration}.
 *
 * @author Stephane Nicoll
 */
class KotlinProjectGenerationConfigurationTests {

	private ProjectAssetTester projectTester;

	@BeforeEach
	void setup(@TempDir Path directory) {
		this.projectTester = new ProjectAssetTester().withIndentingWriterFactory()
				.withConfiguration(SourceCodeProjectGenerationConfiguration.class,
						KotlinProjectGenerationConfiguration.class)
				.withDirectory(directory)
				.withBean(KotlinProjectSettings.class,
						() -> new SimpleKotlinProjectSettings("1.2.70"))
				.withDescriptionCustomizer((description) -> {
					description.setLanguage(new KotlinLanguage());
					description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
					description.setBuildSystem(new MavenBuildSystem());
				});
	}

	@Test
	void mainClassIsContributedWhenGeneratingProject() {
		List<String> relativePaths = this.projectTester.generate(new ProjectDescription())
				.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains(Paths.get("src", "main", "kotlin", "com",
				"example", "demo", "DemoApplication.kt").toString());
	}

	@Test
	void testClassIsContributed() {
		ProjectStructure projectStructure = this.projectTester
				.generate(new ProjectDescription());
		List<String> relativePaths = projectStructure.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains(Paths.get("src", "test", "kotlin", "com",
				"example", "demo", "DemoApplicationTests.kt").toString());
		List<String> lines = projectStructure
				.readAllLines(Paths.get("src", "test", "kotlin", "com", "example", "demo",
						"DemoApplicationTests.kt").toString());
		assertThat(lines).containsExactly("package com.example.demo", "",
				"import org.junit.Test", "import org.junit.runner.RunWith",
				"import org.springframework.boot.test.context.SpringBootTest",
				"import org.springframework.test.context.junit4.SpringRunner", "",
				"@RunWith(SpringRunner::class)", "@SpringBootTest",
				"class DemoApplicationTests {", "", "    @Test",
				"    fun contextLoads() {", "    }", "", "}");
	}

	@Test
	void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging() {
		ProjectDescription description = new ProjectDescription();
		description.setPackaging(new WarPackaging());
		description.setApplicationName("KotlinDemoApplication");
		ProjectStructure projectStructure = this.projectTester.generate(description);
		List<String> relativePaths = projectStructure.getRelativePathsOfProjectFiles();
		assertThat(relativePaths).contains(Paths.get("src", "main", "kotlin", "com",
				"example", "demo", "ServletInitializer.kt").toString());
		List<String> lines = projectStructure.readAllLines(Paths.get("src", "main",
				"kotlin", "com", "example", "demo", "ServletInitializer.kt").toString());
		assertThat(lines).containsExactly("package com.example.demo", "",
				"import org.springframework.boot.builder.SpringApplicationBuilder",
				"import org.springframework.boot.web.servlet.support.SpringBootServletInitializer",
				"", "class ServletInitializer : SpringBootServletInitializer() {", "",
				"    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {",
				"        return application.sources(KotlinDemoApplication::class.java)",
				"    }", "", "}");
	}

}
