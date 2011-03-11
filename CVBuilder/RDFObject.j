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
var LODADED = 1;
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
  return state == LODADED;
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

  networkOperation = @"CREATE"
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

- (void)connection:(CPURLConnection)aConnection didReceiveData:(CPString)data
{

  if([networkOperation isEqualToString:@"LOAD"]) {
    triples = [data objectFromJSON][0];
  } else if([networkOperation isEqualToString:@"CREATE"]){
    triples['@'] = data;
  }

  dirty = NO;
  state = LODADED;

  if(networkDelegate)
    {
      if([networkOperation isEqualToString:@"LOAD"]) {
        [networkDelegate graphLoaded:self];

      } else if([networkOperation isEqualToString:@"CREATE"]) {

        [networkDelegate graphCreated:self];

      } else if([networkOperation isEqualToString:@"UPDATE"]){
        [networkDelegate graphUpdated:self];
      }
    }
}

