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

@implementation CandidateView : CPView
{
	CPTextField addressField;
	CPTextField addressFieldLabel;
	CPTextField nameField;
	CPTextField telephoneField;
        CPTextField telephoneFieldLabel;
        CPButton    editBtn;
	Candidate   candidate;


        CPWindow    editWin;
        CPTextField editNameFieldLabel;
        CPTextField editNameField;
        CPTextField editFamilyNameFieldLabel
        CPTextField editFamilyNameField;
        CPTextField editAddressFieldLabel;
        CPTextField editAddressField;
        CPTextField editTelephoneFieldLabel;
        CPTextField editTelephoneField;
        CPTextField emailFieldLabel;
        CPTextField emailField;
        CPTextField editBDayFieldLabel;
        DatePicker  editBDayDatePicker;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];

    if(self) {
      nameField           = [[CPTextField alloc] initWithFrame:CGRectMake(40, 40, 500, 40)];
      addressFieldLabel   = [[CPTextField alloc] initWithFrame:CGRectMake(40, 90, 100, 20)];
      addressField        = [[CPTextField alloc] initWithFrame:CGRectMake(140, 90, 400, 20)];
      telephoneFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(40, 110, 100, 20)];
      telephoneField      = [[CPTextField alloc] initWithFrame:CGRectMake(140, 110, 400, 20)];
      bdayFieldLabel      = [[CPTextField alloc] initWithFrame:CGRectMake(40, 130, 100, 20)];
      bdayField           = [[CPTextField alloc] initWithFrame:CGRectMake(140, 130, 400, 20)];
      emailFieldLabel     = [[CPTextField alloc] initWithFrame:CGRectMake(40, 150, 100, 25)];
      emailField	  = [[CPTextField alloc] initWithFrame:CGRectMake(140, 150, 400, 25)];


      editBtn             = [[CPButton alloc] initWithFrame:CGRectMake(CGRectGetWidth(frame) - 130, 30, 80, 24)];
      [editBtn setAutoresizingMask:CPViewMinXMargin | CPViewMinYMargin];
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
  [emailField setStringValue:[candidate email]];
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

        // email
        [emailFieldLabel setStringValue: @"Email: "];
        [emailFieldLabel setBackgroundColor:[CPColor whiteColor]];
        [emailFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:15]];

        [emailField setBackgroundColor:[CPColor whiteColor]];
        [emailField setFont:[CPFont fontWithName:@"Arial" size:15]];
        [self addSubview: emailFieldLabel];
        [self addSubview: emailField];


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
  [emailField setBackgroundColor:selectedColor];
  [emailFieldLabel setBackgroundColor:selectedColor];

  // Edit button
  [editBtn setFrame:CGRectMake(CGRectGetWidth([self frame]) - 130, 30, 80, 24)];
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
  [emailField setBackgroundColor:unselectedColor];
  [emailFieldLabel setBackgroundColor:unselectedColor];

  [editBtn removeFromSuperview];
}

-(void)editCandidate:(id)sender {

  editWin = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,310) styleMask:CPTitledWindowMask];
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

  editBDayDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField -5, initialVPos, widthField+5, height)];
  [editBDayDatePicker displayPreset:1];
  if([candidate birthDay]) {
    var date = new Date([candidate birthDay]);
    [editBDayDatePicker setDate:date];
  }

  initialVPos = initialVPos + vInc + 5;

  editEmailFieldLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editEmailFieldLabel setStringValue:@"Email"];
  [editEmailFieldLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];

  editEmailField = [[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editEmailField setStringValue:[candidate email]];
  [editEmailField setFont:[CPFont fontWithName:@"Arial" size:14]];
  [editEmailField setBackgroundColor:[CPColor whiteColor]];
  [editEmailField setEditable:YES];

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
  [candidate setBirthDay:[editBDayDatePicker dateString]];
  [candidate setEmail:[editEmailField stringValue]];

  [editWin close];
  [CPApp abortModal];

  [self updateCandidate];
}

@end
