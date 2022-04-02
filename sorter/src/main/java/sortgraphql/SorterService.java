package sortgraphql;

import static java.util.stream.Collectors.joining;

import graphql.language.*;
import graphql.schema.*;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.FakeRuntimeWiringFactory;
import sortgraphql.sort.OptionsBuilder;
import sortgraphql.sort.SchemaParser;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

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
  private boolean descriptionsAsHashComments;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.createBackupFile = pluginParameters.createBackupFile;
    this.backupFileExtension = pluginParameters.backupFileExtension;
    this.skipUnionTypeSorting = pluginParameters.skipUnionTypeSorting;
    this.skipFieldArgumentSorting = pluginParameters.skipFieldArgumentSorting;
    this.generateSchemaDefinition = pluginParameters.generateSchemaDefinition;
    this.generateAllDirectiveDefinitions = pluginParameters.generateAllDirectiveDefinitions;
    this.descriptionsAsHashComments = pluginParameters.descriptionsAsHashComments;

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

    addArtificialQueryTypeIfMissing(registry);

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

  private void addArtificialQueryTypeIfMissing(TypeDefinitionRegistry registry) {
    var queryType = registry.getType("Query");
    if (queryType.isEmpty()) {
      registry.add(
          ObjectTypeDefinition.newObjectTypeDefinition()
              .name("Query")
              .sourceLocation(new SourceLocation(0, 0, "internal_artificial_type"))
              .fieldDefinitions(
                  List.of(new FieldDefinition("internal_artificial_field", new TypeName("Int"))))
              .build());
    }
  }

  public String sortSchema(GraphQLSchema graphQLSchema, String schemaFileName) {
    var options =
        OptionsBuilder.defaultOptions()
            .setIncludeDirectiveDefinitions(generateAllDirectiveDefinitions)
            .setIncludeDefinedDirectiveDefinitions(true)
            .setDescriptionsAsHashComments(descriptionsAsHashComments)
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
