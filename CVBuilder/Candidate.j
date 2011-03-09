/*
 *  Candidate.j
 *  TestXib
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <Foundation/Foundation.j>
@import "Backend.j"
@import "AppController.j"

@implementation Candidate : CPObject
{
  BOOL dirty;
  id triples;
  AppController networkDelegate;
  id endpoint;
  CPString networkOperation;

}

- (id)init
{
  self = [super init];

  dirty = true;
  triples = {'#':{cvapi: "http://test.com/api/",
                  vcard: "http://www.w3.org/2006/vcard/ns#"}};

  endpoint = [[Backend apiEndpoint] stringByAppendingString:@"/candidates"];

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

-(void)setFamilyName:(CPString)data
{
  triples["vcard:family-name"] = data;
}

-(void)setGivenName:(CPString)data
{
  triples["vcard:given-name"] = data;
}

-(void)setAddress:(CPString)data
{
  triples["vcard:adr"] = data;
}

-(void)setBirthDay:(CPString)data
{
  triples["vcard:bday"] = data;
}

-(void)setTelephone:(CPString)data
{
  triples["vcard:tel"] = data;
}

-(CPString)familyName
{
  return triples["vcard:family-name"];
}

-(CPString)givenName
{
  return triples["vcard:given-name"];
}

-(CPString)fullName
{
  var name = [[[self givenName] stringByAppendingString:" "] stringByAppendingString:[self familyName]];
  return name;
}
-(CPString)address
{
  return triples["vcard:adr"];
}

-(CPString)birthDay
{
  return triples["vcard:bday"];
}

-(CPString)telephone
{
  return triples["vcard:tel"];
}
@end
