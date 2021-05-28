package sortgraphql;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.GraphqlTypeComparatorEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.FakeRuntimeWiringFactory;
import sortgraphql.sort.OptionsBuilder;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

import java.io.File;
import java.util.Comparator;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  private final FileUtil fileUtil = new FileUtil();
  private final FakeRuntimeWiringFactory wiringFactory = new FakeRuntimeWiringFactory();

  private SortingLogger log;

  private File schemaFile;
  private boolean createBackupFile;
  private String backupFileExtension;
  private boolean skipUnionTypeSorting;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.schemaFile = pluginParameters.schemaFile;
    this.createBackupFile = pluginParameters.createBackupFile;
    this.backupFileExtension = pluginParameters.backupFileExtension;
    this.skipUnionTypeSorting = pluginParameters.skipUnionTypeSorting;

    fileUtil.setup(pluginParameters);
  }

  public String getSchemaContent() {
    return fileUtil.getSchemaContent();
  }

  public String sortSchema(String schema) {
    var registry = new SchemaParser().parse(schema);
    RuntimeWiring runtimeWiring = wiringFactory.createFakeRuntime(registry);

    var graphQLSchema = new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);

    var options =
        OptionsBuilder.defaultOptions()
            .setDescriptionsAsHashComments(true)
            .setIncludeDirectiveDefinitions(false)
            .setIncludeDefinedDirectiveDefinitions(true);
    
    if (skipUnionTypeSorting) {
      GraphqlTypeComparatorEnvironment environment = GraphqlTypeComparatorEnvironment.newEnvironment().parentType(GraphQLUnionType.class).elementType(GraphQLOutputType.class).build();
      options.addComparatorToRegistry(environment, (Comparator<GraphQLOutputType>) (o1, o2) -> 0)  ;
    }

    return new SchemaPrinter(options.build()).print(graphQLSchema);
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
    log.info(
        String.format(
            "Saved backup of %s to %s%s",
            schemaFile.getAbsolutePath(), schemaFile.getAbsolutePath(), backupFileExtension));
  }

  public void saveSortedContent(String sortedContent) {
    fileUtil.saveSchema(sortedContent);
  }
}
