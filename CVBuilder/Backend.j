/*
 *  Backend.j
 *  TestXib
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
*/

@import <Foundation/Foundation.j>

configuration = {};
rdfGraph = {};
idCounter = 0;

@implementation Backend : CPObject
{
}

+(void)init
{
  configuration = {};
  rdfGraph = {};
  blankCounter = 0;
}

+(void)setGlobalConfiguration:(CPDictionary)aConfiguration
{
	configuration = aConfiguration;
}

+(CPString)defaultNs
{
  return "https://antoniogarrote.com/cvbuilder/api/";
}

+(CPString)apiEndpoint
{
	return [configuration valueForKey:@"apiEndpoint"];
}

+(void)registerNode:(id)aNode
{
  if([aNode uid]==-1) {
    var uid = ""+idCounter;
    idCounter++;

    [aNode setUid:uid];

    rdfGraph[uid] = aNode;
  }
}

+(id)searchNode:(CPString)uri
{
  for(var k in rdfGraph) {
    var node = rdfGraph[k];

    if(node["@"] == uri) {
      return rdfGraph[k];
    }
  }

  throw "URI "+ uri + " not found in backend";
}

+(void)deleteNode:(id)aNode
{
  var toDelete = null;
  for(var k in rdfGraph) {
    if(k === [aNode uid]) {
      var triples = [aNode triples];
      if(triples["@"]) {
        [aNode prepareDelete];
        return;
      } else {
        delete rdfGraph[k];
        return;
      }
    }
  }
}

+(id)allNodes
{
  return rdfGraph;
}

+(id)countTriples
{
  var count = 0;
  var nodes = [Backend allNodes];

  console.log("register:");
  console.log([Backend allNodes]);

  for(var k in nodes) {
    console.log("Counting triples for node "+k);
    var node = nodes[k];
    if([node isDirty]) {
      count = count + [node triplesCount];
    }
  }

  return count;
}

+(id)countAllTriples
{
  var count = 0;
  var nodes = [Backend allNodes];

  console.log("register:");
  console.log([Backend allNodes]);

  for(var k in nodes) {
    console.log("Counting triples for node "+k);
    var node = nodes[k];
    count = count + [node triplesCount];
  }

  return count;
}

@end
