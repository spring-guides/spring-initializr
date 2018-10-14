package {{packageName}}

{{testImports}}
{{#newTestInfrastructure}}
{{^jupiterAvailable}}
@RunWith(SpringRunner::class)
{{/jupiterAvailable}}
@SpringBootTest
{{/newTestInfrastructure}}
{{^newTestInfrastructure}}
@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf({{applicationName}}::class))
{{/newTestInfrastructure}}
{{testAnnotations}}class {{applicationName}}Tests {

	@Test
	fun contextLoads() {
	}

}
