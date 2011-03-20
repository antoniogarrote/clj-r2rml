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
    triples = {'#':{cvapi: [Backend defaultNs],
                    cv: "http://rdfs.org/resume-rdf/"}};

    triples["cvapi:studiedBy"] = [aCandidate uri];
    var uriTmp = [aCandidate uri];
    uriTmp = uriTmp.split("#")[0];
    uriTmp = uriTmp.replace("cvapi:",[Backend apiEndpoint]+"/");
    endpoint = [uriTmp stringByAppendingString: @"/educations"];
  }
  return self;
}

-(CPString)kind
{
  return "Education";
}

-(void)setStartDate:(CPString)startDate
{
  triples["cv:startDate"] = startDate;
  [self modified];
}

-(void)setEndDate:(CPString)endDate
{
  triples["cv:endDate"] = endDate;
  [self modified];
}

-(void)setStudiedInOrganizationName:(Organization)anOrganization
{
  triples["cv:studiedIn"] = anOrganization;
  [self modified];
}

-(void)setDegreeType:(CPString)degree
{
  triples["cv:degreeType"] = degree;
  [self modified];
}

-(void)setStudiedBy:(Candidate)aCandidate
{
  triples["cvapi:studiedBy"] = [aCandidate uri];
  [self modified];
}

-(void)setEducationDescription:(CPString)jobDescription
{
  triples["cv:courseDescription"] = jobDescription;
  [self modified];
}

-(id)uri
{
  return triples["@"];
}

-(id)educationDescription
{
  return triples["cv:courseDescription"];
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
