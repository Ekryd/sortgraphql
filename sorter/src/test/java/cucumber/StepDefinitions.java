package cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import sortgraphql.SorterService;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/** Step definitions for cucumber tests */
public class StepDefinitions {
  private final PluginParameters.Builder paramBuilder = PluginParameters.builder().setGenerationOptions(false, false, true);
  private final SortingLogger log = mock(SortingLogger.class);
  private List<String> unsortedSchemas;
  private PluginParameters pluginParameters;

  @ParameterType(value = "true|True|TRUE|false|False|FALSE")
  public Boolean booleanValue(String value) {
    return Boolean.valueOf(value);
  }

  @Given("skip union type sorting is {booleanValue}")
  public void skipUnionTypeSortingIsTrue(boolean flag) {
    paramBuilder.setSorting(flag, paramBuilder.build().skipFieldArgumentSorting);
  }

  @Given("skip field argument sorting is {booleanValue}")
  public void skipFieldArgumentSorting(boolean flag) {
    paramBuilder.setSorting(paramBuilder.build().skipUnionTypeSorting, flag);
  }

  @When("unsorted schema content")
  public void unsortedSchemaContent(String unsortedSchemaContent) {
    pluginParameters = paramBuilder.setSchemaFile(new File("name"), emptyList()).build();
    this.unsortedSchemas = Collections.singletonList(unsortedSchemaContent);
  }

  @When("unsorted schema file {string}")
  public void unsortedSchemaFile(String unsortedSchemaFilename) {
    pluginParameters =
        paramBuilder.setSchemaFile(new File("name"), emptyList()).build();
    this.unsortedSchemas =
        Collections.singletonList(getContentFromFileName(unsortedSchemaFilename));
  }

  @When("unsorted schema files")
  public void unsortedSchemaFile(List<String> filenames) {
    var files = filenames.stream().map(File::new).collect(Collectors.toList());
    pluginParameters = paramBuilder.setSchemaFile(null, files).build();
    this.unsortedSchemas =
        filenames.stream().map(this::getContentFromFileName).collect(Collectors.toList());
  }

  @Then("sorted schema content")
  public void sortedSchemaContent(String expectedSchemaContent) {
    assertSortedSchema("name", expectedSchemaContent);
  }

  @Then("sorted schema file {string}")
  public void sortedSchemaFile(String expectedSchemaFilename) {
    assertSortedSchema("name", getContentFromFileName(expectedSchemaFilename));
  }

  @Then("sorted schema {string} file {string}")
  public void sortedSchemaFile(String schemaName, String expectedSchemaFilename) {
    assertSortedSchema(schemaName, getContentFromFileName(expectedSchemaFilename));
  }

  private String getContentFromFileName(String filename) {
    var schemaStream = StepDefinitions.class.getResourceAsStream(filename);
    if (schemaStream == null) {
      throw new RuntimeException("Cannot find file: " + filename);
    }
    try {
      return new String(schemaStream.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void assertSortedSchema(String schemaName, String expectedSchemaContent) {
    var sorterService = new SorterService();
    sorterService.setup(log, pluginParameters);
    var mergedSchema =
        sorterService.createMergedSchema(unsortedSchemas, pluginParameters.schemaFiles);
    var sortedSchema = sorterService.sortSchema(mergedSchema, schemaName);
    assertThat(sortedSchema, is(expectedSchemaContent));
  }
}
