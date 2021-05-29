package cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import sortgraphql.SorterService;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/** Step definitions for cucumber tests */
public class StepDefinitions {
  private final PluginParameters.Builder paramBuilder = PluginParameters.builder();
  private String unsortedSchema;
  private final SortingLogger log = mock(SortingLogger.class);

  @ParameterType(value = "true|True|TRUE|false|False|FALSE")
  public Boolean booleanValue(String value) {
    return Boolean.valueOf(value);
  }

  @Given("schema content")
  public void schemaContent(String unsortedSchema) {
    this.unsortedSchema = unsortedSchema;
  }

  @Then("sorted schema")
  public void sortedSchema(String expectedSchema) {
    SorterService sorterService = new SorterService();
    sorterService.setup(log, paramBuilder.build());
    String sortedSchema = sorterService.sortSchema(unsortedSchema);
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

  @Given("skip union type sorting is {booleanValue}")
  public void skipUnionTypeSortingIsTrue(boolean flag) {
    paramBuilder.setSorting(flag, paramBuilder.build().skipFieldArgumentSorting);
  }

  @Given("skip field argument sorting is {booleanValue}")
  public void skipFieldArgumentSorting(boolean flag) {
    paramBuilder.setSorting(paramBuilder.build().skipUnionTypeSorting, flag);
  }
}
