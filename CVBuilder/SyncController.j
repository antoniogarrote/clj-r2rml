/*
 * SyncController.j
 * CVBuilder
 *
 * Created by You on March 10, 2011.
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


@implementation SyncController : CPObject
{
  id currentNode;
  id triplesCounter;
  id nodesCounter;
  id delegate;
  CPWindow win;
  CPProgressIndicator progressBar;
  CPTextField notification;
}

-(id)initWithDelegate:(id)aDelegate
{
  var self = [super init];
  if(self) {
    delegate = aDelegate;
    currentNode = null;
    triplesCounter = 0;
    nodesCounter = 0;
  }

  return self;
}

-(void)sync
{
  var triplesCount = [Backend countTriples];

  triplesCounter = 0;
  nodesCounter = 0;

  if(triplesCount > 0) {
    [self reloadWin];
    console.log("starting sync");
    var node = [self getCurrentObj];
    [self syncNode:node];
  } else {
    console.log("nothing to sync");
    [self reloadWin];
    [progressBar removeFromSuperview];
    [self endSync];
  }

}

-(void)syncNode:(id)aNode
{
  console.log("syncing node "+ [aNode uid]);
  if([aNode isDirty]) {
    console.log(" * node is dirty");
    if([aNode isLoaded]) {
      console.log(" * node isLoaded");
      [notification setStringValue:"Updating " + [aNode uri]];
      [aNode updateToEndPointWithNetworkDelegate:self];
    } else if([aNode isNew]) {
      console.log(" * node isNew");
      [notification setStringValue:"Saving  _:" + [aNode uid]];
      [aNode saveToEndPointWithNetworkDelegate:self];
    } else if([aNode isDeleted]){
      console.log(" * node isDeleted");
      [notification setStringValue:"Deleting " + [aNode uri]];
      throw "Not implemented yet";
    } else {
      throw "Unknown state for node";
    }
  } else {
    nodesCounter++;
    var node = [self getCurrentObj];
    if(node) {
      [self syncNode:node];
    } else {
      [self endSync];
    }
  }
}

-(id)getCurrentObj
{
  var nodes = [Backend allNodes];
  var i = 0;

  for(var k in nodes) {
    if(i==nodesCounter) {
      return nodes[k];
    }
    i++;
  }

  return null;
}


-(void)endSync
{

  [notification removeFromSuperview];
  notification = [[CPTextField alloc] initWithFrame:CGRectMake(224,190,420,20)];

  [notification setStringValue:@"Finished"];
  [notification setFont:[CPFont boldFontWithName:@"Arial" size:12]];
  [[win contentView] addSubview:notification];


  editBtn = [[CPButton alloc] initWithFrame:CGRectMake(215, 240, 80, 24)];
  [editBtn setTitle:@"Continue"];
  [editBtn setTarget:self];
  [editBtn setAction:@selector(closeSyncPanel:)];
  [delegate syncFinished:self];
  [[win contentView] addSubview:editBtn];
}

-(void)closeSyncPanel:(id)sender
{
  [win close];
  [CPApp abortModal];
}

// Callbacks

-(void)graphCreated:(id)aGraph
{
  var usedTriples = [aGraph triplesCount];
  [progressBar incrementBy:usedTriples];

  nodesCounter++;
  var node = [self getCurrentObj];
  if(node) {
    currentNode = node;
    [self syncNode:node];
  } else {
    [self endSync];
  }
}

-(void)graphUpdated:(id)aGraph
{

  var usedTriples = [aGraph triplesCount];
  [progressBar incrementBy:usedTriples];

  nodesCounter++;
  var node = [self getCurrentObj];
  if(node) {
    currentNode = node;
    [self syncNode:node];
  } else {
    [self endSync];
  }
}

-(void)graphLoaded:(id)aGraph
{


}

-(void)reloadWin
{
  win = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,290) styleMask:CPTitledWindowMask];
  [win setTitle:@"Syncing RDF triples"];
  var contentView = [win contentView];
  [contentView setBackgroundColor:[CPColor colorWithHexString:@"e6e8ea"]];

  var image = [[CPImage alloc] initWithContentsOfFile:[[CPBundle mainBundle] pathForResource:@"sync3.png"] size:CPSizeMake(120, 120)];
  var imageView = [[CPImageView alloc] initWithFrame:CGRectMake(190, 20, 120, 120)];
  [imageView setHasShadow:NO];
  [imageView setImageScaling:CPScaleNone];
  var imageSize = [image size];
  [imageView setFrameSize:imageSize];
  [imageView setImage:image];

  [contentView addSubview:imageView];

  progressBar = [[CPProgressIndicator alloc] initWithFrame:CGRectMake(35,160,420,20)];
  [progressBar sizeToFit];
  [progressBar setMinValue:0];
  [progressBar setMaxValue:[Backend countTriples]];

  [[win contentView] addSubview:progressBar];

  notification = [[CPTextField alloc] initWithFrame:CGRectMake(35,190,420,20)];
  [notification setBackgroundColor:[CPColor whiteColor]];
  [notification setBordered:YES];

  [notification setStringValue:@"syncing..."];
  [notification setFont:[CPFont fontWithName:@"Arial" size:12]];

  [[win contentView] addSubview:notification];

  [win makeKeyAndOrderFront:self];
  [CPApp runModalForWindow:win];

}
@end
