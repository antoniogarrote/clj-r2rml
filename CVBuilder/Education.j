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

-(id)initForCandidate:aCandidate {
  self = [super init];

  if(self) {
    triples = {'#':{cvapi: "http://test.com/api/",
                    cv: "http://rdfs.org/resume-rdf/"}};

    triples["cvapi:studiedBy"] = [aCandidate uri];
    endpoint = [[aCandidate uri] stringByAppendingString: @"/educations"];
  }
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
  triples["cv:studiedIn"] = anOrganization;
}

-(void)setDegreeType:(CPString)degree
{
  triples["cv:degreeType"] = degree;
}

-(void)setStudiedBy:(Candidate)aCandidate
{
  triples["cvapi:studiedBy"] = [aCandidate uri];
}

-(id)uri
{
  return triples["@"];
}

-(id)startDate
{
  return triples["cv:startDate"];
}

-(id)endDate
{
  return triples["cv:endDate"];
}

-(id)studiedInOrganizationName
{
  return triples["cv:studiedIn"];
}

-(id)degreeType
{
  return triples["cv:degreeType"];
}

-(id)studiedBy
{
  return [Backend searchNode:triples["cvapi:studiedBy"]];
}

@end
