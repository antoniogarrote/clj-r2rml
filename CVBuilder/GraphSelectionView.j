/*
 *  GraphSelectionView.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/19/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <AppKit/AppKit.j>
@import <Foundation/Foundation.j>
@import "GraphLoader.j";
@import "GraphItemView.j";

@implementation GraphSelectionView : CPObject
{
  id delegate;
  CPWindow theWin;
  CPContentView content;
  CPScrollView scrollView;
  CPView graphsView;
  CPView parentView;
  CPTextField createGraphLabel;
  CPButton createGraphButton;
  id cvs;
  int windowWidth;
  int windowHeight;
  int scrollTopOffset;
}

- (id)initWithParentView:aView andDelegate:aDelegate {

  self = [super init];

  delegate = aDelegate;
  parentView = aView;

  scrollTopOffset = 240;
  windowWidth = 700;
  cvs = [];

  theWin = [[CPWindow alloc] initWithContentRect:CGRectMake(100,100,windowWidth,300) styleMask:CPHUDBackgroundWindowMask|CPTitledWindowMask];
  [theWin setTitle:@"Welcome to CVBuilder"];
  contentView = [theWin contentView];


  [theWin makeKeyAndOrderFront:parentView];
  [CPApp runModalForWindow:theWin];

  [[[GraphLoader alloc] initWithDelegate:self] loadCVS];

  [self redrawWindow];

  return self;
}


- (void)drawRect:(CPRect)aRect {
  // Drawing code here.
  [super drawRect:aRect];
}

- (void)cvsLoaded:(id)json {
  cvs = json;
  [self redrawWindow];
}

- (void)redrawWindow {
  // cleaning old views
  var views = [contentView subviews];
  var viewsCount = [views count];
  for(var i=0; i<viewsCount; i++) {
    var view = [views objectAtIndex:i];
    [view removeFromSuperview];
  }


  scrollView = [[CPScrollView alloc] initWithFrame:CGRectMake(0,0,windowWidth,scrollTopOffset)];
  [scrollView setAutohidesScrollers:YES];
  [scrollView setAutoresizingMask:CPViewWidthSizable | CPViewHeightSizable];

  graphViews = [];

  var nextPosition = 0;
  var totalHeight = 0;

  if(cvs.length > 0) {
    cvs = cvs[0];
    if(typeof(cvs["foaf:maker"]) === "string") {
      cvs["foaf:maker"] = [cvs["foaf:maker"]];
    }
  } else {
    cvs =  {"foaf:maker":[]};
  }

  var counter = 0;

  var frameWidth = windowWidth - 15;

  for(var i=0; i<cvs["foaf:maker"].length; i++){
    nextPosition = totalHeight;
    var cv = {"#": cvs["#"],
              "foaf:maker": cvs["foaf:maker"][i] };
    var graphView = [[GraphItemView alloc] initWithFrame:CGRectMake(0,nextPosition, frameWidth, 44) andGraphUri:cv andDelegate:self];
    if((counter % 2) === 0) {
      [graphView setBackgroundColor:[CPColor colorWithHexString:@"E6E6FA"]];
    } else {
      [graphView setBackgroundColor:[CPColor whiteColor]];
    }
    graphViews.push(graphView);
    totalHeight = nextPosition + 44;
    counter++;
  }

  while(totalHeight < scrollTopOffset) {
    nextPosition = totalHeight;

    var padding = [[CPView alloc] initWithFrame:CGRectMake(0, nextPosition, frameWidth, 44)];

    if((counter % 2) === 0) {
      [padding setBackgroundColor:[CPColor colorWithHexString:@"E6E6FA"]];
    } else {
      [padding setBackgroundColor:[CPColor whiteColor]];
    }
    graphViews.push(padding);

    totalHeight = nextPosition + 44;
    counter++;
  }

  // tmp
  if(totalHeight === 0) {
    totalHeight = scrollTopOffset;
  }
  graphsView = [[CPView alloc] initWithFrame:CGRectMake(0,0, frameWidth, totalHeight)];
  [graphsView setBackgroundColor:[CPColor whiteColor]];
  [scrollView setDocumentView:graphsView];
  for(var i=0; i<graphViews.length; i++) {
    var view = graphViews[i];
    [graphsView addSubview:view];
  }
  if(graphViews.length === 0) {
    var defaultView = [[CPTextField alloc] initWithFrame:CGRectMake(10,10,frameWidth,40)];
    [defaultView setBackgroundColor:[CPColor whiteColor]];
    [defaultView setFont:[CPFont fontWithName:@"Arial" size:14]];
    [defaultView setTextColor:[CPColor grayColor]];
    [defaultView setStringValue:@"No CVs created yet."];
    [graphsView addSubview:defaultView];
  }
  //

  createGraphLabel= [[CPTextField alloc] initWithFrame:CGRectMake(240, 260, 200, 24)];
  [createGraphLabel setStringValue:@"Create a new CV: "];
  [createGraphLabel setFont:[CPFont fontWithName:@"Arial" size:14]];
  [createGraphLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];
  [createGraphLabel setTextColor:[CPColor whiteColor]];

  createGraphButton =[[CPButton alloc] initWithFrame:CGRectMake(380, 260, 100, 24)];
  [createGraphButton setTitle:@"Create"];
  [createGraphButton setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];
  [createGraphButton setTarget:self];
  [createGraphButton setAction:@selector(createGraph:)];

  [contentView addSubview:scrollView];
  [contentView addSubview:createGraphLabel];
  [contentView addSubview:createGraphButton];

}


-(void)createGraph:(id)sender
{
  [CPApp abortModal];
  [theWin close];

  [delegate editNewCV];
}

-(void)candidateLoaded:candidate{
  [CPApp abortModal];
  [theWin close];

  [delegate candidateLoaded:candidate];
}

-(void)educationLoaded:(Education)anEducation
{
  [delegate educationLoaded:anEducation];
}

-(void)jobLoaded:(Job)aJob
{
  [delegate jobLoaded:aJob];
}

@end
