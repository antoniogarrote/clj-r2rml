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
    triples = {'#':{cv: "http://rdfs.org/resume-rdf/",
                    cvapi: [Backend defaultNs]}};

    triples["cv:heldBy"] = [aCandidate uri];
    var uriTmp = [aCandidate uri];
    uriTmp = uriTmp.split("#")[0];
    uriTmp = uriTmp.replace("cvapi:",[Backend apiEndpoint]+"/");
    endpoint = [uriTmp stringByAppendingString: @"/jobs"];
  }
  return self;
}

-(CPString)kind
{
  return "Job";
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

-(void)setEmployedIn:(Organization)anOrganization
{
  triples["cv:employedIn"] = anOrganization;
  [self modified];
}

-(void)setJobTitle:(CPString)jobTitle
{
  triples["cv:jobTitle"] = jobTitle;
  [self modified];
}

-(void)setJobDescription:(CPString)jobDescription
{
  triples["cv:jobDescription"] = jobDescription;
  [self modified];
}

-(void)setHeldBy:(Candidate)aCandidate
{
  triples["cv:heldBy"] = [aCandidate uri];
  [self modified];
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
