Feature: Sorting GraphQL Schemas
  
  Scenario: basic query schema files
    When unsorted schema file "basic_products.graphqls" 
    Then sorted schema file "basic_products_expected.graphqls"

  Scenario: base federation specification
    When unsorted schema file "federation.graphqls"
    Then sorted schema file "federation_expected.graphqls"

  Scenario: sorting multiple files
    When unsorted schema files 
    | schema.graphqls | 
    | mutations.graphqls |
    Then sorted schema "mutations.graphqls" file "mutations_expected.graphqls"
