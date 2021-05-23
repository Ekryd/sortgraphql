package sortgraphql;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

import java.io.File;

/** Contain the concrete methods to sort the schema */
public class SorterService {
    private final FileUtil fileUtil = new FileUtil();

    private SortingLogger log;
    
    private File schemaFile;
    private boolean createBackupFile;
    private String backupFileExtension;

    public void setup(SortingLogger log, PluginParameters pluginParameters) {
        this.log = log;
        this.schemaFile = pluginParameters.schemaFile;
        this.createBackupFile = pluginParameters.createBackupFile;
        this.backupFileExtension = pluginParameters.backupFileExtension;

        fileUtil.setup(pluginParameters);
    }

    public String getSchemaContent() {
        return fileUtil.getSchemaContent();
    }
    
    public String sortSchema(String schema) {
        var typeDefinitionRegistry = new SchemaParser().parse(schema);
        var graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeDefinitionRegistry);

        var options =
                SchemaPrinter.Options.defaultOptions()
                        .descriptionsAsHashComments(true)
                        .includeDirectiveDefinitions(false)
                        .includeDefinedDirectiveDefinitions(true);

        return new SchemaPrinter(options).print(graphQLSchema);
    }

    public boolean isSchemaSorted(String schemaContent, String sortedContent) {
        return schemaContent.equals(sortedContent);
    }

    public void createBackupFile() {
        if (!createBackupFile) {
            return;
        }
        if (backupFileExtension.trim().length() == 0) {
            throw new FailureException("Could not create backup file, extension name was empty");
        }
        fileUtil.backupFile();
        log.info(String.format("Saved backup of %s to %s%s", schemaFile.getAbsolutePath(),
                schemaFile.getAbsolutePath(), backupFileExtension));


    }

    public void saveSortedContent(String sortedContent) {
        fileUtil.saveSchema(sortedContent);
    }
}
