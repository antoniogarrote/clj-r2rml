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
@import "Education.j"
@import "EducationView.j"
@import "Backend.j"

@implementation AppController : CPObject
{
  Candidate candidate;
  CPMutableArray educations;

  CandidateView candidateView;
  CPView contentView;
  CPToolbar toolbar;

  CPString EducationItemIdentifier;
  CPString JobItemIdentifier;
  CPString SyncItemIdentifier;
  CPString SemanticItemIdentifier;

  id minHeight;
  id marginLeft
  id marginTopCounter;
  id width;
  id minHeight;
  id candidateRectHeight;
}

- (void)applicationDidFinishLaunching:(CPNotification)aNotification
{
  // data

  candidate = NULL;
  educations = [[CPMutableArray alloc] init];

  // views

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
  [Backend init];
  [Backend setGlobalConfiguration:dict];


  // Tool Bar
  EducationItemIdentifier = @"education_item_identifier";
  JobItemIdentifier = @"job_item_identifier";
  SyncItemIdentifier = @"sync_item_identifier";
  SemanticItemIdentifier = @"semantic_item_identifier";


  toolbar = [[CPToolbar alloc] initWithIdentifier:@"Sections"];
  [toolbar setDelegate:self];
  [toolbar setVisible:YES];
  [theWindow setToolbar:toolbar];


  // Redraw main interface
  [self redrawCV];

  // Testing loading a candidate
  var aCandidate = [[Candidate alloc] init];
  //var candidateUri = @"http://localhost:8080/api/candidates/antonio-garrotehernndez-409588"
  var candidateUri = @"http://localhost:8080/api/candidates/antonio-garrote-457906"
  [aCandidate loadFromURL:candidateUri withNetworkDelegate:self];

  // Uncomment the following line to turn on the standard menu bar.
  //[CPMenu setMenuBarVisible:YES];
}

-(void)redrawCV
{
  // cleaning old views
  var views = [contentView subviews];
  var viewsCount = [views count];
  for(var i=0; i<viewsCount; i++) {
    var view = [views objectAtIndex:i];
    [view removeFromSuperview];
  }

  marginLeft       = 40;
  marginTopCounter = 20;
  width            = CGRectGetWidth([contentView bounds]) - 80;
  minHeight        = CGRectGetHeight([contentView bounds]) - 20;

  if(candidate) {

    // candiate's profile section

    candidateRectHeight = 200;
    educationRectHeight = 140;
    var rect = CGRectMake(marginLeft,marginTopCounter,width, candidateRectHeight);

    candidateView = [[CandidateView alloc] initWithFrame:rect];
    [candidateView setAutoresizingMask:CPViewWidthSizable];

    [candidateView setCandidate:candidate];
    [candidateView setBackgroundColor:[CPColor whiteColor]];
    [contentView addSubview:candidateView];

    marginTopCounter = marginTopCounter + candidateRectHeight;

    // candidate's studies section

    var educationsCount = [educations count];
    if(educationsCount > 0) {
      var educationSectionView = [[CPView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, 40)];
      var sectionLabel = [[CPTextField alloc] initWithFrame:CGRectMake(40,10, 300, 30)];
      [sectionLabel setStringValue:@"Academic Background"];
      [sectionLabel setFont:[CPFont boldFontWithName:@"Arial" size:18]];
      [educationSectionView addSubview:sectionLabel];
      [educationSectionView setBackgroundColor:[CPColor whiteColor]];
      [contentView addSubview:educationSectionView];
      marginTopCounter = marginTopCounter + 40;
    }
    for(var i=0; i<educationsCount; i++) {
      var education = [educations objectAtIndex:i];
      var educationView = [[EducationView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, educationRectHeight) andDelegate:self];
      [educationView setEducation:education];
      [contentView addSubview:educationView];

      marginTopCounter = marginTopCounter + educationRectHeight;
    }
  }


  [self drawPadding];
}

-(void)drawPadding {
  if(marginTopCounter < minHeight) {
    var rect = CGRectMake(marginLeft,marginTopCounter,width, (minHeight - marginTopCounter));

    paddingView = [[CPView alloc] initWithFrame:rect];
    [paddingView setAutoresizingMask:CPViewWidthSizable];

    [paddingView setBackgroundColor:[CPColor whiteColor]];
    [contentView addSubview:paddingView];
  }
}

// Handling of Candidates
-(void)candidateCreated:(Candidate)aCandidate
{
  alert(@"A candidate has been created");
}

-(void)candidateLoaded:(Candidate)aCandidate
{
  candidate = aCandidate;
  [self redrawCV];
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
      [toolbarItem setAction:@selector(addEducationSection:)];
      [toolbarItem setLabel:"Education"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];

    } else if(anItemIdentifier == JobItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"work.png"] size:CPSizeMake(30, 30)];
      [toolbarItem setImage:image];
      [toolbarItem setTarget:self];
      //[toolbarItem setAction:@selector(remove:)];
      [toolbarItem setLabel:"Work Experience"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    } else if(anItemIdentifier == SyncItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"sync.png"] size:CPSizeMake(30, 30)];
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

-(void)addEducationSection:(id)sender {
  var education  = [[Education alloc] initForCandidate:candidate];
  [educations addObject:education];
  var educationView = [[EducationView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width) andDelegate:self];
  [educationView setEducation:education];
  [self drawPadding];
  [educationView editNewEducation];
}

-(void)educationAdded:(id)sender {
  [self redrawCV];
}

@end
