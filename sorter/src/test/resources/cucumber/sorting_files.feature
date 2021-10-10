Feature: Sorting GraphQL Schema files

  Scenario: basic query schema files
    Given schema file "basic_products.graphqls"
    When sorting
    Then schema file "basic_products.graphqls" will be "basic_products_expected.graphqls"

  Scenario: base federation specification
    Given schema file "federation.graphqls"
    When sorting
    Then schema file "federation.graphqls" will be "federation_expected.graphqls"

  Scenario: sorting multiple files
    Given schema files
      | wolfMain.graphqls |
      | wolfAdd.graphqls  |
    When sorting
    Then schema file "wolfMain.graphqls" will be "wolfMain_expected.graphqls"
    Then schema file "wolfAdd.graphqls" will be "wolfAdd_expected.graphqls"

  Scenario: sorting multiple files (reverse input order)
    Given schema files
      | wolfAdd.graphqls  |
      | wolfMain.graphqls |
    When sorting
    Then schema file "wolfMain.graphqls" will be "wolfMain_expected.graphqls"
    Then schema file "wolfAdd.graphqls" will be "wolfAdd_expected.graphqls"

  Scenario: base federation specification
    Given schema file "federated_service_no_query.graphqls"
    When sorting
    Then schema file "federated_service_no_query.graphqls" will be "federated_service_no_query_expected.graphqls"

  Scenario: base federation specification
    Given schema file "force_schema_output.graphqls"
    When sorting
    Then schema file "force_schema_output.graphqls" will be "force_schema_output_expected.graphqls"
    
