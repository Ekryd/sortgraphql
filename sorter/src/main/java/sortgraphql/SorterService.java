package sortgraphql;

import graphql.schema.*;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.FakeRuntimeWiringFactory;
import sortgraphql.sort.OptionsBuilder;
import sortgraphql.sort.SchemaParser;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  private final FileUtil fileUtil = new FileUtil();
  private final FakeRuntimeWiringFactory wiringFactory = new FakeRuntimeWiringFactory();

  private SortingLogger log;

  private boolean createBackupFile;
  private String backupFileExtension;
  private boolean skipUnionTypeSorting;
  private boolean skipFieldArgumentSorting;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.createBackupFile = pluginParameters.createBackupFile;
    this.backupFileExtension = pluginParameters.backupFileExtension;
    this.skipUnionTypeSorting = pluginParameters.skipUnionTypeSorting;
    this.skipFieldArgumentSorting = pluginParameters.skipFieldArgumentSorting;

    fileUtil.setup(pluginParameters);
  }

  public Map<File, String> getSchemaContents(List<File> schemaFiles) {
    return schemaFiles.stream()
        .collect(
            Collectors.toMap(
                file -> file, fileUtil::getSchemaContent, (s1, s2) -> s1, LinkedHashMap::new));
  }

  public GraphQLSchema createMergedSchema(Collection<String> schemaContents, List<File> fileNames) {
    TypeDefinitionRegistry registry = new TypeDefinitionRegistry();

    var nameIterator = fileNames.iterator();
    SchemaParser schemaParser = new SchemaParser();
    schemaContents.forEach(
        schemaContent ->
            registry.merge(schemaParser.parse(schemaContent, nameIterator.next().getName())));

    var runtimeWiring = wiringFactory.createFakeRuntime(registry);

    return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
  }

  public String sortSchema(GraphQLSchema graphQLSchema, String schemaFileName) {
    var options =
        OptionsBuilder.defaultOptions()
            .setIncludeDirectiveDefinitions(false)
            .setIncludeDefinedDirectiveDefinitions(true);

    if (skipUnionTypeSorting) {
      var environment =
          GraphqlTypeComparatorEnvironment.newEnvironment()
              .parentType(GraphQLUnionType.class)
              .elementType(GraphQLOutputType.class)
              .build();
      options.addComparatorToRegistry(environment, (Comparator<GraphQLOutputType>) (o1, o2) -> 0);
    }
    if (skipFieldArgumentSorting) {
      var environment =
          GraphqlTypeComparatorEnvironment.newEnvironment()
              .parentType(GraphQLFieldDefinition.class)
              .elementType(GraphQLArgument.class)
              .build();
      options.addComparatorToRegistry(environment, (Comparator<GraphQLArgument>) (o1, o2) -> 0);
    }

    return new SchemaPrinter(options.build()).print(graphQLSchema);
  }

  public boolean isSchemaSorted(String schemaContent, String sortedContent) {
    return schemaContent.equals(sortedContent);
  }

  public void createBackupFile(File schemaFile) {
    if (!createBackupFile) {
      return;
    }
    if (backupFileExtension.trim().length() == 0) {
      throw new FailureException("Could not create backup file, extension name was empty");
    }
    fileUtil.backupFile(schemaFile);
    log.info(
        String.format(
            "Saved backup of %s to %s%s",
            schemaFile.getAbsolutePath(), schemaFile.getAbsolutePath(), backupFileExtension));
  }

  public void saveSortedContent(String sortedContent, File schemaFile) {
    fileUtil.saveSchema(sortedContent, schemaFile);
  }
}
