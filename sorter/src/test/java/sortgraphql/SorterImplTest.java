package sortgraphql;

import org.junit.jupiter.api.Test;
import sortgraphql.exception.FailureException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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

    final FailureException thrown =
        assertThrows(FailureException.class, util::sortSchemas);

    assertThat(thrown.getMessage(), is("Could not create backup file, extension name was empty"));
  }

  @Test
  void includeSchemaShouldIncludeSchemaDefinitionAtTop() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(true, false);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("cucumber/basic_products_with_schema_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }

  @Test
  void includeAllDirectivesShouldIncludeDirectivesAtTop() throws IOException {
    var util = new TestSchemaUtil("cucumber/basic_products.graphqls", ".test_bak");

    util.getPluginParameterBuilder().setGenerationOptions(false, true);
    util.sortSchemas();

    var expectedSchemaContent =
        util.getExpectedSchemaContent("cucumber/basic_products_with_all_directives_expected.graphqls");

    assertThat(util.getTestSchemaContent(), is(expectedSchemaContent));
  }

}
