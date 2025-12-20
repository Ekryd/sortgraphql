package sortgraphql.sort;

import static graphql.Directives.DeprecatedDirective;
import static graphql.introspection.Introspection.DirectiveLocation.ARGUMENT_DEFINITION;
import static graphql.introspection.Introspection.DirectiveLocation.ENUM_VALUE;
import static graphql.introspection.Introspection.DirectiveLocation.FIELD_DEFINITION;
import static graphql.introspection.Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION;
import static graphql.util.EscapeUtil.escapeJsonString;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

import graphql.GraphQLContext;
import graphql.PublicApi;
import graphql.execution.ValuesResolver;
import graphql.language.AbstractDescribedNode;
import graphql.language.AstPrinter;
import graphql.language.Description;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnionType;
import graphql.schema.GraphqlTypeComparatorEnvironment;
import graphql.schema.InputValueWithState;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.visibility.GraphqlFieldVisibility;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** This can print an in memory GraphQL schema back to a logical schema definition */
@PublicApi
public class SchemaPrinter {
  //
  // we use this so that we get the simple "@deprecated" as text and not a full exploded
  // text with arguments (but only when we auto add this)
  //
  private static final GraphQLDirective DeprecatedDirective4Printing =
      GraphQLDirective.newDirective()
          .name("deprecated")
          .validLocations(FIELD_DEFINITION, ENUM_VALUE, ARGUMENT_DEFINITION, INPUT_FIELD_DEFINITION)
          .build();

  private final Options options;

  public SchemaPrinter(Options options) {
    this.options = options;
  }

