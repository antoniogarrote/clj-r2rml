/*
 * AppController.j
 * CVBuilder
 *
 * Created by You on March 6, 2011.
 * Copyright 2011, Your Company All rights reserved.
 */

@import <Foundation/CPObject.j>
@import <AppKit/CPAccordionView.j>
@import "SyncController.j"
@import "TriplesController.j"
@import "Candidate.j"
@import "CandidateView.j"
@import "Education.j"
@import "EducationView.j"
@import "Job.j"
@import "JobView.j"
@import "Backend.j"

@implementation AppController : CPObject
{
  Candidate candidate;
  CPMutableArray educations;
  CPMutableArray jobs;

  CandidateView candidateView;
  CPView contentView;
  id theWindow;
  CPScrollView scrollView;
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

  candidate  = NULL;
  educations = [[CPMutableArray alloc] init];
  jobs       = [[CPMutableArray alloc] init];

  // views

  theWindow = [[CPWindow alloc] initWithContentRect:CGRectMakeZero() styleMask:CPBorderlessBridgeWindowMask];
  var canvas = [theWindow contentView];
  scrollView = [[CPScrollView alloc] initWithFrame:[[theWindow contentView] frame]];
  [scrollView setAutohidesScrollers:YES];
  [scrollView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
  [[theWindow contentView] addSubview:scrollView];
  [theWindow setAcceptsMouseMovedEvents:YES];

  contentView = [[CPView alloc] initWithFrame:[[theWindow contentView] frame]];
  [scrollView setDocumentView:contentView];
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
  [Backend registerNode:aCandidate];
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
    jobRectHeight       = 170;

    var rect = CGRectMake(marginLeft,marginTopCounter,width, candidateRectHeight);

    candidateView = [[CandidateView alloc] initWithFrame:rect];
    [candidateView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
    [candidateView setAutoresizingMask:CPViewWidthSizable];

    [candidateView setCandidate:candidate];
    [candidateView setBackgroundColor:[CPColor whiteColor]];
    [contentView addSubview:candidateView];

    marginTopCounter = marginTopCounter + candidateRectHeight;

    // candidate's work experience

    var jobsCount = [jobs count];
    if(jobsCount > 0) {

      var jobSectionView = [[CPView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, 40)];
      var sectionLabel = [[CPTextField alloc] initWithFrame:CGRectMake(40,10, 300, 30)];
      [sectionLabel setStringValue:@"Work Experience"];
      [sectionLabel setFont:[CPFont boldFontWithName:@"Arial" size:18]];
      [jobSectionView addSubview:sectionLabel];
      [jobSectionView setBackgroundColor:[CPColor whiteColor]];
      [jobSectionView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
      [contentView addSubview:jobSectionView];
      marginTopCounter = marginTopCounter + 40;
    }
    for(var i=0; i<jobsCount; i++) {
      var job = [jobs objectAtIndex:i];

      var descText = [job jobDescription] || "";
      var lines = descText.split("\n").length;

      var linesDescOffset = 0;
      if(lines > 1) {
        linesDescOffset = lines * 20;
      }

      var jobView = [[JobView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, jobRectHeight + linesDescOffset) andDelegate:self andJob:job];
      [jobView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
      [contentView addSubview:jobView];

      marginTopCounter = marginTopCounter + jobRectHeight + linesDescOffset;
    }

    // candidate's studies section

    var educationsCount = [educations count];
    if(educationsCount > 0) {
      var educationSectionView = [[CPView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, 40)];
      var sectionLabel = [[CPTextField alloc] initWithFrame:CGRectMake(40,10, 300, 30)];
      [sectionLabel setStringValue:@"Academic Background"];
      [sectionLabel setFont:[CPFont boldFontWithName:@"Arial" size:18]];
      [educationSectionView addSubview:sectionLabel];
      [educationSectionView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
      [educationSectionView setBackgroundColor:[CPColor whiteColor]];
      [contentView addSubview:educationSectionView];
      marginTopCounter = marginTopCounter + 40;
    }
    for(var i=0; i<educationsCount; i++) {
      var education = [educations objectAtIndex:i];
      var educationView = [[EducationView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width, educationRectHeight) andDelegate:self];
      [educationView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
      [educationView setEducation:education];
      [contentView addSubview:educationView];

      marginTopCounter = marginTopCounter + educationRectHeight;
    }


  }

  [self drawPadding];

  [contentView setBounds:CGRectMake(0,
                                    0,
                                    CGRectGetWidth([scrollView bounds]),
                                    marginTopCounter+40)];
  [contentView setFrame:CGRectMake(0,
                                   0,
                                   CGRectGetWidth([scrollView frame]),
                                   marginTopCounter+40)];

  [scrollView setDocumentView:contentView];

}

-(void)drawPadding {
  if(marginTopCounter < minHeight) {
    var rect = CGRectMake(marginLeft,marginTopCounter,width, (minHeight - marginTopCounter));

    marginTopCounter = minHeight + 20;

    paddingView = [[CPView alloc] initWithFrame:rect];
    [paddingView setAutoresizingMask:CPViewWidthSizable];

    [paddingView setBackgroundColor:[CPColor whiteColor]];
    [contentView addSubview:paddingView];
  }
}

// Handling of graphs
-(void)graphCreated:(id)aGraph
{
  switch ([aGraph kind]) {
  case "Candidate" : [self candidateCreated: aGraph]; break;
  }
}

-(void)graphLoaded:(id)aGraph
{
  debugger;
  switch ([aGraph kind]) {
  case "Candidate" : [self candidateLoaded: aGraph]; break;
  }
}

-(void)syncFinished:(id)sender
{

}

// Handling candidates
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
      [toolbarItem setAction:@selector(addJobSection:)];
      [toolbarItem setLabel:"Work Experience"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    } else if(anItemIdentifier == SyncItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"sync3.png"] size:CPSizeMake(30, 30)];
      [toolbarItem setImage:image];
      [toolbarItem setTarget:self];
      [toolbarItem setAction:@selector(doSync:)];
      [toolbarItem setLabel:"Sync"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    }  else if(anItemIdentifier == SemanticItemIdentifier) {
      var mainBundle = [CPBundle mainBundle];

      var image = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"rdf.png"] size:CPSizeMake(30, 30)];
//    var highlighted = [[CPImage alloc] initWithContentsOfFile:[mainBundle pathForResource:@"removeHighlighted.png"]
//                                       size:CPSizeMake(30, 25)];
      [toolbarItem setImage:image];
//    [toolbarItem setAlternateImage:highlighted];
      [toolbarItem setTarget:self];
      [toolbarItem setAction:@selector(doTriples:)];
      [toolbarItem setLabel:"Triples"];
      [toolbarItem setMinSize:CGSizeMake(32, 32)];
      [toolbarItem setMaxSize:CGSizeMake(32, 32)];
    }

    return toolbarItem;
}

-(void)addEducationSection:(id)sender {
  var education  = [[Education alloc] initForCandidate:candidate];
  [Backend registerNode:education];

  [educations addObject:education];
  var educationView = [[EducationView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width) andDelegate:self];
  [educationView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
  [educationView setEducation:education];
  [self drawPadding];
  [educationView editNewEducation];
}

-(void)educationAdded:(id)sender {
  [self redrawCV];
}

-(void)addJobSection:(id)sender {
  var job  = [[Job alloc] initForCandidate:candidate];
  [Backend registerNode:job];

  [jobs addObject:job];
  var jobView = [[JobView alloc] initWithFrame:CGRectMake(marginLeft, marginTopCounter, width) andDelegate:self andJob:job];
  [jobView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];
  [self drawPadding];
  [jobView editNewJob];
}

-(void)jobAdded:(id)sender {
  [self redrawCV];
}


-(void)doSync:(id)sender {
  var sync = [[SyncController alloc] initWithDelegate:self];
  [sync sync];
}

-(void)doTriples:(id)sender {
  var triples = [[TriplesController alloc] initWithDelegate:self];
  [triples reloadWin];
}

@end
