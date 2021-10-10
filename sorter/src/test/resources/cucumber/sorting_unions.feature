Feature: Sorting unions in different ways
  
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
    When sorting
    Then schema content will be
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
    When sorting
    Then schema content will be
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
