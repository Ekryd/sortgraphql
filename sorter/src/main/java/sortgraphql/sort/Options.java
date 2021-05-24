package sortgraphql.sort;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphqlTypeComparatorRegistry;

import java.util.function.Predicate;

/** Options to use when printing a schema */
public class Options {

  private final boolean includeIntrospectionTypes;
  private final boolean includeScalars;
  private final boolean useAstDefinitions;
  private final boolean includeSchemaDefinition;
  private final boolean includeDirectiveDefinitions;
  private final boolean includeDefinedDirectiveDefinitions;
  private final boolean descriptionsAsHashComments;
  private final Predicate<GraphQLDirective> includeDirective;
  private final Predicate<GraphQLSchemaElement> includeSchemaElement;
  private final GraphqlTypeComparatorRegistry comparatorRegistry;

  Options(
      boolean includeIntrospectionTypes,
      boolean includeScalars,
      boolean includeSchemaDefinition,
      boolean includeDirectiveDefinitions,
      boolean includeDefinedDirectiveDefinitions,
      boolean useAstDefinitions,
      boolean descriptionsAsHashComments,
      Predicate<GraphQLDirective> includeDirective,
      Predicate<GraphQLSchemaElement> includeSchemaElement,
      GraphqlTypeComparatorRegistry comparatorRegistry) {
    this.includeIntrospectionTypes = includeIntrospectionTypes;
    this.includeScalars = includeScalars;
    this.includeSchemaDefinition = includeSchemaDefinition;
    this.includeDirectiveDefinitions = includeDirectiveDefinitions;
    this.includeDefinedDirectiveDefinitions = includeDefinedDirectiveDefinitions;
    this.includeDirective = includeDirective;
    this.useAstDefinitions = useAstDefinitions;
    this.descriptionsAsHashComments = descriptionsAsHashComments;
    this.comparatorRegistry = comparatorRegistry;
    this.includeSchemaElement = includeSchemaElement;
  }

  public boolean isIncludeIntrospectionTypes() {
    return includeIntrospectionTypes;
  }

  public boolean isIncludeScalars() {
    return includeScalars;
  }

  public boolean isIncludeSchemaDefinition() {
    return includeSchemaDefinition;
  }

  public boolean isIncludeDefinedDirectiveDefinitions() {
    return includeDefinedDirectiveDefinitions;
  }

  public boolean isIncludeDirectiveDefinitions() {
    return includeDirectiveDefinitions;
  }

  public Predicate<GraphQLDirective> getIncludeDirective() {
    return includeDirective;
  }

  public Predicate<GraphQLSchemaElement> getIncludeSchemaElement() {
    return includeSchemaElement;
  }

  public boolean isDescriptionsAsHashComments() {
    return descriptionsAsHashComments;
  }

  public GraphqlTypeComparatorRegistry getComparatorRegistry() {
    return comparatorRegistry;
  }

  public boolean isUseAstDefinitions() {
    return useAstDefinitions;
  }
}
