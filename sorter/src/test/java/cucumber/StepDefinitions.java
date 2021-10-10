package cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import sortgraphql.SorterImpl;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/** Step definitions for cucumber tests */
public class StepDefinitions {
  private final PluginParameters.Builder paramBuilder =
      PluginParameters.builder().setGenerationOptions(false, false, true).setEncoding("UTF-8");
  private final SortingLogger log = mock(SortingLogger.class);
  private final Map<String, Path> tempFiles = new LinkedHashMap<>();
  private SorterImpl sorter;

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

  @Given("schema content")
  public void schemaContent(String content) {
    storeSchemaFile("filename", content);
  }

  @Given("schema files")
  public void schemaFiles(List<String> filenames) {
    filenames.forEach(this::schemaFile);
  }

  @Given("schema file {string}")
  public void schemaFile(String filename) {
    var contentFromFileName = getContentFromFileName(filename);
    storeSchemaFile(filename, contentFromFileName);
  }

  @When("sorting")
  public void sorting() {
    sorter = new SorterImpl();

    sorter.setup(log, paramBuilder.build());

    sorter.sortSchemas();
  }

  @Then("schema file {string} will be {string}")
  public void schemaFileWillBe(String filename, String expectedFilenameContent) throws IOException {
    if (sorter == null) {
      fail("Missed the sorting step");
    }
    var expectedSchemaContent = getContentFromFileName(expectedFilenameContent);
    assertThat(Files.readString(tempFiles.get(filename)), is(expectedSchemaContent));
  }

  @Then("schema content will be")
  public void schemaFileWillBe(String expectedSchemaContent) throws IOException {
    if (sorter == null) {
      fail("Missed the sorting step");
    }
    assertThat(Files.readString(tempFiles.get("filename")), is(expectedSchemaContent));
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

  private void storeSchemaFile(String filename, String contentFromFileName) {
    Path tempFile;
    try {
      tempFile = Files.createTempFile(filename, null);
      if (tempFiles.containsKey(filename)) {
        fail("Trying to add same file twice to test: " + filename);
      }
      tempFiles.put(filename, tempFile);
      Files.write(tempFile, contentFromFileName.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var oldSchemaFiles = paramBuilder.build().schemaFiles;
    if (oldSchemaFiles == null || oldSchemaFiles.isEmpty()) {
      paramBuilder.setSchemaFile(tempFile.toFile(), null);
    } else {
      var newFiles = new ArrayList<>(oldSchemaFiles);
      newFiles.add(tempFile.toFile());
      paramBuilder.setSchemaFile(null, newFiles);
    }
  }
}
