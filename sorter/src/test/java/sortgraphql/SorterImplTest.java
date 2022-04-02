package sortgraphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import sortgraphql.exception.FailureException;

class SorterImplTest {

  @Test
  void realSortShouldCreateSortedFileAndBackupFile() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("cucumber/basic_products_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
    assertThat(util.getBackupSchemaFile().exists(), is(true));
    assertThat(util.getBackupSchemaContent(), is(util.getOriginalSchemaContent()));

    var log = util.getLog();
    verify(log).info("Sorting file " + util.getTestSchemaFile().getAbsolutePath());
    verify(log)
        .info(
            "Saved backup of "
                + util.getTestSchemaFile().getAbsolutePath()
                + " to "
                + util.getBackupSchemaFile().getAbsolutePath());
    verify(log).info("Saved sorted schema file to " + util.getTestSchemaFile().getAbsolutePath());
    verifyNoMoreInteractions(log);
  }

  @Test
  void alreadySortedFileSortShouldJustWriteToLog() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products_expected.graphqls", ".test_bak");

    util.sortSchemas();

    assertThat(util.getBackupSchemaFile().exists(), is(false));

    var log = util.getLog();
    verify(log).info("Sorting file " + util.getTestSchemaFile().getAbsolutePath());
    verify(log).info("Schema file is already sorted, exiting");
    verifyNoMoreInteractions(log);
  }

  @Test
  void emptyBackupExtensionShouldThrowException() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", "");

    final FailureException thrown = assertThrows(FailureException.class, util::sortSchemas);

    assertThat(thrown.getMessage(), is("Could not create backup file, extension name was empty"));
  }

  @Test
  void includeSchemaShouldIncludeSchemaDefinitionAtTop() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(true, false, true);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("basic_products_with_schema_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }

  @Test
  void includeAllDirectivesShouldIncludeDirectivesAtTop() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(false, true, true);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("basic_products_with_all_directives_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }

  @Test
  void noBackupFileShouldNotCreateBackupFile() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setBackup(false, ".test_bak");
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("cucumber/basic_products_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
    assertThat(util.getBackupSchemaFile().exists(), is(false));

    var log = util.getLog();
    verify(log).info("Sorting file " + util.getTestSchemaFile().getAbsolutePath());
    verify(log).info("Saved sorted schema file to " + util.getTestSchemaFile().getAbsolutePath());
    verifyNoMoreInteractions(log);
  }

  @Test
  void brokenSchemaShouldThrowComprehensiveException() throws IOException {
    var util = new TestSchemaUtil("broken_schema.graphqls", ".test_bak");

    final FailureException thrown = assertThrows(FailureException.class, util::sortSchemas);

    assertThat(thrown.getMessage(), startsWith("Cannot parse schema '"));
    assertThat(
        thrown.getMessage(),
        endsWith(
            ".graphqls', Invalid Syntax : There are more tokens in the query that have not been consumed offending token 'topProducts' at line 3 column 5"));
  }

  @Test
  void incompleteSchemaShouldThrowComprehensiveException() throws IOException {
    var util = new TestSchemaUtil("incomplete_schema.graphqls", ".test_bak");

    final FailureException thrown = assertThrows(FailureException.class, util::sortSchemas);

    assertThat(thrown.getMessage(), startsWith("Cannot process schema from filename '"));
    assertThat(
        thrown.getMessage(),
        endsWith(
            ".graphqls', errors=[The field type 'Product' is not present when resolving type 'Query' [@2:1], 'topProducts' [@3:5] tried to use an undeclared directive 'resolve', 'Advertisement' [@7:1] tried to use an undeclared directive 'owner', 'Advertisement' [@7:1] tried to use an undeclared directive 'key']"));
  }

  @Test
  void nonHashCommentsShouldTransformComments() throws IOException {
    var util = new TestSchemaUtil("cucumber/wolfMain.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(false, false, false);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("wolfMain_comments_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }

  @Test
  void descriptionsInSchemaShouldBePreserved() throws IOException {
    var util = new TestSchemaUtil("cucumber/descriptions.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(false, false, false);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("cucumber/descriptions_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }
}
