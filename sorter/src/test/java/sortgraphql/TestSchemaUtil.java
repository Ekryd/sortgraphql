package sortgraphql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

/** Utility class for testing to file system */
public class TestSchemaUtil {
  private final SorterImpl sorter;
  private final SortingLogger log = mock(SortingLogger.class);
  private final File testSchemaFile;
  private final File originalSchemaFile;
  private final File backupSchemaFile;
  private final PluginParameters.Builder pluginParameterBuilder;

  public TestSchemaUtil(String schemaFileName, String backupFileExtension) throws IOException {
    testSchemaFile = new File("target/testSchema" + System.currentTimeMillis() + ".graphqls");
    if (testSchemaFile.exists()) {
      assertThat(testSchemaFile.delete(), is(true));
    }
    originalSchemaFile = new File("src/test/resources/" + schemaFileName);
    FileUtils.copyFile(originalSchemaFile, testSchemaFile);

    pluginParameterBuilder =
        PluginParameters.builder()
            .setSchemaFile(testSchemaFile, null)
            .setEncoding("UTF-8")
            .setBackup(true, backupFileExtension)
            .setGenerationOptions(false, false, true);

    sorter = new SorterImpl();
    backupSchemaFile = new File(testSchemaFile.getAbsolutePath() + backupFileExtension);
  }

  public void sortSchemas() {
    sorter.setup(log, pluginParameterBuilder.build());
    sorter.sortSchemas();
  }

  public PluginParameters.Builder getPluginParameterBuilder() {
    return pluginParameterBuilder;
  }

  public SortingLogger getLog() {
    return log;
  }

  public File getTestSchemaFile() {
    return testSchemaFile;
  }

  public String getTestSchemaContent() throws IOException {
    return FileUtils.readFileToString(testSchemaFile, StandardCharsets.UTF_8);
  }

  public String getOriginalSchemaContent() throws IOException {
    return FileUtils.readFileToString(originalSchemaFile, StandardCharsets.UTF_8);
  }

  public File getBackupSchemaFile() {
    return backupSchemaFile;
  }

  public String getBackupSchemaContent() throws IOException {
    return FileUtils.readFileToString(backupSchemaFile, StandardCharsets.UTF_8);
  }

  public String getExpectedSchemaContent(String expectedFilename) throws IOException {
    var fileContent = FileUtils.readFileToString(
        new File("src/test/resources/" + expectedFilename), StandardCharsets.UTF_8);
    return fileContent.replace(System.getProperty("line.separator"), "\n");
  }
}
