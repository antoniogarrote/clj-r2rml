/*
 * AppController.j
 * CVBuilder
 *
 * Created by You on March 6, 2011.
 * Copyright 2011, Your Company All rights reserved.
 */

@import <Foundation/CPObject.j>
@import <AppKit/CPAccordionView.j>


@implementation InitialPanel : CPObject
{
  CPPanel HUDPanel;
  id delegate;
}

-(id)initWithDelegate:(id)aDelegate
{
  var self = [super init];
  if(self){
    delegate = aDelegate;

    var width = 300;
    var HUDPanel = [[CPPanel alloc] initWithContentRect:CGRectMake(200, 200, 300, 200) styleMask:CPHUDBackgroundWindowMask | CPClosableWindowMask];
    [HUDPanel setFloatingPanel:YES];
    [HUDPanel orderFront:self];
  }

  return self;
}
