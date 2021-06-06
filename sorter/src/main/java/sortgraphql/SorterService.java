package sortgraphql;

import graphql.language.AbstractDescribedNode;
import graphql.schema.*;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  private final FileUtil fileUtil = new FileUtil();
  private final FakeRuntimeWiringFactory wiringFactory = new FakeRuntimeWiringFactory();

  private SortingLogger log;

  private boolean createBackupFile;
  private String backupFileExtension;
  private boolean skipUnionTypeSorting;
  private boolean skipFieldArgumentSorting;
  private boolean generateSchemaDefinition;
  private boolean generateAllDirectiveDefinitions;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.createBackupFile = pluginParameters.createBackupFile;
    this.backupFileExtension = pluginParameters.backupFileExtension;
    this.skipUnionTypeSorting = pluginParameters.skipUnionTypeSorting;
    this.skipFieldArgumentSorting = pluginParameters.skipFieldArgumentSorting;
    this.generateSchemaDefinition = pluginParameters.generateSchemaDefinition;
    this.generateAllDirectiveDefinitions = pluginParameters.generateAllDirectiveDefinitions;

    fileUtil.setup(pluginParameters);
  }

  public Map<File, String> getSchemaContents(List<File> schemaFiles) {
    return schemaFiles.stream()
        .collect(
            Collectors.toMap(
                file -> file, fileUtil::getSchemaContent, (s1, s2) -> s1, LinkedHashMap::new));
  }

  public GraphQLSchema createMergedSchema(Collection<String> schemaContents, List<File> fileNames) {
    var registry = new TypeDefinitionRegistry();

    var nameIterator = fileNames.iterator();
    var schemaParser = new SchemaParser();
    schemaContents.forEach(
        schemaContent ->
            registry.merge(schemaParser.parse(schemaContent, nameIterator.next().getName())));

    var runtimeWiring = wiringFactory.createFakeRuntime(registry);

    try {
      return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
    } catch (SchemaProblem schemaProblem) {
      throw new FailureException(
          String.format(
              "Cannot process schema from filename '%s', %s",
              fileNames.stream().map(File::toString).collect(joining(", ")),
              schemaProblem.getMessage()));
    }
  }

  public String sortSchema(GraphQLSchema graphQLSchema, String schemaFileName) {
    var options =
        OptionsBuilder.defaultOptions()
            .setIncludeDirectiveDefinitions(generateAllDirectiveDefinitions)
            .setIncludeDefinedDirectiveDefinitions(true)
            .setDescriptionsAsHashComments(true)
            .setIncludeSchemaDefinition(generateSchemaDefinition);

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
    options.setNodeDescriptionFilter(sourceLocationPredicate(schemaFileName));

    return new SchemaPrinter(options.build()).print(graphQLSchema);
  }

  private Predicate<AbstractDescribedNode<?>> sourceLocationPredicate(String schemaFileName) {
    return node -> {
      if (node == null || node.getSourceLocation() == null) {
        // If we cannot find description or source location, just print the node
        return true;
      }
      return schemaFileName.equals(node.getSourceLocation().getSourceName());
    };
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
