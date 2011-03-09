/*
 *  EducationView.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
*/

@import <AppKit/AppKit.j>
@import <Foundation/Foundation.j>
@import "Education.j"
@import "DatePicker.j"

@implementation EducationView : CPView
{
  DatePicker  startDatePicker;
  CPTextField startDateLabel;
  
  CPButton    editBtn;
  Education   education;


  CPWindow    editWin;

  DatePicker  editStartDatePicker;
  CPTextField editStartDatePickerLabel;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];

    if(self) {
      var marginLeft     = 20;
      var marginLeftForm = 120;
      var topOffset      = 40;
      var labelWidth     = 100;
      var formWidth      = 500;
      var height         = 20

      startDateLabel= [[CPTextField alloc] initWithFrame:CGRectMake(marginLeft, topOffset, labelWidth, height)];
      startDatePicker [[DatePicker alloc] initWithContentRect:CGRectMake(marginLeftForm, topOffset, formWidth, height)];
      [startDatePicker displayPreset:1];
      [startDatePicker setDelegate:self];

      editBtn             = [[CPButton alloc] initWithFrame:CGRectMake(CGRectGetWidth(frame) - 130, 30, 80, 24)];

      [editBtn setTitle:@"Edit"];
      [editBtn setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/edit.png"]];
      [editBtn setTarget:self];
      [editBtn setAction:@selector(editCandidate:)];
    }
    return self;
}

-(void)updateCandidate {
  var name = [[[candidate givenName] stringByAppendingString:" "] stringByAppendingString:[candidate familyName]];
  [nameField setStringValue:name];
  [addressField setStringValue:[candidate address]];
  [telephoneField setStringValue:[candidate telephone]];
  [bdayField setStringValue:[candidate birthDay]];
}

-(void)setCandidate:(Candidate)aCandidate
{
	candidate = aCandidate;

        // Created the UI components

        // Name
        [nameField setFont:[CPFont boldFontWithName:@"Arial" size:30]];
        [nameField setBackgroundColor:[CPColor whiteColor]];
        [self addSubview:nameField];

        // Address
        [addressFieldLabel setStringValue: @"Address: "];
        [addressFieldLabel setBackgroundColor:[CPColor whiteColor]];
        [addressFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:15]];

        [addressField setBackgroundColor:[CPColor whiteColor]];
        [addressField setFont:[CPFont fontWithName:@"Arial" size:15]];
        [self addSubview: addressFieldLabel];
        [self addSubview: addressField];

        // Telephone
        [telephoneFieldLabel setStringValue: @"Telephone: "];
        [telephoneFieldLabel setBackgroundColor:[CPColor whiteColor]];
        [telephoneFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:15]];

        [telephoneField setBackgroundColor:[CPColor whiteColor]];
        [telephoneField setFont:[CPFont fontWithName:@"Arial" size:15]];
        [self addSubview: telephoneFieldLabel];
        [self addSubview: telephoneField];

        // Bday
        [bdayFieldLabel setStringValue: @"Birth Day: "];
        [bdayFieldLabel setBackgroundColor:[CPColor whiteColor]];
        [bdayFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:15]];

        [bdayField setBackgroundColor:[CPColor whiteColor]];
        [bdayField setFont:[CPFont fontWithName:@"Arial" size:15]];
        [self addSubview: bdayFieldLabel];
        [self addSubview: bdayField];


        [self updateCandidate];

}

- (void)drawRect:(CPRect)aRect {
    // Drawing code here.
  [super drawRect:aRect];
}

-(void)mouseEntered:(CPEvent)anEvent {
  var selectedColor = [CPColor colorWithHexString:@"dee4eb"];
  [self setBackgroundColor:selectedColor];
  [addressField setBackgroundColor:selectedColor];
  [addressFieldLabel setBackgroundColor:selectedColor];
  [nameField setBackgroundColor:selectedColor];
  [telephoneField setBackgroundColor:selectedColor];
  [telephoneFieldLabel setBackgroundColor:selectedColor];
  [bdayField setBackgroundColor:selectedColor];
  [bdayFieldLabel setBackgroundColor:selectedColor];

  // Edit button
  [self addSubview:editBtn];
}


