package sortgraphql.sort;

import graphql.Assert;
import graphql.PublicApi;
import graphql.language.*;
import graphql.schema.*;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static graphql.Directives.DeprecatedDirective;
import static graphql.introspection.Introspection.DirectiveLocation.*;
import static graphql.util.EscapeUtil.escapeJsonString;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

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
    var schemaDirectives =
        schema.getSchemaDirectives().stream()
            .sorted(Comparator.comparing(GraphQLDirective::getName))
            .collect(toList());

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

    if (needsSchemaPrinted) {
      out.format(
              "schema%s{",
              schemaDirectives.isEmpty()
                  ? " "
                  : "\n" + directivesString(GraphQLSchemaElement.class, schemaDirectives))
          .append("\n");
      if (queryType != null) {
        out.format("  query: %s", queryType.getName()).append("\n");
      }
      if (mutationType != null) {
        out.format("  mutation: %s", mutationType.getName()).append("\n");
      }
      if (subscriptionType != null) {
        out.format("  subscription: %s", subscriptionType.getName()).append("\n");
      }
      out.append("}\n\n");
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
    var types = type.getTypes().stream().sorted(comparator).collect(toList());
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
        type.getName(), directivesString(GraphQLInputObjectType.class, type.getDirectives()));
    var inputObjectFields = visibility.getFieldDefinitions(type);
    if (!inputObjectFields.isEmpty()) {
      out.append(" {\n");
      inputObjectFields.stream()
          .filter(options.getIncludeSchemaElement())
          .sorted(comparator)
          .forEach(
              fd -> {
                printComments(out, fd, "  ");
                out.format("  %s: %s", fd.getName(), typeString(fd.getType()));
                var defaultValue = fd.getDefaultValue();
                if (defaultValue != null) {
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
    var values = type.getValues().stream().sorted(comparator).collect(toList());
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
    return testType -> testType.getName().equals(name) && options.getIncludeSchemaElement().test(testType);
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

  private static String printAst(Object value, GraphQLInputType type) {
    var node = value instanceof Value ? (Value<?>) value : AstValueHelper.astFromValue(value, type);
    return AstPrinter.printAst(node);
  }

  private List<GraphQLDirective> getSchemaDirectives(GraphQLSchema schema) {
    return schema.getDirectives().stream()
        .filter(options.getIncludeDirective())
        .filter(options.getIncludeSchemaElement())
        .filter(d -> options.getNodeDescriptionFilter().test(d.getDefinition()))
        .sorted(Comparator.comparing(GraphQLDirective::getName))
        .collect(toList());
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
        arguments.stream()
            .sorted(comparator)
            .filter(options.getIncludeSchemaElement())
            .collect(toList());
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
      var defaultValue = argument.getDefaultValue();
      if (defaultValue != null) {
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
            .collect(toList());

    if (directives.isEmpty()) {
      return "";
    }
    var sb = new StringBuilder();
    if (parent == GraphQLObjectType.class || parent == GraphQLInterfaceType.class) {
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

    directives = directives.stream().sorted(comparator).collect(toList());
    for (var i = 0; i < directives.size(); i++) {
      var directive = directives.get(i);
      sb.append(directiveString(directive));
      if (parent == GraphQLObjectType.class || parent == GraphQLSchemaElement.class) {
        sb.append("\n");
      } else if (i < directives.size() - 1) {
        sb.append(" ");
      }
    }
    return sb.toString();
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

    var environment =
        GraphqlTypeComparatorEnvironment.newEnvironment()
            .parentType(GraphQLDirective.class)
            .elementType(GraphQLArgument.class)
            .build();
    var comparator = options.getComparatorRegistry().getComparator(environment);

    var args = directive.getArguments();
    args =
        args.stream()
            .filter(arg -> arg.getValue() != null && !arg.getValue().equals(arg.getDefaultValue()))
            .sorted(comparator)
            .collect(toList());
    if (!args.isEmpty()) {
      sb.append("(");
      for (var i = 0; i < args.size(); i++) {
        var arg = args.get(i);
        String argValue = null;
        if (arg.getValue() != null) {
          argValue = printAst(arg.getValue(), arg.getType());
        } else if (arg.getDefaultValue() != null) {
          argValue = printAst(arg.getDefaultValue(), arg.getType());
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
    args =
        args.stream()
            .filter(options.getIncludeSchemaElement())
            .sorted(comparator)
            .collect(toList());

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

  private String printComments(Object graphQLType, String prefix) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    printComments(pw, graphQLType, prefix);
    return sw.toString();
  }

  private void printComments(PrintWriter out, Object graphQLType, String prefix) {

    var descriptionText = getDescription(graphQLType);
    if (isNullOrEmpty(descriptionText)) {
      return;
    }

    if (!isNullOrEmpty(descriptionText)) {
      var lines = Arrays.asList(descriptionText.split("\n"));
      if (options.isDescriptionsAsHashComments()) {
        printMultiLineHashDescription(out, prefix, lines);
      } else if (!lines.isEmpty()) {
        if (lines.size() > 1) {
          printMultiLineDescription(out, prefix, lines);
        } else {
          printSingleLineDescription(out, prefix, lines.get(0));
        }
      }
    }
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

  private boolean hasDescription(Object descriptionHolder) {
    var description = getDescription(descriptionHolder);
    return !isNullOrEmpty(description);
  }

  private String getDescription(Object descriptionHolder) {
    if (descriptionHolder instanceof GraphQLObjectType) {
      var type = (GraphQLObjectType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(ObjectTypeDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLEnumType) {
      var type = (GraphQLEnumType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(EnumTypeDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLFieldDefinition) {
      var type = (GraphQLFieldDefinition) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(FieldDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLEnumValueDefinition) {
      var type = (GraphQLEnumValueDefinition) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(EnumValueDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLUnionType) {
      var type = (GraphQLUnionType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(UnionTypeDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLInputObjectType) {
      var type = (GraphQLInputObjectType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition())
              .map(InputObjectTypeDefinition::getDescription)
              .orElse(null));
    } else if (descriptionHolder instanceof GraphQLInputObjectField) {
      var type = (GraphQLInputObjectField) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(InputValueDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLInterfaceType) {
      var type = (GraphQLInterfaceType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition())
              .map(InterfaceTypeDefinition::getDescription)
              .orElse(null));
    } else if (descriptionHolder instanceof GraphQLScalarType) {
      var type = (GraphQLScalarType) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(ScalarTypeDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLArgument) {
      var type = (GraphQLArgument) descriptionHolder;
      return description(
          type.getDescription(),
          ofNullable(type.getDefinition()).map(InputValueDefinition::getDescription).orElse(null));
    } else if (descriptionHolder instanceof GraphQLDirective) {
      var type = (GraphQLDirective) descriptionHolder;
      return description(type.getDescription(), null);
    } else {
      return Assert.assertShouldNeverHappen();
    }
  }

  String description(String runtimeDescription, Description descriptionAst) {
    //
    // 95% of the time if the schema was built from SchemaGenerator then the runtime description is
    // the only description
    // So the other code here is a really defensive way to get the description
    //
    var descriptionText = runtimeDescription;
    if (isNullOrEmpty(descriptionText) && descriptionAst != null) {
      descriptionText = descriptionAst.getContent();
    }
    return descriptionText;
  }

  private static boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }
}
