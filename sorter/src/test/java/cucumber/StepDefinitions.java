package cucumber;

import graphql.schema.GraphQLSchema;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import sortgraphql.SorterService;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/** Step definitions for cucumber tests */
public class StepDefinitions {
  private final PluginParameters.Builder paramBuilder = PluginParameters.builder();
  private final SortingLogger log = mock(SortingLogger.class);
  private String unsortedSchema;

  @ParameterType(value = "true|True|TRUE|false|False|FALSE")
  public Boolean booleanValue(String value) {
    return Boolean.valueOf(value);
  }

  @Given("schema content file {string}")
  public void schemaContentFileBasic_productsGraphqls(String filename) {
    schemaContent(getContentFromFileName(filename));
  }

  @Given("schema content")
  public void schemaContent(String unsortedSchema) {
    this.unsortedSchema = unsortedSchema;
  }

  private String getContentFromFileName(String filename) {
    var schemaStream = StepDefinitions.class.getResourceAsStream(filename);
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

  @Then("sorted schema")
  public void sortedSchema(String expectedSchema) {
    var sorterService = new SorterService();
    PluginParameters pluginParameters =
        paramBuilder.setSchemaFile(new File("name"), emptyList()).build();
    sorterService.setup(log, pluginParameters);
    GraphQLSchema mergedSchema =
        sorterService.createMergedSchema(List.of(unsortedSchema), pluginParameters.schemaFiles);
    var sortedSchema = sorterService.sortSchema(mergedSchema, "name");
    assertThat(sortedSchema, is(expectedSchema));
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
