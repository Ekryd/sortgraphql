package cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import sortgraphql.SorterService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definitions for cucumber tests
 */
public class StepDefinitions {
    private String unsortedSchema;
    private URI scenarioUri;

    @Before
    public void beforeCucumberStep(Scenario scenario) {
        scenarioUri = scenario.getUri();
    }

    @Given("schema content")
    public void schemaContent(String unsortedSchema) {
        this.unsortedSchema = unsortedSchema;
    }

    @Then("sorted schema")
    public void sortedSchema(String expectedSchema) {
        String sortedSchema = new SorterService().sortSchema(unsortedSchema);
        assertThat(sortedSchema, is(expectedSchema));
    }

    @Given("schema content file {string}")
    public void schemaContentFileBasic_productsGraphqls(String filename) {
        schemaContent(getContentFromFileName(filename));
    }

    private String getContentFromFileName(String filename) {
        InputStream schemaStream = StepDefinitions.class.getResourceAsStream(filename);
        try {
            return new String(schemaStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Then("sorted schema file {string}")
    public void sortedSchemaFile(String sortedFilename) {
        sortedSchema(getContentFromFileName(sortedFilename));
    }
}