-(void)mouseExited:(CPEvent)anEvent {
  var unselectedColor = [CPColor whiteColor];
  [self setBackgroundColor:unselectedColor];
  [addressField setBackgroundColor:unselectedColor];
  [addressFieldLabel setBackgroundColor:unselectedColor];
  [nameField setBackgroundColor:unselectedColor];
  [telephoneField setBackgroundColor:unselectedColor];
  [telephoneFieldLabel setBackgroundColor:unselectedColor];
  [bdayField setBackgroundColor:unselectedColor];
  [bdayFieldLabel setBackgroundColor:unselectedColor];

  [editBtn removeFromSuperview];
}

-(void)editCandidate:(id)sender {

  editWin = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,270) styleMask:CPTitledWindowMask];
  var contentView = [editWin contentView];
  [contentView setBackgroundColor:[CPColor colorWithHexString:@"e6e8ea"]];

  var xPosLabel = 20;
  var xPosField = 120;
  var widthLabel = 100;
  var widthField = 340;
  var initialVPos = 40;
  var vInc = 30;
  var height = 20;

  [editWin makeKeyAndOrderFront:self];
  var title = [[CPString alloc] initWithString:@"Edit profile for: "];
  [editWin setTitle:[title stringByAppendingString:[candidate fullName]]];
  [CPApp runModalForWindow:editWin];

  editNameFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editNameFieldLabel setStringValue:@"Name"];
  [editNameFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editNameField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editNameField setStringValue:[candidate givenName]];
  [editNameField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editNameField setBackgroundColor:[CPColor whiteColor]];
  [editNameField setEditable:YES];


  initialVPos = initialVPos + vInc;


  editFamilyNameFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editFamilyNameFieldLabel setStringValue:@"FamilyName"];
  [editFamilyNameFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editFamilyNameField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editFamilyNameField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editFamilyNameField setStringValue:[candidate familyName]];
  [editFamilyNameField setBackgroundColor:[CPColor whiteColor]];
  [editFamilyNameField setEditable:YES];

  initialVPos = initialVPos + vInc;


  editAddressFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editAddressFieldLabel setStringValue:@"Address"];
  [editAddressFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editAddressField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editAddressField setStringValue:[candidate address]];
  [editAddressField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editAddressField setBackgroundColor:[CPColor whiteColor]];
  [editAddressField setEditable:YES];

  initialVPos = initialVPos + vInc;

  editTelephoneFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editTelephoneFieldLabel setStringValue:@"Telephone"];
  [editTelephoneFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editTelephoneField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editTelephoneField setStringValue:[candidate telephone]];
  [editTelephoneField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editTelephoneField setBackgroundColor:[CPColor whiteColor]];
  [editTelephoneField setEditable:YES];

  initialVPos = initialVPos + vInc;


  editBDayFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editBDayFieldLabel setStringValue:@"Birth Day"];
  [editBDayFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editBDayField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editBDayField setStringValue:[candidate birthDay]];
  [editBDayField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editBDayField setBackgroundColor:[CPColor whiteColor]];
  [editBDayField setEditable:YES];

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
  [contentView addSubview:editBDayField];




  var okButton = [[CPButton alloc] initWithFrame:CGRectMake(160,initialVPos+30, 70, 24)];
  [okButton setTitle:@"Ok"];

  var cancelButton = [[CPButton alloc] initWithFrame:CGRectMake(250,initialVPos+30, 70, 24)];
  [cancelButton setTitle:@"Cancel"];

  [contentView addSubview:okButton];
  [contentView addSubview:cancelButton];


  // actions

  [cancelButton setTarget:self];
  [cancelButton setAction:@selector(cancelCandidateEdit:)]

  [okButton setTarget:self];
  [okButton setAction:@selector(doCandidateEdit:)]

}


-(void)cancelCandidateEdit:(id)sender {
  [editWin close];
  [CPApp abortModal];
}

-(void)doCandidateEdit:(id)sender {
  [candidate setGivenName:[editNameField stringValue]];
  [candidate setFamilyName:[editFamilyNameField stringValue]];
  [candidate setTelephone:[editTelephoneField stringValue]];
  [candidate setAddress:[editAddressField stringValue]];
  [candidate setBirthDay:[editBDayField stringValue]];

  [editWin close];
  [CPApp abortModal];

  [self updateCandidate];
}

@end
