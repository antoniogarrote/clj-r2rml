/*
 *  GraphLoader.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */
@import <Foundation/Foundation.j>
@import "Backend.j"
@import "Candidate.j"
@import "Education.j"
@import "Job.j"

@implementation GraphLoader : CPObject
{
  id delegate;
  id candidate;
  id state;
  int maxTriples;
  CPWindow win;
  CPProgressIndicator progressBar;
  CPTextField notification;
}

-(id)initWithDelegate:(id)aDelegate {
  self = [super init];

  if(self) {
    delegate = aDelegate;
    maxTriples = 3.0;
  }

  return self;
}

// Loading a use graph
-(void)loadCandidateGraph:(CPString)uri
{
  [self reloadWin];
  [[[Candidate alloc] init] loadFromURL:uri withNetworkDelegate:self];
}

-(void)graphLoaded:(id)aGraph
{
  candidate = aGraph;
  [Backend registerNode:candidate];
  [delegate candidateLoaded:aGraph];
  [progressBar incrementBy:1.0];
  [self loadEducations];
}

-(void)loadEducations
{
  state = "LOAD_EDUCATIONS_STEP";


  var uriTmp = [candidate uri];
  uriTmp = uriTmp.split("#")[0];
  uriTmp = uriTmp.replace("cvapi:",[Backend apiEndpoint]+"/");
  var endpoint = [uriTmp stringByAppendingString: @"/educations"];


  var request = [[CPURLRequest alloc] initWithURL:endpoint];
  [request setHTTPMethod:@"GET"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"LOAD";

  var urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];

}

-(void)loadCVS
{
  maxTriples = 1;

  state = "LOAD_CVS_STEP";

  var endpoint = [[Backend apiEndpoint] stringByAppendingString: @"/cvs"];

  if(jQuery) {
    jQuery.ajax({
      url: endpoint,
      contentType: "application/json",
      dataType: "json",
      success: function(objs){
          [delegate cvsLoaded:objs];
      },
      error: function() {
        [AppController networkError];
      }
    });
  } else {
    var request = [[CPURLRequest alloc] initWithURL:endpoint];
    [request setHTTPMethod:@"GET"];
    [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

    networkOperation = @"LOAD";

    var urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  }

}

-(void)loadJobs
{
  state = "LOAD_JOBS_STEP";


  var uriTmp = [candidate uri];
  uriTmp = uriTmp.split("#")[0];
  uriTmp = uriTmp.replace("cvapi:",[Backend apiEndpoint]+"/");
  var endpoint = [uriTmp stringByAppendingString: @"/jobs"];

  var request = [[CPURLRequest alloc] initWithURL:endpoint];
  [request setHTTPMethod:@"GET"];
  [request setValue:"application/json" forHTTPHeaderField:@"Content-Type"];

  networkOperation = @"LOAD";

  var urlConnection = [CPURLConnection connectionWithRequest:request delegate:self];
  [urlConnection start];
}

-(void)graphLoaded
{
  [win close];
  [CPApp abortModal];
}

-(void)connection:(CPURLConnection)connection didFailWithError:(CPString)error {
  [AppController networkError];
}


-(void)connection:(CPURLConnection)aConnection didReceiveData:(CPString)data {
  objs = [data objectFromJSON];

  if(state === "LOAD_EDUCATIONS_STEP") {
    [progressBar incrementBy:1.0];
    for(var i=0; i<objs.length; i++) {
      var education = [[Education alloc] initForCandidate:candidate];
      [education clean];
      [education hasBeenLoaded];
      [education setTriples:objs[i]];
      [Backend registerNode:education];
      [delegate educationLoaded:education];
    }

    [self loadJobs];

  } else if(state === "LOAD_CVS_STEP"){
    if(data === "") {
      // this seems to be a bug in Cappuccino, didFailWithError is never invoked
      [AppController networkError];
    } else {
      [delegate cvsLoaded:objs];
    }
  } else {
    [progressBar incrementBy:1.0];
    for(var i=0; i<objs.length; i++) {
      var job = [[Job alloc] initForCandidate:candidate];
      [job clean];
      [job hasBeenLoaded];
      [job setTriples:objs[i]];
      [Backend registerNode:job];
      [delegate jobLoaded:job];
    }

    [self graphLoaded];
  }
}

-(void)reloadWin
{
  win = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,290) styleMask:CPTitledWindowMask];
  [win setTitle:@"Loading RDF graph"];
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
  [progressBar setMaxValue:maxTriples];

  notification = [[CPTextField alloc] initWithFrame:CGRectMake(224,190,420,20)];

  [notification setStringValue:@"Loading..."];
  [notification setFont:[CPFont boldFontWithName:@"Arial" size:12]];
  [[win contentView] addSubview:notification];

  [[win contentView] addSubview:progressBar];

  notification = [[CPTextField alloc] initWithFrame:CGRectMake(35,190,420,20)];

  [win makeKeyAndOrderFront:self];
  [CPApp runModalForWindow:win];
}

@end
