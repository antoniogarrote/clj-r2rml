/*
 *  GraphItemView.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/19/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */
@import <Foundation/Foundation.j>
@import <AppKit/CPAccordionView.j>

@implementation GraphItemView : CPView
{
  CPString graphUri;
  id delegate;
  CPTextField uriTextField;
  CPButton loadButton;
  CPButton viewButton;
  CPColor oldColor;
}

-(id)initWithFrame:frame andGraphUri:acl andDelegate:aDelegate {
  self = [super initWithFrame:frame];

  if(self) {
    graphUri = acl["foaf:maker"];

    graphUri = graphUri.replace("cvapi:",acl["#"]["cvapi"]);
    delegate = aDelegate;

    [self setBackgroundColor:[CPColor whiteColor]];
    uriTextField = [[CPTextField alloc] initWithFrame:CGRectMake(10,10,400,24)];
    [uriTextField setStringValue:graphUri.split("#self")[0]];
    [uriTextField setFont:[CPFont boldFontWithName:@"Arial" size:12]];

    [self addSubview:uriTextField];
  }
  return self;
}

-(void)loadCV:(id)sender
{
  var loader = [[GraphLoader alloc] initWithDelegate:delegate];
  [loader loadCandidateGraph:graphUri];
}

-(void)viewCV:(id)sender
{
  var uri = graphUri.split("#self")[0];
  uri = uri.replace("api/candidates", "cvs");
  window.open(uri,'preview CV');
}

-(void)mouseEntered:(CPEvent)anEvent {
  oldColor = [self backgroundColor];
  [self setBackgroundColor:[CPColor colorWithHexString:@"F0E68C"]];
  loadButton = [[CPButton alloc] initWithFrame:CGRectMake(510, 10, 80, 24)];
  [loadButton setTitle:@"Load"];
  [loadButton setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/open.png"]];
  [loadButton setTarget:self];
  [loadButton setAction:@selector(loadCV:)];

  viewButton = [[CPButton alloc] initWithFrame:CGRectMake(600, 10, 80, 24)];
  [viewButton setTitle:@"View"];
  [viewButton setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/find.png"]];
  [viewButton setTarget:self];
  [viewButton setAction:@selector(viewCV:)];

  [self addSubview:viewButton];
  [self addSubview:loadButton];
}

-(void)mouseExited:(CPEvent)anEvent {
  [loadButton removeFromSuperview];
  [viewButton removeFromSuperview];
  [self setBackgroundColor:oldColor];
}

@end
