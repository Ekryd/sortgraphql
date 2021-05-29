package sortgraphql;

import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;

import java.io.File;

/**
 * The implementation of the Mojo (Maven plugin) that sorts the schema file.
 */
public class SorterImpl {
    private final SorterService sorterService = new SorterService();
    
    private SortingLogger log;
    private File schemaFile;

    public void setup(SortingLogger log, PluginParameters pluginParameters) {
        this.log = log;
        this.schemaFile = pluginParameters.schemaFile;

        sorterService.setup(log, pluginParameters);
    }

    public void sortSchema() {
        log.info("Sorting file " + schemaFile.getAbsolutePath());

        var schemaContent = sorterService.getSchemaContent();
        var sortedContent = sorterService.sortSchema(schemaContent);
        if (sorterService.isSchemaSorted(schemaContent, sortedContent)) {
            log.info("Schema file is already sorted, exiting");
            return;
        }
        sorterService.createBackupFile();
        sorterService.saveSortedContent(sortedContent);
        log.info("Saved sorted schema file to " + schemaFile.getAbsolutePath());
    }
}
