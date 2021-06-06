Feature: Sorting GraphQL Schema files

  Scenario: basic query schema files
    When unsorted schema file "basic_products.graphqls"
    Then sorted schema file "basic_products_expected.graphqls"

  Scenario: base federation specification
    When unsorted schema file "federation.graphqls"
    Then sorted schema file "federation_expected.graphqls"

  Scenario: sorting multiple files
    When unsorted schema files
      | wolfMain.graphqls |
      | wolfAdd.graphqls  |
    Then sorted schema "wolfMain.graphqls" file "wolfMain_expected.graphqls"

  Scenario: sorting multiple files (reverse input order)
    When unsorted schema files
      | wolfAdd.graphqls  |
      | wolfMain.graphqls |
    Then sorted schema "wolfMain.graphqls" file "wolfMain_expected.graphqls"

  Scenario: sorting multiple files (verify addition)
    When unsorted schema files
      | wolfMain.graphqls |
      | wolfAdd.graphqls  |
    Then sorted schema "wolfAdd.graphqls" file "wolfAdd_expected.graphqls"

  Scenario: schema without query type
    When unsorted schema file "federated_service_no_query.graphqls"
    Then sorted schema file "federated_service_no_query_expected.graphqls"
    
