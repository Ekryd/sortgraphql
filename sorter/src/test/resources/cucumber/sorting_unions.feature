Feature: Sorting unions in different ways
  
  Scenario: Unions are sorted by default
    When schema content
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
    
  Scenario: Do not sort union types
    Given skip union type sorting is true
    When schema content
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

union Something = B | C | A

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
