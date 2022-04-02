package sortgraphql;

import graphql.schema.GraphQLSchema;
import java.io.File;
import java.util.List;
import java.util.Map;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

/** The implementation of the Mojo (Maven plugin) that sorts the schema file. */
public class SorterImpl {
  private final SorterService sorterService = new SorterService();

  private SortingLogger log;
  private List<File> schemaFiles;
  private Map<File, String> schemaContents;
  private boolean individualSchemas;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.schemaFiles = pluginParameters.schemaFiles;
    this.individualSchemas = pluginParameters.individualSchemas;

    sorterService.setup(log, pluginParameters);
  }

  public void sortSchemas() {
    schemaContents = sorterService.getSchemaContents(schemaFiles);
    if (individualSchemas) {
      schemaContents.forEach(
          (file, content) -> {
            var mergedSchema = sorterService.createMergedSchema(List.of(content), List.of(file));
            sortSchema(file, mergedSchema);
          });
    } else {
      var mergedSchema = sorterService.createMergedSchema(schemaContents.values(), schemaFiles);
      schemaFiles.forEach(schemaFile -> sortSchema(schemaFile, mergedSchema));
    }
  }

  private void sortSchema(File schemaFile, GraphQLSchema mergedSchema) {
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
