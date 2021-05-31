package sortgraphql;

import graphql.schema.GraphQLSchema;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.File;
import java.util.List;
import java.util.Map;

/** The implementation of the Mojo (Maven plugin) that sorts the schema file. */
public class SorterImpl {
  private final SorterService sorterService = new SorterService();

  private SortingLogger log;
  private List<File> schemaFiles;
  private Map<File, String> schemaContents;
  private GraphQLSchema mergedSchema;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.schemaFiles = pluginParameters.schemaFiles;

    sorterService.setup(log, pluginParameters);
  }

  public void sortSchemas() {
    schemaContents = sorterService.getSchemaContents(schemaFiles);
    mergedSchema = sorterService.createMergedSchema(schemaContents.values(), schemaFiles);
    schemaFiles.forEach(this::sortSchema);
  }

  private void sortSchema(File schemaFile) {
    log.info("Sorting file " + schemaFile.getAbsolutePath());

    var sortedContent = sorterService.sortSchema(mergedSchema, schemaFile.getName());

    if (sorterService.isSchemaSorted(schemaContents.get(schemaFile), sortedContent)) {
      log.info("Schema file is already sorted, exiting");
      return;
    }

    sorterService.createBackupFile(schemaFile);
    sorterService.saveSortedContent(sortedContent, schemaFile);
    log.info("Saved sorted schema file to " + schemaFile.getAbsolutePath());
  }
}
