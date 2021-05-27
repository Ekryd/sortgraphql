Feature: Sorting annotations in different ways
  
  Scenario: Unions are sorted by default
    Given schema content
"""
union Something = B | C | A

type Query {
  thing: String
}

type A {
  thing: String
}
type B {
  thing: String
}
type C {
  thing: String
}
"""
    Then sorted schema
"""
type Query {
  thing: String
}

union Something = A | B | C

type A {
  thing: String
}

type B {
  thing: String
}

type C {
  thing: String
}

"""
