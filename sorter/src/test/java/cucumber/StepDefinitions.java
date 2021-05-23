package cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import sortgraphql.SorterService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definitions for cucumber tests
 */
public class StepDefinitions {
    private String unsortedSchema;

    @Given("schema content")
    public void schemaContent(String unsortedSchema) {
        this.unsortedSchema = unsortedSchema;
    }

    @Then("sorted schema")
    public void sortedSchema(String expectedSchema) {
        String sortedSchema = new SorterService().sortSchema(unsortedSchema);
        assertThat(sortedSchema, is(expectedSchema));
    }
}
