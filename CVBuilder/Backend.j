/*
 *  Backend.j
 *  TestXib
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
*/

@import <Foundation/Foundation.j>

var configuration;
var rdfGraph;

@implementation Backend : CPObject
{
}

+(void)init
{
  configuration = {};
  rdfGraph = {};
}

+(void)setGlobalConfiguration:(CPDictionary)aConfiguration
{
	configuration = aConfiguration;
}

+(CPString)apiEndpoint
{
	return [configuration valueForKey:@"apiEndpoint"];
}

+(void)registerNode:(id)aNode
{
  if(aNode["@"]) {
    rdfGraph[aNode["@"]] = aNode;
  } else {
    throw("Impossible to register a node without URI");
  }
}

+(id)searchNode:(CPString)uri
{
  return rdfGraph[uri];
}
@end
