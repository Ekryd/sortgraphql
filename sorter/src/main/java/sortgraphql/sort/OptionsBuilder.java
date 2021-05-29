package sortgraphql.sort;

import graphql.schema.*;

import java.util.Comparator;
import java.util.function.Predicate;

public class OptionsBuilder {
  private boolean includeIntrospectionTypes;
  private boolean includeScalars;
  private boolean includeSchemaDefinition;
  private boolean includeDirectiveDefinitions;
  private boolean includeDefinedDirectiveDefinitions;
  private boolean useAstDefinitions;
  private boolean descriptionsAsHashComments;
  private Predicate<GraphQLDirective> includeDirective;
  private Predicate<GraphQLSchemaElement> includeSchemaElement;
  private DefaultGraphqlTypeComparatorRegistry.Builder comparatorRegistryBuilder = DefaultGraphqlTypeComparatorRegistry.newComparators();

  private OptionsBuilder() {}

  /** This will allow you to include introspection types that are contained in a schema */
  public OptionsBuilder setIncludeIntrospectionTypes(boolean includeIntrospectionTypes) {
    this.includeIntrospectionTypes = includeIntrospectionTypes;
    return this;
  }

  /** This will allow you to include scalar types that are contained in a schema */
  public OptionsBuilder setIncludeScalars(boolean includeScalars) {
    this.includeScalars = includeScalars;
    return this;
  }

  /**
   * This will force the printing of the graphql schema definition even if the query, mutation,
   * and/or subscription types use the default names. Some graphql parsers require this information
   * even if the schema uses the default type names. The schema definition will always be printed if
   * any of the query, mutation, or subscription types do not use the default names.
   */
  public OptionsBuilder setIncludeSchemaDefinition(boolean includeSchemaDefinition) {
    this.includeSchemaDefinition = includeSchemaDefinition;
    return this;
  }

  /**
   * This flag controls whether schema printer will include directive definitions at the top of the
   * schema, but does not remove them from the field or type usage.
   *
   * <p>In some schema definitions, like Apollo Federation, the schema should be printed without the
   * directive definitions. This simplified schema is returned by a GraphQL query to other services,
   * in a format that is different that the introspection query.
   *
   * <p>On by default.
   */
  public OptionsBuilder setIncludeDirectiveDefinitions(boolean includeDirectiveDefinitions) {
    this.includeDirectiveDefinitions = includeDirectiveDefinitions;
    return this;
  }

  /**
   * This flag controls whether schema printer will include non-standard directive definitions at
   * the top of the schema, but does not remove them from the field or type usage.
   *
   * <p>In some schema definitions, like Apollo Federation, the schema should be printed without the
   * directive definitions. This simplified schema is returned by a GraphQL query to other services,
   * in a format that is different that the introspection query.
   *
   * <p>On by default.
   */
  public OptionsBuilder setIncludeDefinedDirectiveDefinitions(
      boolean includeDefinedDirectiveDefinitions) {
    this.includeDefinedDirectiveDefinitions = includeDefinedDirectiveDefinitions;
    return this;
  }

  /**
   * Allow to print directives. In some situations, auto-generated schemas contain a lot of
   * directives that make the printout noisy and having this flag would allow cleaner printout. On
   * by default.
   */
  public OptionsBuilder setUseAstDefinitions(boolean useAstDefinitions) {
    this.useAstDefinitions = useAstDefinitions;
    return this;
  }

  /**
   * Descriptions are defined as preceding string literals, however an older legacy versions of SDL
   * supported preceding '#' comments as descriptions. Set this to true to enable this deprecated
   * behavior. This option is provided to ease adoption and may be removed in future versions.
   */
  public OptionsBuilder setDescriptionsAsHashComments(boolean descriptionsAsHashComments) {
    this.descriptionsAsHashComments = descriptionsAsHashComments;
    return this;
  }

  /**
   * This is a Predicate that decides whether a directive element is printed.
   *
   * @param includeDirective the predicate to decide of a directive is printed
   * @return new instance of options
   */
  public OptionsBuilder setIncludeDirective(Predicate<GraphQLDirective> includeDirective) {
    this.includeDirective = includeDirective;
    return this;
  }

  /**
   * This is a general purpose Predicate that decides whether a schema element is printed ever.
   *
   * @param includeSchemaElement the predicate to decide of a schema is printed
   * @return new instance of options
   */
  public OptionsBuilder setIncludeSchemaElement(
      Predicate<GraphQLSchemaElement> includeSchemaElement) {
    this.includeSchemaElement = includeSchemaElement;
    return this;
  }

  /**
   * The comparator registry controls the printing order for registered {@code GraphQLType}s.
   *
   * <p>The default is to sort elements by name but you can put in your own code to decide on the
   * field order
   */
  public <T extends GraphQLType> OptionsBuilder addComparatorToRegistry(GraphqlTypeComparatorEnvironment environment, Comparator<? super T> comparator) {

    @SuppressWarnings("unchecked")
    var clazz = (Class<T>) GraphQLType.class;
    this.comparatorRegistryBuilder.addComparator(environment, clazz, comparator);
    return this;
  }

  public Options build() {
    return new Options(
        includeIntrospectionTypes,
        includeScalars,
        includeSchemaDefinition,
        includeDirectiveDefinitions,
        includeDefinedDirectiveDefinitions,
        useAstDefinitions,
        descriptionsAsHashComments,
        includeDirective,
        includeSchemaElement,
        comparatorRegistryBuilder.build());
  }

  public static OptionsBuilder defaultOptions() {
    return new OptionsBuilder()
        .setIncludeIntrospectionTypes(false)
        .setIncludeScalars(true)
        .setIncludeSchemaDefinition(false)
        .setIncludeDirectiveDefinitions(true)
        .setIncludeDefinedDirectiveDefinitions(false)
        .setUseAstDefinitions(false)
        .setDescriptionsAsHashComments(false)
        .setIncludeDirective(directive -> true)
        .setIncludeSchemaElement(element -> true);
  }
}
