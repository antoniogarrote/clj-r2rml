/*
 *  Candidate.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <Foundation/Foundation.j>
@import "RDFObject.j"

@implementation Candidate : RDFObject
{
}

- (id)init
{
  self = [super init];

  triples = {'#':{cvapi: [Backend defaultNs],
                  vcard: "http://www.w3.org/2006/vcard/ns#"}};

  endpoint = [[Backend apiEndpoint] stringByAppendingString:@"/candidates"];

  return self;
}

-(CPString)kind
{
  return "Candidate";
}

-(void)setFamilyName:(CPString)data
{
  triples["vcard:family-name"] = data;
  [self modified];
}

-(void)setGivenName:(CPString)data
{
  triples["vcard:given-name"] = data;
  [self modified];
}

-(void)setAddress:(CPString)data
{
  triples["vcard:adr"] = data;
  [self modified];
}

-(void)setBirthDay:(CPString)data
{
  triples["vcard:bday"] = data;
  [self modified];
}

-(void)setTelephone:(CPString)data
{
  triples["vcard:tel"] = data;
  [self modified];
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

-(CPString)uri
{
  return triples["@"];
}
@end
