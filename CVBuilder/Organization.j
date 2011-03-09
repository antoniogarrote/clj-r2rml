/*
 *  Organization.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <Foundation/Foundation.j>
@import "RDFObject.j"

@implementation Organization : RDFObject
{
}

- (id)initForCandidate:aCandidate
{
  self = [super init];

  triples = {'#':{vc: "http://www.w3.org/2006/vcard/ns#",
                  foaf: "http://xmlns.com/foaf/0.1/"}};

  endpoint = [[Backend apiEndpoint] stringByAppendingString:@"/organizations"];

  return self;
}


-(void)setOrganizationName:(CPString)name
{
  triples["vc:organization-name"] = name;
}

-(void)setHomePage:(CPString)uri
{
  triples["foaf:homepage"] = uri;
}

-(void)uri
{
  return triples["@"];
}

-(void)organizationName:
{
  return triples["vc:organization-name"];
}

-(void)homePage:
{
  return triples["foaf:homepage"];
}
@end
