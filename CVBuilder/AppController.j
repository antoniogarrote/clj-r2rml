/*
 * AppController.j
 * CVBuilder
 *
 * Created by You on March 6, 2011.
 * Copyright 2011, Your Company All rights reserved.
 */

@import <Foundation/CPObject.j>
@import <AppKit/CPAccordionView.j>
@import "Candidate.j"
@import "CandidateView.j"
@import "Backend.j"

@implementation AppController : CPObject
{
  Candidate candidate;
  CandidateView candidateView;
  CPView contentView;
  CPToolbar toolbar;

  CPString EducationItemIdentifier;
  CPString JobItemIdentifier;
  CPString SyncItemIdentifier;
  CPString SemanticItemIdentifier;
}

- (void)applicationDidFinishLaunching:(CPNotification)aNotification
{

  var theWindow = [[CPWindow alloc] initWithContentRect:CGRectMakeZero() styleMask:CPBorderlessBridgeWindowMask];
  contentView = [theWindow contentView];

  [theWindow setAcceptsMouseMovedEvents:YES];


  [contentView setBackgroundColor:[CPColor grayColor]];
  [contentView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];

  [theWindow orderFront:self];


  // Configuration
  var keys = [CPArray arrayWithObjects:@"apiEndpoint"];
  var objects = [CPArray arrayWithObjects:@"http://localhost:8080/api"];
  var dict = [CPDictionary dictionaryWithObjects:objects forKeys:keys];
  [Backend setGlobalConfiguration:dict];


  // Tool Bar
  EducationItemIdentifier = @"education_item_identifier";
  JobItemIdentifier = @"job_item_identifier";
  SyncItemIdentifier = @"sync_item_identifier";
  SemanticItemIdentifier = @"semantic_item_identifier";


  toolbar = [[CPToolbar alloc] initWithIdentifier:"Sections"];
  [toolbar setDelegate:self];
  [toolbar setVisible:YES];
  [theWindow setToolbar:toolbar];


  // Testing loading a candidate
  candidate = [[Candidate alloc] init];
  [candidate loadFromURL:@"http://localhost:8080/api/candidates/antonio-garrote-457906" withNetworkDelegate:self];


  // Uncomment the following line to turn on the standard menu bar.
  //[CPMenu setMenuBarVisible:YES];
}

// Handling of Candidates
-(void)candidateCreated:(Candidate)aCandidate
{
  alert(@"A candidate has been created");
}

-(void)candidateLoaded:(Candidate)aCandidate
{

  var rect = CGRectMake(40,20,CGRectGetWidth([contentView bounds]) - 80, 200);

  candidateView = [[CandidateView alloc] initWithFrame:rect];
  [candidateView setAutoresizingMask:CPViewWidthSizable];

  [candidateView setCandidate:aCandidate];
  [candidateView setBackgroundColor:[CPColor whiteColor]];
  [contentView addSubview:candidateView];
}

// ToolBar

// Return an array of toolbar item identifier (all the toolbar items that may be present in the toolbar)
- (CPArray)toolbarAllowedItemIdentifiers:(CPToolbar)aToolbar {
  return [EducationItemIdentifier, JobItemIdentifier, CPToolbarFlexibleSpaceItemIdentifier, SyncItemIdentifier, SemanticItemIdentifier];
}

// Return an array of toolbar item identifier (the default toolbar items that are present in the toolbar)
- (CPArray)toolbarDefaultItemIdentifiers:(CPToolbar)aToolbar {
  return [EducationItemIdentifier, JobItemIdentifier, CPToolbarFlexibleSpaceItemIdentifier, SyncItemIdentifier, SemanticItemIdentifier];
}

- (CPToolbarItem)toolbar:(CPToolbar)aToolbar itemForItemIdentifier:(CPString)anItemIdentifier willBeInsertedIntoToolbar:(BOOL)aFlag {

  var toolbarItem = [[CPToolbarItem alloc] initWithItemIdentifier:anItemIdentifier];

    if (anItemIdentifier == EducationItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"education.png"] size:CPSizeMake(30, 30)];
      [toolbarItem setImage:image];
      [toolbarItem setTarget:self];
      //[toolbarItem setAction:@selector(remove:)];
      [toolbarItem setLabel:"Education"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];

    } else if(anItemIdentifier == JobItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"education.png"] size:CPSizeMake(30, 30)];
      [toolbarItem setImage:image];
      [toolbarItem setTarget:self];
      //[toolbarItem setAction:@selector(remove:)];
      [toolbarItem setLabel:"Work Experience"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    } else if(anItemIdentifier == SyncItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"education.png"] size:CPSizeMake(30, 30)];
      [toolbarItem setImage:image];
      [toolbarItem setTarget:self];
      //[toolbarItem setAction:@selector(remove:)];
      [toolbarItem setLabel:"Sync"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    }  else if(anItemIdentifier == SemanticItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"education.png"] size:CPSizeMake(30, 30)];
//    var highlighted = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"removeHighlighted.png"]
//                                       size:CPSizeMake(30, 25)];
      [toolbarItem setImage:image];
//    [toolbarItem setAlternateImage:highlighted];
      [toolbarItem setTarget:self];
      //[toolbarItem setAction:@selector(remove:)];
      [toolbarItem setLabel:"Triples"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    }

    return toolbarItem;
}
@end
