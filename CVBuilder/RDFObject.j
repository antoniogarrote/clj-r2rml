/*
 *  RDFObject.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */
@import <Foundation/Foundation.j>
@import "Backend.j"
@import "AppController.j"


@implementation RDFObject : CPObject
{
  BOOL dirty;
  id triples;
  AppController networkDelegate;
  id endpoint;
  CPString networkOperation;
}

-(id) init {
  self = [super init];

  if(self) {
      dirty = true;
      triples = {}
  }

  return self;
}

-(id)loadFromURL:(CPString)aUrl withNetworkDelegate:(AppController)aDelegate
{
  networkDelegate = aDelegate;

  var request = [[CPURLRequest alloc] initWithURL:aUrl];
  [request setHTTPMethod:@"GET"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"LOAD";

  var urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

-(void)saveToEndPointWithNetworkDelegate:(AppController)aDelegate
{
  networkDelegate = aDelegate;

  var msg = [CPString JSONFromObject:triples];

  request = [[CPURLRequest alloc] initWithURL:endpoint];
  [request setHTTPMethod:@"POST"];
  [request setHTTPBody:msg];
  [request setValue:[msg length] forHTTPHeaderField:@"Content-Length"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"CREATE"
    urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

- (void)connection:(CPURLConnection)aConnection didReceiveData:(CPString)data
{

  if([networkOperation isEqualToString:@"LOAD"]) {
    triples = [data objectFromJSON][0];
  } else if([networkOperation isEqualToString:@"CREATE"]){
    triples['@'] = data;
  }

  dirty = NO;

  if(networkDelegate)
    {
      if([networkOperation isEqualToString:@"LOAD"]) {
        [networkDelegate candidateLoaded:self];

      } else if([networkOperation isEqualToString:@"CREATE"]) {

        [networkDelegate candidateCreated:self];

      }
    }
}
