Luposdate-Nonstandard-Endpoint
==============================

This is an extension to the standard endpoint of
[LUPOSDATE](https://github.com/luposdate/luposdate). Follow the
[instructions](#build-and-run) to build and run. The standard endpoint serves an
HTML form under `http://localhost:8080` which you can use to run a SPARQL query
against the endpoint.

This extension provides a set of new endpoints, which can be accessed via following routes: `/nonstandard/sparql`, `/nonstandard/sparql/info`, `/nonstandard/sparql/graphs`, `/nonstandard/rif`, `/nonstandard/rif/info` and `/nonstandard/rif/graphs`.

These can be used to perform offline queries, retrieve ASTs and operator graphs. In general it's a good idea to build up a request for `nonstandard/sparql` or `nonstandard/rif` and send the exact same request to `info` and `graphs` routes to retrieve more information. Not needed parameters will be ignored. All extensions are mainly intended as a backend for [Semantic Web education tools](https://github.com/hauptbenutzer/luposdate-spa-client).

Contents:

* [Build and Run](#build-and-run)
* [POST to /nonstandard/sparql](#post-to-nonstandardsparql)
* [POST to /nonstandard/sparql/info](#post-to-nonstandardsparqlinfo)
* [POST to /nonstandard/sparql/graphs](#post-to-nonstandardsparqlgraphs)
* [POST to /nonstandard/rif](#post-to-nonstandardrif)
* [POST to /nonstandard/rif/info](#post-to-nonstandardrifinfo)
* [POST to /nonstandard/rif/graphs](#post-to-nonstandardrifgraphs)

Build and Run
=============

Before you start, make sure that you have a JDK and [Maven](http://maven.apache.org/) installed.

1. Download source
2. Run `mvn package`
3. Run `java -jar target/luposdateExtendedEndpoint-0.1-SNAPSHOT-jar-with-dependencies.jar`

**It will create an index on the first run or when run with command line argument `--rebuild-index`.**

POST to /nonstandard/sparql
===========================

Returns the result of a SPARQL query running against provided RDF data.

Request body must be a JSON object. Two short examples:

Minimal configuration
---------------------

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10",
        "rdf": "@prefix dc: <http://purl.org/dc/elements/1.1/>. <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\"."
    }

    Response

    {
        "JSON": [{
                    "head": {
                        "vars": [
                            "p",
                            "s",
                            "o"
                        ]
                    },
                    "results": {
                        "bindings": [
                            {
                                "p": {
                                    "type": "uri",
                                    "value": "http://purl.org/dc/elements/1.1/title"
                                },
                                "s": {
                                    "type": "uri",
                                    "value": "http://en.wikipedia.org/wiki/Tony_Benn"
                                },
                                "o": {
                                    "type": "literal",
                                    "value": "Tony Benn"
                                }
                            },
                            {
                                "p": {
                                    "type": "uri",
                                    "value": "http://purl.org/dc/elements/1.1/publisher"
                                },
                                "s": {
                                    "type": "uri",
                                    "value": "http://en.wikipedia.org/wiki/Tony_Benn"
                                },
                                "o": {
                                    "type": "literal",
                                    "value": "Wikipedia"
                                }
                            }
                        ]
                    }
                }]
    }

Alternative output formats
--------------------------

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10",
        "rdf": "@prefix dc: <http://purl.org/dc/elements/1.1/>. <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\"."
        "formats": ["plain", "xml", "html"]
    }

    Response (raw)

    {
        "Plain": ["[{?p=<http://purl.org/dc/elements/1.1/title>, ?s=<http://en.wikipedia.org/wiki/Tony_Benn>, ?o=\\"Tony Benn\\"}, {?p=<http://purl.org/dc/elements/1.1/publisher>, ?s=<http://en.wikipedia.org/wiki/Tony_Benn>, ?o=\\"Wikipedia\\"}]"],
        "XML": ["<?xml version=\\"1.0\\"?>\\n<sparql xmlns:rdf=\\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\\" xmlns:xs=\\"http://www.w3.org/2001/XMLSchema#\\" xmlns=\\"http://www.w3.org/2005/sparql-results#\\">\\n <head>\\n  <variable name=\\"p\\"/>\\n  <variable name=\\"s\\"/>\\n  <variable name=\\"o\\"/>\\n </head>\\n <results>\\n   <result>\\n    <binding name=\\"p\\">\\n     <uri>http://purl.org/dc/elements/1.1/title</uri>\\n    </binding>\\n    <binding name=\\"s\\">\\n     <uri>http://en.wikipedia.org/wiki/Tony_Benn</uri>\\n    </binding>\\n    <binding name=\\"o\\">\\n     <literal>Tony Benn</literal>\\n    </binding>\\n   </result>\\n   <result>\\n    <binding name=\\"p\\">\\n     <uri>http://purl.org/dc/elements/1.1/publisher</uri>\\n    </binding>\\n    <binding name=\\"s\\">\\n     <uri>http://en.wikipedia.org/wiki/Tony_Benn</uri>\\n    </binding>\\n    <binding name=\\"o\\">\\n     <literal>Wikipedia</literal>\\n    </binding>\\n   </result>\\n </results>\\n</sparql>"],
        "Comma Separated Values (CSV)": ["p,s,o\\n<http://purl.org/dc/elements/1.1/title>,<http://en.wikipedia.org/wiki/Tony_Benn>,\\"Tony Benn\\"\\n<http://purl.org/dc/elements/1.1/publisher>,<http://en.wikipedia.org/wiki/Tony_Benn>,\\"Wikipedia\\"\\n"]
    }

    Response (pretty without escaping - this is not valid JSON)

    {
        "Plain": ["
            [{?p=<http://purl.org/dc/elements/1.1/title>, ?s=<http://en.wikipedia.org/wiki/Tony_Benn>, ?o="Tony Benn"}, {?p=<http://purl.org/dc/elements/1.1/publisher>, ?s=<http://en.wikipedia.org/wiki/Tony_Benn>, ?o="Wikipedia"}]
        "],
        "XML": ["
            <?xml version="1.0"?>
            <sparql xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:xs="http://www.w3.org/2001/XMLSchema#" xmlns="http://www.w3.org/2005/sparql-results#">
             <head>
              <variable name="p"/>
              <variable name="s"/>
              <variable name="o"/>
             </head>
             <results>
               <result>
                <binding name="p">
                 <uri>http://purl.org/dc/elements/1.1/title</uri>
                </binding>
                <binding name="s">
                 <uri>http://en.wikipedia.org/wiki/Tony_Benn</uri>
                </binding>
                <binding name="o">
                 <literal>Tony Benn</literal>
                </binding>
               </result>
               <result>
                <binding name="p">
                 <uri>http://purl.org/dc/elements/1.1/publisher</uri>
                </binding>
                <binding name="s">
                 <uri>http://en.wikipedia.org/wiki/Tony_Benn</uri>
                </binding>
                <binding name="o">
                 <literal>Wikipedia</literal>
                </binding>
               </result>
             </results>
            </sparql>
        "],
        ["Comma Separated Values (CSV)": "
            <http://purl.org/dc/elements/1.1/title>,<http://en.wikipedia.org/wiki/Tony_Benn>,"Tony Benn"
            <http://purl.org/dc/elements/1.1/publisher>,<http://en.wikipedia.org/wiki/Tony_Benn>,"Wikipedia"
        "]
    }

SPARQL graph query
------------------

```
Request

    {
        "query": "CONSTRUCT WHERE { ?s ?p ?o. }",
        "rdf": "@prefix dc: <http://purl.org/dc/elements/1.1/>.\n <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\".",
        "formats": ["json", "plain"]
    }
```

```
Response

{
  "JSON": [
    {
      "rdf": "<http://en.wikipedia.org/wiki/Tony_Benn> <http://purl.org/dc/elements/1.1/title> \"Tony Benn\" .\n<http://en.wikipedia.org/wiki/Tony_Benn> <http://purl.org/dc/elements/1.1/publisher> \"Wikipedia\" .\n"
    }
  ],
  "triples": [
    {
      "subject": {
        "value": "http://en.wikipedia.org/wiki/Tony_Benn",
        "type": "uri"
      },
      "predicate": {
        "value": "http://purl.org/dc/elements/1.1/title",
        "type": "uri"
      },
      "object": {
        "value": "Tony Benn",
        "type": "literal"
      }
    },
    {
      "subject": {
        "value": "http://en.wikipedia.org/wiki/Tony_Benn",
        "type": "uri"
      },
      "predicate": {
        "value": "http://purl.org/dc/elements/1.1/publisher",
        "type": "uri"
      },
      "object": {
        "value": "Wikipedia",
        "type": "literal"
      }
    }
  ],
  "Plain": [
    "[(<http://en.wikipedia.org/wiki/Tony_Benn>,<http://purl.org/dc/elements/1.1/title>,\"Tony Benn\"), (<http://en.wikipedia.org/wiki/Tony_Benn>,<http://purl.org/dc/elements/1.1/publisher>,\"Wikipedia\")]"
  ]
}
```

JSON Keys
---------

**query (mandatory)**

Query must be a valid SPARQL query

**rdf (mandatory)**

RDF Data to perform the query on. RDF Data must be provided in N3 format.

**formats (optional)**

JSON Array of Strings.
Valid formats are:
`Comma Separated Values (CSV)`, `XML with Query Triples`, `Query-Triples`, `JSON`, `JSON with Query-Triples`, `Tab Separated Values (TSV)`, `Colored HTML`, `Colored HTML with Query-Triples`, `HTML`, `XML`, `Plain`, `HTML with Query-Triples`
Aliases:
`text/csv`, `application/sparql-results+xml+querytriples`, `text/n3`, `application/sparql-results+json`, `application/sparql-results+json+querytriples`, `text/tsv`, `colored html`, `colored html with query-triples`, `html`, `application/sparql-results+xml`, `text/plain`, `html with query-triples`

**Please note that `Query-Triples` (`text/n3`) formatter is broken at the moment (02/2015)!**

**evaluator (optional)**

Must be one of `MemoryIndex`, `RDF3X`, `Stream`, `Jena` or `Sesame`.
Defaults to `MemoryIndex`

**inference (optional)**

Must be one of `NONE`, `RIF`, `RDFS` or `OWL2RL`.
Defaults to `NONE`.

**inferenceGeneration (optional)**

Only relevant if `inference` is `RDFS` or `OWL2RL`.
Must be one of `GENERATED`, `GENERATEDOPT` or `FIXED`.
Defaults to `FIXED`.

**owl2rlInconsistencyCheck (optional)**

Only relevant if `inference` is `OWL2RL`.
Must be a boolean.
Defaults to `false`.

**rif (optional)**

RIF rule set to be used for RIF inference.
Only relevant if `inference` is `RIF`.
Defaults to `( empty string )`.

Errors
------

The server usually responds with an error object describing what went wrong. This is not a complete list - just some examples.

**Missing RDF (400 Bad Request)**

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10"
    }

    Response

    {
        "error": "Key \"rdf\" must be present in body."
    }

**Malformed JSON (400 Bad Request)**

    Request

    {
        foobar
    }

    Response

    {
        "error": "Expected a ':' after a key at 13 [character 1 line 4]"
    }

For semantic errors the server issues a 200 OK response.

**Bad query (200 OK)**

    Request

    {
        "query": "foobar",
        "rdf": "@prefix dc: <http://purl.org/dc/elements/1.1/>. <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\"."
    }

    Response

    {
        "queryError": {
            "line": 1,
            "column": 7,
            "errorMessage": "Lexical error at line 1, column 7.  Encountered: <EOF> after : \"foobar\""
        }
    }

**Another bad query (200 OK)**

    {
        "query": "SELECT ** WHERE { ?s ?p ?o. } LIMIT 10",
        "rdf": " @prefix dc: <http://purl.org/dc/elements/1.1/>. <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\"."
    }

    Response

    {
        "queryError": {
            "line": 1,
            "column": 9,
            "errorMessage": "Encountered \" \"*\" \"* \"\" at line 1, column 9.\nWas expecting one of:\n    \"{\" ...\n    \"WHERE\" ...\n    \"FROM\" ...\n    "
        }
    }

**Bad RDF (200 OK)**

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10",
        "rdf": "foobar"
    }

    Response

    {
        "rdfError": {
            "line": 1,
            "column": 7,
            "errorMessage": "Lexical error at line 1, column 7.  Encountered: <EOF> after : \"foobar\""
        }
    }


POST to /nonstandard/sparql/info
================================

Returns additional compilation information about a SPARQL query. At its best it will be the AST for the query, the query in core SPARQL and the AST of the core SPARQL query.

Request body must be a JSON object.

Minimal configuration
---------------------

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10"
    }

    Response (nested output format)

    {
      "coreSPARQL": "SELECT *\n\nWHERE \n{\n?s ?p ?o .\n} LIMIT 10\n",
      "AST": {
        "id": 1283301884,
        "description": "Query",
        "classification": "QueryHead",
        "children": [
          {
            "id": 846561706,
            "operandPosition": 0,
            "description": "SelectQuery disctinct :false reduced:false select all:true",
            "classification": "QueryHead",
            "children": [
              {
                "id": 1083431367,
                "operandPosition": 0,
                "description": "GroupConstraint",
                "classification": "NonTerminalNode",
                "children": [
                  {
                    "id": 717142256,
                    "operandPosition": 0,
                    "description": "TripleSet",
                    "classification": "UnknownNode",
                    "children": [
                      {
                        "id": 1331669628,
                        "operandPosition": 0,
                        "description": "Var s",
                        "classification": "TerminalNode",
                        "type": "ASTVar"
                      },
                      {
                        "id": 296868762,
                        "operandPosition": 1,
                        "description": "Var p",
                        "classification": "TerminalNode",
                        "type": "ASTVar"
                      },
                      {
                        "id": 868770953,
                        "operandPosition": 2,
                        "description": "ObjectList",
                        "classification": "NonTerminalNode",
                        "children": [
                          {
                            "id": 692091118,
                            "operandPosition": 0,
                            "description": "Var o",
                            "classification": "TerminalNode",
                            "type": "ASTVar"
                          }
                        ],
                        "type": "ASTObjectList"
                      }
                    ],
                    "type": "ASTTripleSet"
                  }
                ],
                "type": "ASTGroupConstraint"
              },
              {
                "id": 1203988074,
                "operandPosition": 1,
                "description": "Limit 10",
                "classification": "HighLevelOperator",
                "type": "ASTLimit"
              }
            ],
            "type": "ASTSelectQuery"
          }
        ],
        "type": "ASTQuery"
      },
      "coreAST": {
        "id": 1836637684,
        "description": "Query",
        "classification": "QueryHead",
        "children": [
          {
            "id": 439613010,
            "operandPosition": 0,
            "description": "SelectQuery disctinct :false reduced:false select all:true",
            "classification": "QueryHead",
            "children": [
              {
                "id": 1232113390,
                "operandPosition": 0,
                "description": "GroupConstraint",
                "classification": "NonTerminalNode",
                "children": [
                  {
                    "id": 2092421356,
                    "operandPosition": 0,
                    "description": "TripleSet",
                    "classification": "UnknownNode",
                    "children": [
                      {
                        "id": 133527020,
                        "operandPosition": 0,
                        "description": "Var s",
                        "classification": "TerminalNode",
                        "type": "ASTVar"
                      },
                      {
                        "id": 68214025,
                        "operandPosition": 1,
                        "description": "Var p",
                        "classification": "TerminalNode",
                        "type": "ASTVar"
                      },
                      {
                        "id": 1864334324,
                        "operandPosition": 2,
                        "description": "ObjectList",
                        "classification": "NonTerminalNode",
                        "children": [
                          {
                            "id": 2080573738,
                            "operandPosition": 0,
                            "description": "Var o",
                            "classification": "TerminalNode",
                            "type": "ASTVar"
                          }
                        ],
                        "type": "ASTObjectList"
                      }
                    ],
                    "type": "ASTTripleSet"
                  }
                ],
                "type": "ASTGroupConstraint"
              },
              {
                "id": 726590465,
                "operandPosition": 1,
                "description": "Limit 10",
                "classification": "HighLevelOperator",
                "type": "ASTLimit"
              }
            ],
            "type": "ASTSelectQuery"
          }
        ],
        "type": "ASTQuery"
      }
    }

Alternative output formats
--------------------------

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10",
        "astFormat": "graph"
    }

```
Response

{
  "coreSPARQL": "SELECT *\n\nWHERE \n{\n?s ?p ?o .\n} LIMIT 10\n",
  "AST": {
    "edges": {
      "1920728698": [
        {
          "operandPosition": 0,
          "nodeId": "1198346414"
        }
      ],
      "540591806": [
        {
          "operandPosition": 0,
          "nodeId": "1870656632"
        }
      ],
      "1198346414": [
        {
          "operandPosition": 0,
          "nodeId": "1506538532"
        },
        {
          "operandPosition": 1,
          "nodeId": "1560909194"
        },
        {
          "operandPosition": 2,
          "nodeId": "540591806"
        }
      ],
      "1998854828": [
        {
          "operandPosition": 0,
          "nodeId": "1666404175"
        }
      ],
      "1666404175": [
        {
          "operandPosition": 0,
          "nodeId": "1920728698"
        },
        {
          "operandPosition": 1,
          "nodeId": "713045582"
        }
      ]
    },
    "nodes": [
      {
        "id": 1998854828,
        "description": "Query",
        "classification": "QueryHead",
        "type": "ASTQuery",
        "depth": 0
      },
      {
        "id": 1666404175,
        "description": "SelectQuery disctinct :false reduced:false select all:true",
        "classification": "QueryHead",
        "type": "ASTSelectQuery",
        "depth": 1
      },
      {
        "id": 1920728698,
        "description": "GroupConstraint",
        "classification": "NonTerminalNode",
        "type": "ASTGroupConstraint",
        "depth": 2
      },
      {
        "id": 713045582,
        "description": "Limit 10",
        "classification": "HighLevelOperator",
        "type": "ASTLimit",
        "depth": 2
      },
      {
        "id": 1198346414,
        "description": "TripleSet",
        "classification": "UnknownNode",
        "type": "ASTTripleSet",
        "depth": 3
      },
      {
        "id": 1506538532,
        "description": "Var s",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 4
      },
      {
        "id": 1560909194,
        "description": "Var p",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 4
      },
      {
        "id": 540591806,
        "description": "ObjectList",
        "classification": "NonTerminalNode",
        "type": "ASTObjectList",
        "depth": 4
      },
      {
        "id": 1870656632,
        "description": "Var o",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 5
      }
    ]
  },
  "coreAST": {
    "edges": {
      "2098935205": [
        {
          "operandPosition": 0,
          "nodeId": "90121166"
        },
        {
          "operandPosition": 1,
          "nodeId": "690465827"
        },
        {
          "operandPosition": 2,
          "nodeId": "1805009648"
        }
      ],
      "297717907": [
        {
          "operandPosition": 0,
          "nodeId": "2098935205"
        }
      ],
      "1805009648": [
        {
          "operandPosition": 0,
          "nodeId": "1443156414"
        }
      ],
      "1607659723": [
        {
          "operandPosition": 0,
          "nodeId": "297717907"
        },
        {
          "operandPosition": 1,
          "nodeId": "107965439"
        }
      ],
      "965421944": [
        {
          "operandPosition": 0,
          "nodeId": "1607659723"
        }
      ]
    },
    "nodes": [
      {
        "id": 965421944,
        "description": "Query",
        "classification": "QueryHead",
        "type": "ASTQuery",
        "depth": 0
      },
      {
        "id": 1607659723,
        "description": "SelectQuery disctinct :false reduced:false select all:true",
        "classification": "QueryHead",
        "type": "ASTSelectQuery",
        "depth": 1
      },
      {
        "id": 297717907,
        "description": "GroupConstraint",
        "classification": "NonTerminalNode",
        "type": "ASTGroupConstraint",
        "depth": 2
      },
      {
        "id": 107965439,
        "description": "Limit 10",
        "classification": "HighLevelOperator",
        "type": "ASTLimit",
        "depth": 2
      },
      {
        "id": 2098935205,
        "description": "TripleSet",
        "classification": "UnknownNode",
        "type": "ASTTripleSet",
        "depth": 3
      },
      {
        "id": 90121166,
        "description": "Var s",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 4
      },
      {
        "id": 690465827,
        "description": "Var p",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 4
      },
      {
        "id": 1805009648,
        "description": "ObjectList",
        "classification": "NonTerminalNode",
        "type": "ASTObjectList",
        "depth": 4
      },
      {
        "id": 1443156414,
        "description": "Var o",
        "classification": "TerminalNode",
        "type": "ASTVar",
        "depth": 5
      }
    ]
  }
}
```

JSON Keys
---------

**query (mandatory)**

Query must be a valid SPARQL query

**evaluator (optional)**

Must be one of `MemoryIndex`, `RDF3X`, `Stream`, `Jena` or `Sesame`.
Defaults to `MemoryIndex`

**Please note that not all evaluators provided the same level of information (if at all)!**

Errors
------

The server usually responds with an error object describing what went wrong. This is not a complete list - just some examples.

**Requested evaluator does not provide information (200 OK)**

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. }",
        "evaluator": "Jena"
    }

    Response

    {
        "info": "Compiler does not provide additional information."
    }

POST to /nonstandard/sparql/graphs
==================================

Returns operator graphs for each optimization phase used when processing the query.

Request body must be a JSON object. The request format is the same as for [POST to /nonstandard/sparql](#post-to-nonstandardsparql).

Minimal configuration
---------------------

    Request

    {
        "query": "SELECT * WHERE { ?s ?p ?o. } LIMIT 10",
        "rdf": "@prefix dc: <http://purl.org/dc/elements/1.1/>.\n <http://en.wikipedia.org/wiki/Tony_Benn> dc:title \"Tony Benn\"; dc:publisher \"Wikipedia\"."
    }

```
Response (always graph output format)

{
  "prefix": {
    "names": [],
    "pre-defined": {
      "<http://www.w3.org/2000/01/rdf-schema#>": "rdfs",
      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>": "rdf",
      "<http://www.w3.org/2005/xpath-functions#>": "fn",
      "<http://www.w3.org/2001/XMLSchema#>": "xsd"
    },
    "prefixes": {}
  },
  "optimization": {
    "steps": [
      {
        "ruleName": "correctoperatorgraphPackageDescription",
        "description": "Before a possible correction of the operator graph...",
        "operatorGraph": {
          "edges": {
            "0": [
              {
                "operandPosition": 0,
                "nodeId": "44"
              }
            ],
            "44": [
              {
                "operandPosition": 0,
                "nodeId": "154"
              }
            ],
            "154": [
              {
                "operandPosition": 0,
                "nodeId": "186"
              }
            ]
          },
          "nodes": [
            {
              "id": 0,
              "description": "MemoryIndexRoot",
              "type": "BasicOperatorByteArray",
              "depth": 0
            },
            {
              "id": 44,
              "description": "Memory Index Scan on\nTriplePattern (?s, ?p, ?o)",
              "type": "BasicOperatorByteArray",
              "depth": 1
            },
            {
              "id": 154,
              "description": "Limit 10",
              "type": "BasicOperatorByteArray",
              "depth": 2
            },
            {
              "id": 186,
              "description": "Result",
              "type": "BasicOperatorByteArray",
              "depth": 3
            }
          ]
        }
      },
      {
        "ruleName": "logicaloptimizationPackageDescription",
        "description": "Before logical optimization...",
        "operatorGraph": {
          "edges": {
            "0": [
              {
                "operandPosition": 0,
                "nodeId": "44"
              }
            ],
            "44": [
              {
                "operandPosition": 0,
                "nodeId": "154"
              }
            ],
            "154": [
              {
                "operandPosition": 0,
                "nodeId": "186"
              }
            ]
          },
          "nodes": [
            {
              "id": 0,
              "description": "MemoryIndexRoot",
              "type": "BasicOperatorByteArray",
              "depth": 0
            },
            {
              "id": 44,
              "description": "Memory Index Scan on\nTriplePattern (?s, ?p, ?o)",
              "type": "BasicOperatorByteArray",
              "depth": 1
            },
            {
              "id": 154,
              "description": "Limit 10",
              "type": "BasicOperatorByteArray",
              "depth": 2
            },
            {
              "id": 186,
              "description": "Result",
              "type": "BasicOperatorByteArray",
              "depth": 3
            }
          ]
        }
      },
      {
        "ruleName": "optimizingjoinord;erRule",
        "description": "After optimizing the join order...",
        "operatorGraph": {
          "edges": {
            "0": [
              {
                "operandPosition": 0,
                "nodeId": "44"
              }
            ],
            "44": [
              {
                "operandPosition": 0,
                "nodeId": "154"
              }
            ],
            "154": [
              {
                "operandPosition": 0,
                "nodeId": "186"
              }
            ]
          },
          "nodes": [
            {
              "id": 0,
              "description": "MemoryIndexRoot",
              "type": "BasicOperatorByteArray",
              "depth": 0
            },
            {
              "id": 44,
              "description": "Memory Index Scan on\nTriplePattern (?s, ?p, ?o)",
              "type": "BasicOperatorByteArray",
              "depth": 1
            },
            {
              "id": 154,
              "description": "Limit 10",
              "type": "BasicOperatorByteArray",
              "depth": 2
            },
            {
              "id": 186,
              "description": "Result",
              "type": "BasicOperatorByteArray",
              "depth": 3
            }
          ]
        }
      },
      {
        "ruleName": "physicaloptimizationRule",
        "description": "After physical optimization...",
        "operatorGraph": {
          "edges": {
            "0": [
              {
                "operandPosition": 0,
                "nodeId": "44"
              }
            ],
            "44": [
              {
                "operandPosition": 0,
                "nodeId": "154"
              }
            ],
            "154": [
              {
                "operandPosition": 0,
                "nodeId": "186"
              }
            ]
          },
          "nodes": [
            {
              "id": 0,
              "description": "MemoryIndexRoot",
              "type": "BasicOperatorByteArray",
              "depth": 0
            },
            {
              "id": 44,
              "description": "Memory Index Scan on\nTriplePattern (?s, ?p, ?o)",
              "type": "BasicOperatorByteArray",
              "depth": 1
            },
            {
              "id": 154,
              "description": "Limit 10",
              "type": "BasicOperatorByteArray",
              "depth": 2
            },
            {
              "id": 186,
              "description": "Result",
              "type": "BasicOperatorByteArray",
              "depth": 3
            }
          ]
        }
      }
    ]
  }
}
```

POST to /nonstandard/rif
========================

Request format is the same as for POSTs to [POST to /nonstandard/sparql](#post-to-nonstandardsparql), except that `query` must be RIF. Also optional `evaluator` key is ignored.

**Please note that some formatters like `JSON` aren't supporting RIF results yet (02/2015)!**

Minimal configuration
---------------------

    Request

    {
        "query": "Document(\n  Prefix(cpt <http://example.com/concepts#>)\n  Prefix(ppl <http://example.com/people#>)\n  Prefix(bks <http://example.com/books#>)\n\n  Group\n  (\n    Forall ?Buyer ?Item ?Seller (\n cpt:buy(?Buyer ?Item ?Seller) :- cpt:sell(?Seller ?Item ?Buyer)\n    )\n \n    cpt:sell(ppl:John bks:LeRif ppl:Mary)\n  )\n)",
        "rdf": "",
        "formats": ["plain"]
    }

```
Response

{
  "predicates": [
    {
      "parameters": [
        {
          "value": "http://example.com/people#Mary",
          "type": "uri"
        },
        {
          "value": "http://example.com/books#LeRif",
          "type": "uri"
        },
        {
          "value": "http://example.com/people#John",
          "type": "uri"
        }
      ],
      "predicateName": {
        "value": "http://example.com/concepts#buy",
        "type": "uri"
      }
    }
  ],
  "Plain": [
    "[<http://example.com/concepts#buy>(<http://example.com/people#Mary>, <http://example.com/books#LeRif>, <http://example.com/people#John>)]"
  ]
}
```

POST to /nonstandard/rif/info
=============================

Returns additional compilation information about a RIF document. It will return the AST and the rule set.

Request format is the same as for POSTs to [POST to /nonstandard/sparql/info](#post-to-nonstandardsparqlinfo), except that `query` must be RIF. Also optional `evaluator` key is ignored.

Minimal configuration
---------------------

    Request

    {
        "query": "Document(\n  Prefix(cpt <http://example.com/concepts#>)\n  Prefix(ppl <http://example.com/people#>)\n  Prefix(bks <http://example.com/books#>)\n\n  Group\n  (\n    Forall ?Buyer ?Item ?Seller (\n cpt:buy(?Buyer ?Item ?Seller) :- cpt:sell(?Seller ?Item ?Buyer)\n    )\n \n    cpt:sell(ppl:John bks:LeRif ppl:Mary)\n  )\n)"
    }

    Response (nested output format and stripped down - these are big even for small queries)

    {
        "AST": {
            [...] (format same as rulesAST)
        }
        "rulesAST": {
            "children": [
                {
                    "children": [
                        {
                            "id": 1227773968,
                            "operandPosition": 0,
                            "description": "?Item",
                            "type": "RuleVariable"
                        },
                        {
                            "id": 26716153,
                            "operandPosition": 1,
                            "description": "?Buyer",
                            "type": "RuleVariable"
                        },
                        {
                            "id": 194301248,
                            "operandPosition": 2,
                            "description": "?Seller",
                            "type": "RuleVariable"
                        },
                        {
                            "id": 1445931696,
                            "operandPosition": 3,
                            "description": "<http://example.com/concepts#buy>(?Buyer, ?Item, ?Seller))",
                            "type": "RulePredicate"
                        },
                        {
                            "id": 849065220,
                            "operandPosition": 4,
                            "description": "<http://example.com/concepts#sell>(?Seller, ?Item, ?Buyer))",
                            "type": "RulePredicate"
                        }
                    ],
                    "id": 1259760240,
                    "operandPosition": 0,
                    "description": "<http://example.com/concepts#buy>(?Buyer, ?Item, ?Seller))",
                    "type": "Rule"
                }
            ],
            "description": "Document\nPrefix: rdf - http://www.w3.org/1999/02/22-rdf-syntax-ns#\nPrefix: cpt - http://example.com/concepts#\nPrefix: rdfs - http://www.w3.org/2000/01/rdf-schema#\nPrefix: bks - http://example.com/books#\nPrefix: xs - http://www.w3.org/2001/XMLSchema#\nPrefix: ppl - http://example.com/people#\nPrefix: rif - http://www.w3.org/2007/rif#\n",
            "type": "Document"
        }
    }

POST to /nonstandard/rif/graphs
===============================

Returns operator graphs for each optimization phase used when processing the query.

Request body must be a JSON object. The request format is the same as for [POST to /nonstandard/rif](#post-to-nonstandardrif).
