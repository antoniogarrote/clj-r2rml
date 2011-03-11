/*
 * TriplesController.j
 * CVBuilder
 *
 * Created by You on March 11, 2011.
 * Copyright 2011, Your Company All rights reserved.
 */

@import <Foundation/CPObject.j>
@import <AppKit/CPAccordionView.j>
@import "Candidate.j"
@import "CandidateView.j"
@import "Education.j"
@import "EducationView.j"
@import "Job.j"
@import "JobView.j"
@import "Backend.j"


@implementation TriplesController : CPObject
{
  id delegate;
  CPTableView table;
  CPWindow win;
}

-(id)initWithDelegate:(id)aDelegate
{
  var self = [super init];
  if(self) {
    delegate = aDelegate;
  }1

  return self;
}

-(void)reloadWin
{
  win = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,290) styleMask:CPTitledWindowMask];
  [win setTitle:@"RDF viewer"];
  [win setShowsResizeIndicator:YES];
  var contentView = [win contentView];
  [contentView setBackgroundColor:[CPColor colorWithHexString:@"e6e8ea"]];

 // create a CPScrollView that will contain the CPTableView
  var scrollView = [[CPScrollView alloc] initWithFrame:[contentView frame]];
  [scrollView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];

  table = [[CPTableView alloc] initWithFrame:[contentView bounds]];
  [table setDataSource:self];
  [table setUsesAlternatingRowBackgroundColors:YES];

  var column = [[CPTableColumn alloc] initWithIdentifier:@"Subject"];
  [[column headerView] setStringValue:"Subject"];
  [column setWidth:125.0];
  [table addTableColumn:column];

  var column = [[CPTableColumn alloc] initWithIdentifier:@"Predicate"];
  [[column headerView] setStringValue:"Predicate"];
  [column setWidth:125.0];
  [table addTableColumn:column];

  var column = [[CPTableColumn alloc] initWithIdentifier:@"Object"];
  [[column headerView] setStringValue:"Object"];
  [column setWidth:125.0];
  [table addTableColumn:column];

  var column = [[CPTableColumn alloc] initWithIdentifier:@"Graph"];
  [[column headerView] setStringValue:"Graph"];
  [column setWidth:125.0];
  [table addTableColumn:column];


  [scrollView setDocumentView:table];
  [contentView addSubview:scrollView];

  [win makeKeyAndOrderFront:self];
  [CPApp runModalForWindow:win];
}

// Delegate methods

-(int)numberOfRowsInTableView:(CPTableView)aTableView
{
  var toReturn = [Backend countAllTriples];
  console.log("returning "+toReturn+ " nodes");
  return toReturn;
}

-(id)tableView:(CPTableView)aTableView objectValueForTableColumn:(CPTableColumn)aColumn row:(int)aRowIndex {
  var count = -1;
  var nodes = [Backend allNodes];

  for(var k in nodes) {
    console.log("Counting triples for node "+k);
    var node = nodes[k];
    var triples = [nodes[k] triples];
    for(var j in triples) {
      if(j != "#" && j != "@") {
        count++;
        if(count==aRowIndex) {
          console.log(nodes[k]);
          triples = [nodes[k] triples];
          if([aColumn identifier] === @"Subject") {
            return triples["@"];
          } else if([aColumn identifier] === @"Predicate") {
            return j
          } else if([aColumn identifier] === @"Object") {
            return triples[j];
          } else if([aColumn identifier] === @"Graph") {
            return @"graph"
          }
        }
      }
    }
  }
  return @"error";
}
@end
