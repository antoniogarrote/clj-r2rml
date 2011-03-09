/*
 *  Education.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <Foundation/Foundation.j>
@import "RDFObject.j"


@implementation Education : RDFObject
{
}

- (id)initForCandidate:aCandidate
{
  self = [super init];

  triples = {'#':{cvapi: "http://test.com/api/",
                  cv: "http://rdfs.org/resume-rdf/"}};

  endpoint = [[stringByAppendingString:uri] @"/educations"]                                         
  return self;
}

-(void)setStartDate:(CPString)startDate
{
  triples["cv:startDate"] = startDate;
}

-(void)setEndDate:(CPString)endDate
{
  triples["cv:endDate"] = endDate;
}

-(void)setStudiedInOrganizationName:(Organization)anOrganization
{
  triples["cv:studiedIn"] = [anOrganization homepage];
}

-(void)setDegreeType:(CPString)degree
{
  triples["cv:degreeType"] = degree;
}

-(void)setStudiedBy:(Candidate)aCandidate
{
  triples["cvapi:studiedBy"] = [aCandidate uri];
}

-(void)uri
{
  return triples["@"];
}

-(void)sartDate:
{
  return triples["cv:startDate"];
}

-(void)setEndDate:
{
  return triples["cv:endDate"];
}

-(void)setStudiedInOrganizationName
{
  return triples["cv:studiedIn"];
}

-(void)setDegreeType:
{
  return triples["cv:degreeType"];
}

-(void)setStudiedBy:
{
  return triples["cvapi:studiedBy"];
}

@end
