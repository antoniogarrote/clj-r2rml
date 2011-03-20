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

var NEW = 0;
var LOADED = 1;
var DELETED = 2;

@implementation RDFObject : CPObject
{
  id uid;
  BOOL dirty;
  id triples;
  id networkDelegate;
  id endpoint;
  CPString networkOperation;
  id state;
  id delegate;
}

-(id) init {
  self = [super init];

  if(self) {
    uid = -1;
    state = NEW;
    dirty = YES;
    triples = {}
  }

  return self;
}

-(id)uid
{
  return uid;
}

-(void)setUid:(id)anUID
{
  uid = anUID;
}

-(BOOL)isNew
{
  return state == NEW;
}

-(BOOL)isLoaded
{
  return state == LOADED;
}

-(void)hasBeenLoaded
{
  state = LOADED;
}

-(BOOL)isDeleted
{
  return state == DELETED;
}

-(BOOL)isDirty
{
  return dirty;
}

-(void)modified
{
  dirty = YES;
}

-(void)clean
{
  dirty = NO;
}

-(void)prepareDelete
{
  state = DELETED;
  dirty = YES;
}

-(id)triplesCount
{
  var count = 0;
  for(var k in triples) {
    if(k != "#" && k != "@") {
      count++;
    }
  }
  return count;
}

-(id)triples
{
  return triples;
}

-(void)setTriples:(id)graph
{
  triples = graph;
}

-(id)loadFromURL:(CPString)aUrl withNetworkDelegate:(id)aDelegate
{
  networkDelegate = aDelegate;

  var request = [[CPURLRequest alloc] initWithURL:aUrl];
  [request setHTTPMethod:@"GET"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"LOAD";

  var urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

-(void)saveToEndPointWithNetworkDelegate:(id)aDelegate
{
  networkDelegate = aDelegate;

  var msg = [CPString JSONFromObject:triples];

  request = [[CPURLRequest alloc] initWithURL:endpoint];
  [request setHTTPMethod:@"POST"];
  [request setHTTPBody:msg];
  [request setValue:[msg length] forHTTPHeaderField:@"Content-Length"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"CREATE";
  urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

-(void)updateToEndPointWithNetworkDelegate:(AppController)aDelegate
{
  networkDelegate = aDelegate;

  var msg = [CPString JSONFromObject:triples];

  uri = triples["@"].split("#")[0];
  uri = uri+"?_method=PUT";
  uri = uri.replace("cvapi:",[Backend apiEndpoint]+"/");

  request = [[CPURLRequest alloc] initWithURL:uri];
  [request setHTTPMethod:@"PUT"];
  [request setHTTPBody:msg];
  [request setValue:[msg length] forHTTPHeaderField:@"Content-Length"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"UPDATE"
    urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

-(void)deleteToEndPointWithNetworkDelegate:(AppController)aDelegate
{
  networkDelegate = aDelegate;

  var msg = [CPString JSONFromObject:triples];

  uri = triples["@"].split("#")[0];
  uri = uri+"?_method=DELETE";
  uri = uri.replace("cvapi:",[Backend apiEndpoint]+"/");

  request = [[CPURLRequest alloc] initWithURL:uri];
  [request setHTTPMethod:@"DELETE"];
  [request setHTTPBody:msg];
  [request setValue:[msg length] forHTTPHeaderField:@"Content-Length"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"DELETE"
    urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

- (void)connection:(CPURLConnection)aConnection didReceiveData:(CPString)data
{

  if([networkOperation isEqualToString:@"LOAD"]) {
    triples = [data objectFromJSON][0];
    dirty = NO;
    state = LOADED;
  } else if([networkOperation isEqualToString:@"CREATE"]){
    triples['@'] = data;
    dirty = NO;
    state = LOADED;
  } else if([networkOperation isEqualToString:@"DELETE"]) {
    triples['@'] = null;
    dirty = YES;
    state = DELETED;
  }

  if(networkDelegate)
    {
      if([networkOperation isEqualToString:@"LOAD"]) {
        [networkDelegate graphLoaded:self];

      } else if([networkOperation isEqualToString:@"CREATE"]) {

        [networkDelegate graphCreated:self];

      } else if([networkOperation isEqualToString:@"UPDATE"]){
        [networkDelegate graphUpdated:self];
      } else if([networkOperation isEqualToString:@"DELETE"]){
        [networkDelegate graphDeleted:self];
      }
    }
}
@end
