/*
 *  Job.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/10/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <Foundation/Foundation.j>
@import "RDFObject.j"

 @implementation Job : RDFObject
 {
 }

-(id)initForCandidate:aCandidate {
  self = [super init];

  if(self) {
    triples = {'#':{cv: "http://rdfs.org/resume-rdf/"}};

    triples["cv:heldBy"] = [aCandidate uri];
    endpoint = [[aCandidate uri] stringByAppendingString: @"/jobs"];
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

-(void)setEmployedIn:(Organization)anOrganization
{
  triples["cv:employedIn"] = anOrganization;
}

-(void)setJobTitle:(CPString)jobTitle
{
  triples["cv:jobTitle"] = jobTitle;
}

-(void)setJobDescription:(CPString)jobDescription
{
  triples["cv:jobDescription"] = jobDescription;
}

-(void)setHeldBy:(Candidate)aCandidate
{
  triples["cv:heldBy"] = [aCandidate uri];
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

-(id)employedIn
{
  return triples["cv:employedIn"];
}

-(id)jobTitle
{
  return triples["cv:jobTitle"];
}

-(id)jobDescription
{
  return triples["cv:jobDescription"];
}

-(id)heldBy
{
  return [Backend searchNode:triples["cv:heldBy"]];
}

@end
