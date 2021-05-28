Feature: Sorting GraphQL Schemas

  Scenario: basic query schema
    When schema content
"""
schema
@graph(name: "accounts", url: "https://accounts.api.com")
@graph(name: "inventory", url: "https://inventory.api.com")
@graph(name: "product", url: "https://product.api.com")
@graph(name: "reviews", url: "https://reviews.api.com")
@graph(name: "advertisement", url: "https://advertisement.api.com")
@composedGraph(version: 1)
{
    query: Query
}

directive @composedGraph(version: Int!) on SCHEMA
directive @graph(name: String!, url: String!) repeatable on SCHEMA
directive @owner(graph: String!) on OBJECT
directive @key(fields: String!, graph: String!) repeatable on OBJECT
directive @resolve(graph: String!) on FIELD_DEFINITION
directive @provides(fields: String!) on FIELD_DEFINITION
directive @requires(fields: String!) on FIELD_DEFINITION
directive @stream on FIELD
directive @transform(from: String!) on FIELD


type Query {
    topProducts(first: Int = 5): [Product] @resolve(graph: "product")
    me: User @resolve(graph: "accounts")
    users(filter: UserFilter): [User] @resolve(graph: "accounts")
    myVehicle: Vehicle @resolve(graph: "product")
    topAds(first: Int = 5): [Advertisement] @resolve(graph: "advertisement")
}

input UserFilter
{
    nrResults: Int = 5
    nameMatches: UserNameMatcher
}

input UserNameMatcher
{
    startsWith: String
    contains: String
    length: Int
}

type Advertisement
@owner(graph: "advertisement")
@key(fields: "{id}", graph: "advertisement")
{
    id: ID!
    title: String
    body: String
    reviews: ReviewPage
}


type Product
@owner(graph: "product")
@key(fields: "{upc}", graph: "product")
@key(fields: "{upc}", graph: "inventory")
@key(fields: "{upc}", graph: "reviews")
{
    upc: String!
    name: String!
    price: Int
    weight: Int
    similarProducts: [Product]
    reviews: [Review] @resolve(graph: "reviews")
    relatedReviews: [Review] @resolve(graph: "reviews") @requires(fields: "{ similarProducts { upc } }")
    inStock: Boolean @resolve(graph: "inventory")
    shippingEstimate: Int @resolve(graph: "inventory") @requires(fields: "{price weight}")
}

type Review
@owner(graph: "reviews")
@key(fields: "{id}", graph: "reviews")
@key(fields: "{id}", graph: "advertisement")
{
    id: ID!
    body: String
    author: User @provides(fields: "{username}")
    product: Product
}

type ReviewPage
@owner(graph: "reviews")
@key(fields: "{items}", graph: "reviews")
@key(fields: "{items}", graph: "advertisement")
{
    totalReviews: Int
    items: [Review]
    cursor: String
}

type User
@owner(graph: "accounts")
@key(fields: "{id}", graph: "accounts")
@key(fields: "{id}", graph: "reviews")
@key(fields: "{id}", graph: "product")
{
    id: ID!
    name: String
    username (uppercase: Boolean = false): String
    reviews: [Review] @resolve(graph: "reviews")
    vehicle: Vehicle @resolve(graph: "product")
    unionThing: Thing @resolve(graph: "product")
}

type Car implements Vehicle
@owner(graph: "product")
@key(fields: "{id}", graph: "product")
{
    id: ID!
    price: String
    retailPrice: String
}

type Van implements Vehicle
@owner(graph: "product")
@key(fields: "{id}", graph: "product")
{
    id: ID!
    price: String
    retailPrice: String
}

interface Vehicle
{
    id: ID!
    retailPrice: String
}

union Thing = Car | Product
"""
    Then sorted schema 
"""
directive @composedGraph(version: Int!) on SCHEMA
directive @graph(name: String!, url: String!) repeatable on SCHEMA
directive @key(fields: String!, graph: String!) repeatable on OBJECT
directive @owner(graph: String!) on OBJECT
directive @provides(fields: String!) on FIELD_DEFINITION
directive @requires(fields: String!) on FIELD_DEFINITION
directive @resolve(graph: String!) on FIELD_DEFINITION
directive @stream on FIELD
directive @transform(from: String!) on FIELD

type Query {
  me: User @resolve(graph: "accounts")
  myVehicle: Vehicle @resolve(graph: "product")
  topAds(first: Int = 5): [Advertisement] @resolve(graph: "advertisement")
  topProducts(first: Int = 5): [Product] @resolve(graph: "product")
  users(filter: UserFilter): [User] @resolve(graph: "accounts")
}

interface Vehicle {
  id: ID!
  retailPrice: String
}

union Thing = Car | Product

input UserFilter {
  nameMatches: UserNameMatcher
  nrResults: Int = 5
}

input UserNameMatcher {
  contains: String
  length: Int
  startsWith: String
}

type Advertisement
@key(fields: "{id}", graph: "advertisement")
@owner(graph: "advertisement")
{
  body: String
  id: ID!
  reviews: ReviewPage
  title: String
}

type Car implements Vehicle
@key(fields: "{id}", graph: "product")
@owner(graph: "product")
{
  id: ID!
  price: String
  retailPrice: String
}

type Product
@key(fields: "{upc}", graph: "product")
@key(fields: "{upc}", graph: "inventory")
@key(fields: "{upc}", graph: "reviews")
@owner(graph: "product")
{
  inStock: Boolean @resolve(graph: "inventory")
  name: String!
  price: Int
  relatedReviews: [Review] @requires(fields: "{ similarProducts { upc } }") @resolve(graph: "reviews")
  reviews: [Review] @resolve(graph: "reviews")
  shippingEstimate: Int @requires(fields: "{price weight}") @resolve(graph: "inventory")
  similarProducts: [Product]
  upc: String!
  weight: Int
}

type Review
@key(fields: "{id}", graph: "reviews")
@key(fields: "{id}", graph: "advertisement")
@owner(graph: "reviews")
{
  author: User @provides(fields: "{username}")
  body: String
  id: ID!
  product: Product
}

type ReviewPage
@key(fields: "{items}", graph: "reviews")
@key(fields: "{items}", graph: "advertisement")
@owner(graph: "reviews")
{
  cursor: String
  items: [Review]
  totalReviews: Int
}

type User
@key(fields: "{id}", graph: "accounts")
@key(fields: "{id}", graph: "reviews")
@key(fields: "{id}", graph: "product")
@owner(graph: "accounts")
{
  id: ID!
  name: String
  reviews: [Review] @resolve(graph: "reviews")
  unionThing: Thing @resolve(graph: "product")
  username(uppercase: Boolean = false): String
  vehicle: Vehicle @resolve(graph: "product")
}

type Van implements Vehicle
@key(fields: "{id}", graph: "product")
@owner(graph: "product")
{
  id: ID!
  price: String
  retailPrice: String
}

"""
