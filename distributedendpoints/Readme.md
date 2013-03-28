# LUPOSDATE Semantic Web Database Management System

## Module distributedendpoints

This module contains the code for the distributed data storage and querying based on SPARQL endpoints.
All registered endpoints are asked for the evaluation of the triple patterns within a SPARQL query.
It is assumed that the data is not distributed in an intelligent way and that any registered endpoint
can have data for any triple pattern.
Also non-luposdate SPARQL endpoints are supported.
It uses the super and helper classes of the distributed module for a first and simple example of a distributed scenario. 