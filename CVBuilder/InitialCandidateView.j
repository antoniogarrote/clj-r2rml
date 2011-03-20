/*
 *  CandidateView.j
 *  TestXib
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
 */

@import <AppKit/AppKit.j>
@import <Foundation/Foundation.j>
@import "Candidate.j"
@import "DatePicker.j"

@implementation InitialCandidateView : CPObject
{
  Candidate   candidate;
  id delegate

  CPWindow    editWin;
  CPTextField editNameFieldLabel;
  CPTextField editNameField;
  CPTextField editFamilyNameFieldLabel
  CPTextField editFamilyNameField;
  CPTextField editAddressFieldLabel;
  CPTextField editAddressField;
  CPTextField editTelephoneFieldLabel
  CPTextField editTelephoneField;
  CPTextField editBDayFieldLabel;
  DatePicker  editBDayDatePicker;
  CPTextField editEmailFieldLabel;
  CPTextField editEmailField;

}

- (id)initWithDelegate:(id)aDelegate {
    self = [super init];

    if(self) {
      delegate = aDelegate;
      candidate = [[Candidate alloc] init];
    }
    return self;
}

-(void)editCandidate {

  editWin = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,310) styleMask:CPHUDBackgroundWindowMask|CPTitledWindowMask];
  var contentView = [editWin contentView];
  //[contentView setBackgroundColor:[CPColor colorWithHexString:@"e6e8ea"]];
  [contentView  setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  var xPosLabel = 20;
  var xPosField = 120;
  var widthLabel = 100;
  var widthField = 340;
  var initialVPos = 40;
  var vInc = 30;
  var height = 20;

  [editWin makeKeyAndOrderFront:self];
  var title = [[CPString alloc] initWithString:@"Edit profile for: "];
  [editWin setTitle:[title stringByAppendingString:@"new CV candidate"]];
  [CPApp runModalForWindow:editWin];

  editNameFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editNameFieldLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];
  [editNameFieldLabel setStringValue:@"Name"];
  [editNameFieldLabel setTextColor:[CPColor whiteColor]];
  [editNameFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editNameField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editNameField setStringValue:[candidate givenName]];
  [editNameField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editNameField setBackgroundColor:[CPColor whiteColor]];
  [editNameField setEditable:YES];
  [editNameField setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  initialVPos = initialVPos + vInc;


  editFamilyNameFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editFamilyNameFieldLabel setStringValue:@"FamilyName"];
  [editFamilyNameFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  [editFamilyNameFieldLabel setTextColor:[CPColor whiteColor]];
  [editFamilyNameFieldLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  editFamilyNameField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editFamilyNameField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editFamilyNameField setStringValue:[candidate familyName]];
  [editFamilyNameField setBackgroundColor:[CPColor whiteColor]];
  [editFamilyNameField setEditable:YES];
  [editFamilyNameField setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  initialVPos = initialVPos + vInc;


  editAddressFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editAddressFieldLabel setStringValue:@"Address"];
  [editAddressFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  [editAddressFieldLabel setTextColor:[CPColor whiteColor]];
  [editAddressFieldLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  editAddressField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editAddressField setStringValue:[candidate address]];
  [editAddressField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editAddressField setBackgroundColor:[CPColor whiteColor]];
  [editAddressField setEditable:YES];
  [editAddressField setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  initialVPos = initialVPos + vInc;

  editTelephoneFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editTelephoneFieldLabel setStringValue:@"Telephone"];
  [editTelephoneFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  [editTelephoneFieldLabel setTextColor:[CPColor whiteColor]];
  [editTelephoneFieldLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  editTelephoneField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editTelephoneField setStringValue:[candidate telephone]];
  [editTelephoneField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editTelephoneField setBackgroundColor:[CPColor whiteColor]];
  [editTelephoneField setEditable:YES];
  [editTelephoneField setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];
  initialVPos = initialVPos + vInc;


  editBDayFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editBDayFieldLabel setStringValue:@"Birth Day"];
  [editBDayFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  [editBDayFieldLabel setTextColor:[CPColor whiteColor]];
  [editBDayFieldLabel  setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  editBDayDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField -5, initialVPos, widthField+5, height)];
  [editBDayDatePicker displayPreset:1];

  initialVPos = initialVPos + vInc + 10;

  editEmailFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editEmailFieldLabel setStringValue:@"Email"];
  [editEmailFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  [editEmailFieldLabel setTextColor:[CPColor whiteColor]];
  [editEmailFieldLabel setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  editEmailField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editEmailField setStringValue:[candidate email]];
  [editEmailField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editEmailField setBackgroundColor:[CPColor whiteColor]];
  [editEmailField setEditable:YES];
  [editEmailField setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];
  initialVPos = initialVPos + vInc;


  [contentView addSubview:editNameFieldLabel];
  [contentView addSubview:editNameField];
  [contentView addSubview:editFamilyNameFieldLabel];
  [contentView addSubview:editFamilyNameField];
  [contentView addSubview:editAddressFieldLabel];
  [contentView addSubview:editAddressField];
  [contentView addSubview:editTelephoneFieldLabel];
  [contentView addSubview:editTelephoneField];
  [contentView addSubview:editBDayFieldLabel];
  [contentView addSubview:editBDayDatePicker];
  [contentView addSubview:editEmailFieldLabel];
  [contentView addSubview:editEmailField];




  var okButton = [[CPButton alloc] initWithFrame:CGRectMake(160,initialVPos+30, 70, 24)];
  [okButton setTitle:@"Ok"];
  [okButton setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  var cancelButton = [[CPButton alloc] initWithFrame:CGRectMake(250,initialVPos+30, 70, 24)];
  [cancelButton setTitle:@"Cancel"];
  [cancelButton setTheme:[CPTheme themeNamed:@"Aristo-HUD"]];

  [contentView addSubview:okButton];
  [contentView addSubview:cancelButton];


  // actions

  [okButton setTarget:self];
  [okButton setAction:@selector(doCandidateCreate:)]

  [cancelButton setTarget:self];
  [cancelButton setAction:@selector(doCandidateCancel:)]

}

-(void)doCandidateCreate:(id)sender {
  [candidate setGivenName:[editNameField stringValue]];
  [candidate setFamilyName:[editFamilyNameField stringValue]];
  [candidate setTelephone:[editTelephoneField stringValue]];
  [candidate setAddress:[editAddressField stringValue]];
  [candidate setBirthDay:[editBDayDatePicker dateString]];
  [candidate setEmail:[editEmailField stringValue]];

  [candidate saveToEndPointWithNetworkDelegate:self];
}

-(void)doCandidateCancel:(id)sender {
  [editWin close];
  [CPApp abortModal];
  [delegate initialCandidateCanceled];
}

-(void)graphCreated:createdCandidate {
  candidate = createdCandidate;
  [editWin close];
  [CPApp abortModal];
  [delegate initialCandidateCreated:createdCandidate];
}
@end
