package com.backend.jobstream;

import io.cucumber.core.options.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runner pour les tests Cucumber/Gherkin avec JUnit 5 (Spring Boot 3+)
 * Pour lancer les tests:
 * - mvn test
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.backend.jobstream.steps")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
public class CucumberTestRunner {
    // Classe de configuration Cucumber
    // Les step definitions sont dans le package com.backend.jobstream.steps
    // Les features sont dans src/test/resources/features
}
