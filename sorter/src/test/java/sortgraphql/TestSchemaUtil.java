package sortgraphql;

import org.apache.commons.io.FileUtils;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/** Utility class for testing to file system */
public class TestSchemaUtil {
  private final SorterImpl sorter;
  private final SortingLogger log = mock(SortingLogger.class);
  private final File testSchemaFile;
  private final File originalSchemaFile;
  private final File backupSchemaFile;

  public TestSchemaUtil(String schemaFileName, String backupFileExtension) throws IOException {
    testSchemaFile = new File("target/testSchema" + System.currentTimeMillis() + ".graphqls");
    if (testSchemaFile.exists()) {
      assertThat(testSchemaFile.delete(), is(true));
    }
    originalSchemaFile = new File("src/test/resources/" + schemaFileName);
    FileUtils.copyFile(originalSchemaFile, testSchemaFile);

    var pluginParameters =
        PluginParameters.builder()
            .setSchemaFile(testSchemaFile, null)
            .setEncoding("UTF-8")
            .setBackup(true, backupFileExtension)
            .build();

    sorter = new SorterImpl();
    sorter.setup(log, pluginParameters);
    backupSchemaFile = new File(testSchemaFile.getAbsolutePath() + backupFileExtension);
  }

  public SorterImpl getSorter() {
    return sorter;
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
    return FileUtils.readFileToString(
        new File("src/test/resources/" + expectedFilename), StandardCharsets.UTF_8);
  }
}
