package runner;

import io.cucumber.core.options.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

/* Below block is for Junit4 */
//@RunWith(Cucumber.class)
//@CucumberOptions(
//        features = "src/test/aft/resources/features",
//        glue = "stepdefinition",
//        stepNotifications = true
//)

/* Block for Junit5 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "stepdefinition")
@ConfigurationParameter(key = Constants.FEATURES_PROPERTY_NAME, value = "src/test/aft/resources/features")
public class OrderTestRunnerIT {
}