  /**
   * This can print an in memory GraphQL schema back to a logical schema definition
   *
   * @param schema the schema in play
   * @return the logical schema definition
   */
  public String print(GraphQLSchema schema) {
    var sw = new StringWriter();
    var out = new PrintWriter(sw);

    var visibility = schema.getCodeRegistry().getFieldVisibility();

    printSchema(out, schema);

    List<GraphQLNamedType> typesAsList =
        schema.getAllTypesAsList().stream()
            .sorted(Comparator.comparing(GraphQLNamedType::getName))
            .collect(toCollection(ArrayList::new));

    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesTypeName(schema.getQueryType()),
            type -> printObject(out, (GraphQLObjectType) type, visibility));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesTypeName(schema.getMutationType()),
            type -> printObject(out, (GraphQLObjectType) type, visibility));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesTypeName(schema.getSubscriptionType()),
            type -> printObject(out, (GraphQLObjectType) type, visibility));

    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesClass(GraphQLScalarType.class),
            type -> printScalar(out, (GraphQLScalarType) type));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesClass(GraphQLInterfaceType.class),
            type -> printInterface(out, (GraphQLInterfaceType) type, visibility));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesClass(GraphQLUnionType.class),
            type -> printUnion(out, (GraphQLUnionType) type));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesClass(GraphQLInputObjectType.class),
            type -> printInput(out, (GraphQLInputObjectType) type, visibility));
    typesAsList =
        removeMatchingItems(
            typesAsList,
            matchesClass(GraphQLObjectType.class),
            type -> printObject(out, (GraphQLObjectType) type, visibility));
    typesAsList.stream()
        .filter(matchesClass(GraphQLEnumType.class))
        .forEach(type -> printEnum(out, (GraphQLEnumType) type));

    var result = sw.toString();
    if (result.endsWith("\n\n")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  private void printSchema(PrintWriter out, GraphQLSchema schema) {
    if (needsSchemaPrinted(schema)) {
      printSchemaElement(out, schema);
    }

    if (options.isIncludeDirectiveDefinitions()) {
      var directives = getSchemaDirectives(schema);
      if (!directives.isEmpty()) {
        out.append(directiveDefinitions(directives));
      }
    } else if (options.isIncludeDefinedDirectiveDefinitions()) {
      var directives =
          getSchemaDirectives(schema).stream()
              .filter(
                  directive ->
                      directive.getDefinition() != null
                          && directive.getDefinition().getSourceLocation() != null)
              .collect(Collectors.toList());
      if (!directives.isEmpty()) {
        out.append(directiveDefinitions(directives));
      }
    }
  }

  private boolean needsSchemaPrinted(GraphQLSchema schema) {
    var queryType = schema.getQueryType();
    var mutationType = schema.getMutationType();
    var subscriptionType = schema.getSubscriptionType();

    // when serializing a GraphQL schema using the type system language, a
    // schema definition should be omitted if only uses the default root type names.
    var needsSchemaPrinted = options.isIncludeSchemaDefinition();

    if (!needsSchemaPrinted) {
      if (queryType != null && !queryType.getName().equals("Query")) {
        needsSchemaPrinted = true;
      }
      if (mutationType != null && !mutationType.getName().equals("Mutation")) {
        needsSchemaPrinted = true;
      }
      if (subscriptionType != null && !subscriptionType.getName().equals("Subscription")) {
        needsSchemaPrinted = true;
      }
    }
    return needsSchemaPrinted;
  }

  private void printSchemaElement(PrintWriter out, GraphQLSchema schema) {
    // Too much work to replace this deprecation
    //noinspection deprecation
    var schemaDirectives =
        schema.getSchemaDirectives().stream()
            .sorted(Comparator.comparing(GraphQLDirective::getName))
            .toList();

    out.format(
            "schema%s{",
            schemaDirectives.isEmpty()
                ? " "
                : directivesString(GraphQLSchemaElement.class, schemaDirectives))
        .append("\n");

    var queryType = schema.getQueryType();
    if (queryType != null) {
      out.format("  query: %s", queryType.getName()).append("\n");
    }

    var mutationType = schema.getMutationType();
    if (mutationType != null) {
      out.format("  mutation: %s", mutationType.getName()).append("\n");
    }

    var subscriptionType = schema.getSubscriptionType();
    if (subscriptionType != null) {
      out.format("  subscription: %s", subscriptionType.getName()).append("\n");
    }

    out.append("}\n\n");
  }

  private void printScalar(PrintWriter out, GraphQLScalarType type) {
    if (!options.isIncludeScalars() || ScalarInfo.isGraphqlSpecifiedScalar(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }
    printComments(out, type, "");
    out.format(
            "scalar %s%s",
            type.getName(), directivesString(GraphQLScalarType.class, type.getDirectives()))
        .append("\n\n");
  }

  private void printInterface(
      PrintWriter out, GraphQLInterfaceType type, GraphqlFieldVisibility visibility) {
    if (isIntrospectionType(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }
    printComments(out, type, "");
    if (type.getInterfaces().isEmpty()) {
      out.format(
          "interface %s%s",
          type.getName(),
          type.getDirectives().isEmpty()
              ? " "
              : directivesString(GraphQLInterfaceType.class, type.getDirectives()));
    } else {

      var environment =
          GraphqlTypeComparatorEnvironment.newEnvironment()
              .parentType(GraphQLInterfaceType.class)
              .elementType(GraphQLOutputType.class)
              .build();
      var implementsComparator = options.getComparatorRegistry().getComparator(environment);

      var interfaceNames =
          type.getInterfaces().stream().sorted(implementsComparator).map(GraphQLNamedType::getName);
      out.format(
          "interface %s implements %s%s",
          type.getName(),
          interfaceNames.collect(joining(" & ")),
          type.getDirectives().isEmpty()
              ? " "
              : directivesString(GraphQLInterfaceType.class, type.getDirectives()));
    }

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLInterfaceType.class)
            .elementType(GraphQLFieldDefinition.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    printFieldDefinitions(out, comparator, visibility.getFieldDefinitions(type));
    out.append("\n\n");
  }

  private void printUnion(PrintWriter out, GraphQLUnionType type) {
    if (isIntrospectionType(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLUnionType.class)
            .elementType(GraphQLOutputType.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    printComments(out, type, "");
    out.format(
        "union %s%s = ",
        type.getName(), directivesString(GraphQLUnionType.class, type.getDirectives()));
    var types = type.getTypes().stream().sorted(comparator).toList();
    for (var i = 0; i < types.size(); i++) {
      var objectType = types.get(i);
      if (i > 0) {
        out.append(" | ");
      }
      out.append(objectType.getName());
    }
    out.append("\n\n");
  }

  private void printInput(
      PrintWriter out, GraphQLInputObjectType type, GraphqlFieldVisibility visibility) {
    if (isIntrospectionType(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }
    printComments(out, type, "");
    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLInputObjectType.class)
            .elementType(GraphQLInputObjectField.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    out.format(
        "input %s%s",
        type.getName(),
        type.getDirectives().isEmpty()
            ? " "
            : directivesString(GraphQLInputObjectType.class, type.getDirectives()));
    var inputObjectFields = visibility.getFieldDefinitions(type);
    if (!inputObjectFields.isEmpty()) {
      out.append("{\n");
      inputObjectFields.stream()
          .filter(options.getIncludeSchemaElement())
          .sorted(comparator)
          .forEach(
              fd -> {
                printComments(out, fd, "  ");
                out.format("  %s: %s", fd.getName(), typeString(fd.getType()));
                if (fd.hasSetDefaultValue()) {
                  var defaultValue = fd.getInputFieldDefaultValue();
                  var astValue = printAst(defaultValue, fd.getType());
                  out.format(" = %s", astValue);
                }
                out.format(directivesString(GraphQLInputObjectField.class, fd.getDirectives()));
                out.append("\n");
              });
      out.append("}");
    }
    out.append("\n\n");
  }

  private void printObject(
      PrintWriter out, GraphQLObjectType type, GraphqlFieldVisibility visibility) {
    if (isIntrospectionType(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }
    printComments(out, type, "");
    if (type.getInterfaces().isEmpty()) {
      out.format(
          "type %s%s",
          type.getName(),
          type.getDirectives().isEmpty()
              ? " "
              : directivesString(GraphQLObjectType.class, type.getDirectives()));
    } else {

      var environment =
          GraphqlTypeComparatorEnvironment.newEnvironment()
              .parentType(GraphQLObjectType.class)
              .elementType(GraphQLOutputType.class)
              .build();
      var implementsComparator = options.getComparatorRegistry().getComparator(environment);

      var interfaceNames =
          type.getInterfaces().stream().sorted(implementsComparator).map(GraphQLNamedType::getName);
      out.format(
          "type %s implements %s%s",
          type.getName(),
          interfaceNames.collect(joining(" & ")),
          type.getDirectives().isEmpty()
              ? " "
              : directivesString(GraphQLObjectType.class, type.getDirectives()));
    }

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLObjectType.class)
            .elementType(GraphQLFieldDefinition.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    printFieldDefinitions(out, comparator, visibility.getFieldDefinitions(type));
    out.append("\n\n");
  }

  private void printEnum(PrintWriter out, GraphQLEnumType type) {
    if (isIntrospectionType(type)) {
      return;
    }
    if (!options.getNodeDescriptionFilter().test(type.getDefinition())) {
      return;
    }

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLEnumType.class)
            .elementType(GraphQLEnumValueDefinition.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    printComments(out, type, "");
    out.format(
        "enum %s%s", type.getName(), directivesString(GraphQLEnumType.class, type.getDirectives()));
    var values = type.getValues().stream().sorted(comparator).toList();
    if (!values.isEmpty()) {
      out.append(" {\n");
      for (var enumValueDefinition : values) {
        printComments(out, enumValueDefinition, "  ");
        var enumValueDirectives = enumValueDefinition.getDirectives();
        if (enumValueDefinition.isDeprecated()) {
          enumValueDirectives = addDeprecatedDirectiveIfNeeded(enumValueDirectives);
        }
        out.format(
                "  %s%s",
                enumValueDefinition.getName(),
                directivesString(GraphQLEnumValueDefinition.class, enumValueDirectives))
            .append("\n");
      }
      out.append("}");
    }
    out.append("\n\n");
  }

  private Predicate<GraphQLNamedType> matchesClass(Class<?> clazz) {
    return type ->
        clazz.isAssignableFrom(type.getClass()) && options.getIncludeSchemaElement().test(type);
  }

  private Predicate<GraphQLNamedType> matchesTypeName(GraphQLObjectType namedType) {
    if (namedType == null) {
      return testType -> false;
    }
    var name = namedType.getName();
    return testType ->
        testType.getName().equals(name) && options.getIncludeSchemaElement().test(testType);
  }

  private <T> List<T> removeMatchingItems(
      List<T> list, Predicate<T> filter, Consumer<T> removedItemsFn) {
    List<T> returnValue = new ArrayList<>();
    for (var item : list) {
      if (filter.test(item)) {
        removedItemsFn.accept(item);
      } else {
        returnValue.add(item);
      }
    }
    return returnValue;
  }

  private boolean isIntrospectionType(GraphQLNamedType type) {
    return !options.isIncludeIntrospectionTypes() && type.getName().startsWith("__");
  }

  private void printFieldDefinitions(
      PrintWriter out,
      Comparator<? super GraphQLSchemaElement> comparator,
      List<GraphQLFieldDefinition> fieldDefinitions) {
    if (fieldDefinitions.isEmpty()) {
      return;
    }

    out.append("{\n");
    fieldDefinitions.stream()
        .filter(options.getIncludeSchemaElement())
        .filter(fd -> options.getNodeDescriptionFilter().test(fd.getDefinition()))
        .sorted(comparator)
        .forEach(
            fd -> {
              printComments(out, fd, "  ");
              var fieldDirectives = fd.getDirectives();
              if (fd.isDeprecated()) {
                fieldDirectives = addDeprecatedDirectiveIfNeeded(fieldDirectives);
              }

              out.format(
                      "  %s%s: %s%s",
                      fd.getName(),
                      argsString(GraphQLFieldDefinition.class, fd.getArguments()),
                      typeString(fd.getType()),
                      directivesString(GraphQLFieldDefinition.class, fieldDirectives))
                  .append("\n");
            });
    out.append("}");
  }

  private static String printAst(InputValueWithState value, GraphQLInputType type) {
    return AstPrinter.printAst(
        ValuesResolver.valueToLiteral(
            value, type, GraphQLContext.getDefault(), Locale.getDefault()));
  }

  private List<GraphQLDirective> getSchemaDirectives(GraphQLSchema schema) {
    return schema.getDirectives().stream()
        .filter(options.getIncludeDirective())
        .filter(options.getIncludeSchemaElement())
        .filter(d -> options.getNodeDescriptionFilter().test(d.getDefinition()))
        .sorted(Comparator.comparing(GraphQLDirective::getName))
        .toList();
  }

  String typeString(GraphQLType rawType) {
    return GraphQLTypeUtil.simplePrint(rawType);
  }

  String argsString(Class<? extends GraphQLSchemaElement> parent, List<GraphQLArgument> arguments) {
    var hasDescriptions = arguments.stream().anyMatch(this::hasDescription);
    var halfPrefix = hasDescriptions ? "  " : "";
    var prefix = hasDescriptions ? "    " : "";
    var count = 0;
    var sb = new StringBuilder();

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(parent)
            .elementType(GraphQLArgument.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    arguments =
        arguments.stream().sorted(comparator).filter(options.getIncludeSchemaElement()).toList();
    for (var argument : arguments) {
      if (count == 0) {
        sb.append("(");
      } else {
        sb.append(", ");
      }
      if (hasDescriptions) {
        sb.append("\n");
      }
      sb.append(printComments(argument, prefix));

      sb.append(prefix)
          .append(argument.getName())
          .append(": ")
          .append(typeString(argument.getType()));
      if (argument.hasSetDefaultValue()) {
        var defaultValue = argument.getArgumentDefaultValue();
        sb.append(" = ");
        sb.append(printAst(defaultValue, argument.getType()));
      }

      argument.getDirectives().stream()
          .filter(options.getIncludeSchemaElement())
          .map(this::directiveString)
          .filter(it -> !it.isEmpty())
          .forEach(directiveString -> sb.append(" ").append(directiveString));

      count++;
    }
    if (count > 0) {
      if (hasDescriptions) {
        sb.append("\n");
      }
      sb.append(halfPrefix).append(")");
    }
    return sb.toString();
  }

  String directivesString(
      Class<? extends GraphQLSchemaElement> parent, List<GraphQLDirective> directives) {
    directives =
        directives.stream()
            // @deprecated is special - we always print it if something is deprecated
            .filter(
                directive ->
                    options.getIncludeDirective().test(directive)
                        || isDeprecatedDirective(directive))
            .filter(options.getIncludeSchemaElement())
            .toList();

    if (directives.isEmpty()) {
      return "";
    }
    var sb = new StringBuilder();
    if (hasDirectiveOnOwnLine(parent)) {
      sb.append("\n");
    } else if (parent != GraphQLSchemaElement.class) {
      sb.append(" ");
    }

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(parent)
            .elementType(GraphQLDirective.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    directives = directives.stream().sorted(comparator).toList();
    for (var i = 0; i < directives.size(); i++) {
      var directive = directives.get(i);
      sb.append(directiveString(directive));
      if (hasDirectiveOnOwnLine(parent)) {
        sb.append("\n");
      } else if (i < directives.size() - 1) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }

  private boolean hasDirectiveOnOwnLine(Class<? extends GraphQLSchemaElement> parent) {
    return parent == GraphQLObjectType.class
        || parent == GraphQLInterfaceType.class
        || parent == GraphQLSchemaElement.class
        || parent == GraphQLInputObjectType.class;
  }

  private String directiveString(GraphQLDirective directive) {
    if (!options.getIncludeSchemaElement().test(directive)) {
      return "";
    }
    // @deprecated is special - we always print it if something is deprecated
    if (!(options.getIncludeDirective().test(directive) || isDeprecatedDirective(directive))) {
      return "";
    }

    var sb = new StringBuilder();
    sb.append("@").append(directive.getName());

    List<GraphQLArgument> args = getSortedDirectiveArgument(directive);
    if (!args.isEmpty()) {
      sb.append("(");
      for (var i = 0; i < args.size(); i++) {
        var arg = args.get(i);
        String argValue = null;
        if (arg.hasSetValue()) {
          argValue = printAst(arg.toAppliedArgument().getArgumentValue(), arg.getType());
        } else if (arg.hasSetDefaultValue()) {
          argValue = printAst(arg.getArgumentDefaultValue(), arg.getType());
        }
        if (!isNullOrEmpty(argValue)) {
          sb.append(arg.getName());
          sb.append(": ");
          sb.append(argValue);
          if (i < args.size() - 1) {
            sb.append(", ");
          }
        }
      }
      sb.append(")");
    }
    return sb.toString();
  }

  private List<GraphQLArgument> getSortedDirectiveArgument(GraphQLDirective directive) {
    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLDirective.class)
            .elementType(GraphQLArgument.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    var args = directive.getArguments();
    args =
        args.stream()
            .filter(arg -> arg.hasSetValue() && !sameAsDefaultValue(arg))
            .sorted(comparator)
            .toList();
    return args;
  }

  private boolean sameAsDefaultValue(GraphQLArgument arg) {
    if (arg.hasSetValue() && arg.hasSetDefaultValue()) {
      var argValue = arg.toAppliedArgument().getArgumentValue().getValue();
      var defaultValue = arg.getArgumentDefaultValue().getValue();
      //noinspection DataFlowIssue
      return argValue.toString().equals(defaultValue.toString());
    }
    return false;
  }

  private boolean isDeprecatedDirective(GraphQLDirective directive) {
    return directive.getName().equals(DeprecatedDirective.getName());
  }

  private boolean hasDeprecatedDirective(List<GraphQLDirective> directives) {
    return directives.stream().filter(this::isDeprecatedDirective).count() == 1;
  }

  private List<GraphQLDirective> addDeprecatedDirectiveIfNeeded(List<GraphQLDirective> directives) {
    if (!hasDeprecatedDirective(directives)) {
      directives = new ArrayList<>(directives);
      directives.add(DeprecatedDirective4Printing);
    }
    return directives;
  }

  private String directiveDefinitions(List<GraphQLDirective> directives) {
    var sb = new StringBuilder();
    directives.stream()
        .filter(options.getIncludeSchemaElement())
        .forEach(
            directive -> {
              sb.append(directiveDefinition(directive));
              sb.append("\n");
            });
    if (!directives.isEmpty()) {
      sb.append("\n");
    }
    return sb.toString();
  }

  private String directiveDefinition(GraphQLDirective directive) {
    var sb = new StringBuilder();

    var sw = new StringWriter();
    printComments(new PrintWriter(sw), directive, "");

    sb.append(sw);

    sb.append("directive @").append(directive.getName());

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLDirective.class)
            .elementType(GraphQLArgument.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    var args = directive.getArguments();
    args = args.stream().filter(options.getIncludeSchemaElement()).sorted(comparator).toList();

    sb.append(argsString(GraphQLDirective.class, args));

    if (directive.isRepeatable()) {
      sb.append(" repeatable");
    }

    sb.append(" on ");

    var locations =
        directive.validLocations().stream().map(Enum::name).collect(Collectors.joining(" | "));
    sb.append(locations);

    return sb.toString();
  }

  private String printComments(GraphQLNamedSchemaElement graphQLType, String prefix) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    printComments(pw, graphQLType, prefix);
    return sw.toString();
  }

  private void printComments(
      PrintWriter out, GraphQLNamedSchemaElement graphQLType, String prefix) {

    var documentation = getDocumentation(graphQLType);
    if (documentation.isNullOrEmpty()) {
      return;
    }

    printMultiLineHashDescription(out, prefix, documentation.getComments());

    documentation
        .getDescription()
        .map(description -> asList(description.split("\n")))
        .filter(lines -> !lines.isEmpty())
        .ifPresent(
            lines -> {
              if (options.isDescriptionsAsHashComments()) {
                printMultiLineHashDescription(out, prefix, lines);
              } else if (lines.size() > 1) {
                printMultiLineDescription(out, prefix, lines);
              } else {
                printSingleLineDescription(out, prefix, lines.get(0));
              }
            });
  }

  private void printMultiLineHashDescription(PrintWriter out, String prefix, List<String> lines) {
    lines.forEach(l -> out.printf("%s#%s", prefix, l).append("\n"));
  }

  private void printMultiLineDescription(PrintWriter out, String prefix, List<String> lines) {
    out.printf("%s\"\"\"", prefix).append("\n");
    lines.forEach(l -> out.printf("%s%s", prefix, l).append("\n"));
    out.printf("%s\"\"\"", prefix).append("\n");
  }

  private void printSingleLineDescription(PrintWriter out, String prefix, String s) {
    // See: https://github.com/graphql/graphql-spec/issues/148
    var desc = escapeJsonString(s);
    out.printf("%s\"%s\"", prefix, desc).append("\n");
  }

  private boolean hasDescription(GraphQLNamedSchemaElement descriptionHolder) {
    var description = getDocumentation(descriptionHolder);
    return !description.isNullOrEmpty();
  }

  private DescriptionAndComments getDocumentation(GraphQLNamedSchemaElement type) {
    var returnValue = new DescriptionAndComments();

    AbstractDescribedNode<?> definition = (AbstractDescribedNode<?>) type.getDefinition();

    if (definition != null) {
      returnValue.comments(definition.getComments());
    }

    Optional.ofNullable(definition)
        .map(AbstractDescribedNode::getDescription)
        .map(Description::getContent)
        .or(() -> Optional.ofNullable(type.getDescription()))
        .filter(d -> !d.isBlank())
        .ifPresent(returnValue::description);

    return returnValue;
  }

  private static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
