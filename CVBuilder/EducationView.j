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
@import "LPMultiLineTextField.j"

@implementation EducationView : CPView
{

  BOOL isCreation;

  CPTextField startDateTextField;
  CPTextField endDateTextField;
  CPTextField dashTextField;
  CPTextField degreeTextField;
  CPTextField institutionNameTextField;
  CPTextField educationDescriptionTextField;

  CPButton    editBtn;
  CPButton    deleteBtn;
  Education   education;


  CPWindow    editWin;

  DatePicker  editStartDatePicker;
  CPTextField editStartDatePickerLabel;
  DatePicker  editEndDatePicker;
  CPTextField editEndDatePickerLabel;
  CPTextField editDegreeTextField;
  CPTextField editDegreeLabel;
  CPTextField editInstitutionName;
  CPTextField editInstitutionNameLabel;
  CPTextField editInstitutionUri;
  CPTextField editInstitutionUriLabel;
  LPMultiLineTextField editEducationDescriptionTextField;
  CPTextField editEducationDescriptionLabel;


  id delegate;
}

- (id)initWithFrame:(CGRect)frame andDelegate:aDelegate {
    self = [super initWithFrame:frame];

    if(self) {
      isCreation = false;

      delegate  = aDelegate;

      var marginLeft     = 40;
      var marginLeftForm = 120;
      var topOffset      = 40;
      var labelWidth     = 100;
      var formWidth      = 500;
      var height         = 40

      startDateTextField = [[CPTextField alloc] initWithFrame:CGRectMake(40, topOffset, 70, height)];
      [startDateTextField setBackgroundColor:[CPColor whiteColor]];
      [startDateTextField setFont:[CPFont fontWithName:@"Arial" size:15]];

      dashTextField =  [[CPTextField alloc] initWithFrame:CGRectMake(110, topOffset, 20, height)];
      [dashTextField setBackgroundColor:[CPColor whiteColor]];
      [dashTextField setFont:[CPFont fontWithName:@"Arial" size:15]];
      [dashTextField setStringValue:@" - "];

      endDateTextField = [[CPTextField alloc] initWithFrame:CGRectMake(130, topOffset, 90, height)];
      [endDateTextField setBackgroundColor:[CPColor whiteColor]];
      [endDateTextField setFont:[CPFont fontWithName:@"Arial" size:15]];

      degreeTextField  = [[CPTextField alloc] initWithFrame:CGRectMake(230, topOffset, formWidth, 30)];
      [degreeTextField setBackgroundColor:[CPColor whiteColor]];
      [degreeTextField setFont:[CPFont boldFontWithName:@"Arial" size:18]];


      topOffset = topOffset + height;

      institutionNameTextField = [[CPTextField alloc] initWithFrame:CGRectMake(230, topOffset, formWidth, height)];
      [institutionNameTextField setBackgroundColor:[CPColor whiteColor]];
      [institutionNameTextField setFont:[CPFont fontWithName:@"Arial" size:15]];

      topOffset = topOffset + height;

      var descText = [education educationDescription] || "";
      var lines = descText.split("\n").length;
      linesDescOffset = 0;
      console.log("Found "+ linesDescOffset + " lines");
      if(lines > 1) {
        linesDescOffset = lines * 20;
      }

      educationDescriptionTextField = [[CPTextField alloc] initWithFrame:CGRectMake(230, topOffset, formWidth, height + linesDescOffset)];
      [educationDescriptionTextField setBackgroundColor:[CPColor whiteColor]];
      [educationDescriptionTextField setFont:[CPFont fontWithName:@"Arial" size:15]];


      editBtn = [[CPButton alloc] initWithFrame:CGRectMake(CGRectGetWidth(frame) - 130, 30, 80, 24)];

      [editBtn setTitle:@"Edit"];
      [editBtn setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/edit.png"]];
      [editBtn setTarget:self];
      [editBtn setAction:@selector(editEducation:)];

      deleteBtn = [[CPButton alloc] initWithFrame:CGRectMake(CGRectGetWidth(frame) - 130, 60, 80, 24)];

      [deleteBtn setTitle:@"Delete"];
      [deleteBtn setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/delete.png"]];
      [deleteBtn setTarget:self];
      [deleteBtn setAction:@selector(deleteEducation:)];

    }
    return self;
}

-(void)updateEducation {
  [startDateTextField setStringValue:[education startDate]];
  [endDateTextField setStringValue:[education endDate]];
  [degreeTextField setStringValue:[education degreeType]];
  [institutionNameTextField setStringValue:[education studiedInOrganizationName]];
  [educationDescriptionTextField setStringValue:[education educationDescription]];
}

-(void)setEducation:(Education)anEducation
{

  education = anEducation;

  // Created the UI components

  [self setBackgroundColor:[CPColor whiteColor]];
  [self addSubview:startDateTextField];
  [self addSubview:dashTextField];
  [self addSubview:endDateTextField];
  [self addSubview:degreeTextField];
  [self addSubview:institutionNameTextField];
  [self addSubview:educationDescriptionTextField];

  [self updateEducation];
}

- (void)drawRect:(CPRect)aRect {
    // Drawing code here.
  [super drawRect:aRect];
}

-(void)mouseEntered:(CPEvent)anEvent {
  var selectedColor = [CPColor colorWithHexString:@"dee4eb"];

  [self setBackgroundColor:selectedColor];

  [startDateTextField setBackgroundColor:selectedColor];
  [dashTextField setBackgroundColor:selectedColor];
  [endDateTextField setBackgroundColor:selectedColor];
  [degreeTextField setBackgroundColor:selectedColor];
  [institutionNameTextField setBackgroundColor:selectedColor];
  [educationDescriptionTextField setBackgroundColor:selectedColor];

  // Edit button
  [editBtn setFrame:CGRectMake(CGRectGetWidth([self frame]) - 130, 30, 80, 24)];
  [self addSubview:editBtn];

  [deleteBtn setFrame:CGRectMake(CGRectGetWidth([self frame]) - 130, 60, 80, 24)];
  [self addSubview:deleteBtn];

}


-(void)mouseExited:(CPEvent)anEvent {
  var unselectedColor = [CPColor whiteColor];


  [self setBackgroundColor:unselectedColor];

  [startDateTextField setBackgroundColor:unselectedColor];
  [dashTextField setBackgroundColor:unselectedColor];
  [endDateTextField setBackgroundColor:unselectedColor];
  [degreeTextField setBackgroundColor:unselectedColor];
  [institutionNameTextField setBackgroundColor:unselectedColor];
  [educationDescriptionTextField setBackgroundColor:unselectedColor];

  [editBtn removeFromSuperview];
  [deleteBtn removeFromSuperview];
}

-(void)editNewEducation {
  isCreation = YES;
  [self editEducation:self];
}

-(void)deleteEducation:(id)sender {
  [delegate educationDeleted:education];
}

-(void)editEducation:(id)sender {
  editWin = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,290) styleMask:CPTitledWindowMask];
  var contentView = [editWin contentView];
  [contentView setBackgroundColor:[CPColor colorWithHexString:@"e6e8ea"]];

  var xPosLabel = 30;
  var xPosField = 170;
  var widthLabel = 140;
  var widthField = 300;
  var initialVPos = 40;
  var vInc = 25;
  var height = 20;

  [editWin makeKeyAndOrderFront:self];
  var title = @"Academic formation ";
  [editWin setTitle:title];
  [CPApp runModalForWindow:editWin];


  editDegreeLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editDegreeLabel setStringValue:@"Degree"];
  [editDegreeLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editDegreeTextField =[[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editDegreeTextField setBackgroundColor:[CPColor whiteColor]];
  [editDegreeTextField setEditable:YES];
  [editDegreeTextField setStringValue:[education degreeType]];

  initialVPos = initialVPos + vInc;

  editStartDatePickerLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos+5, widthLabel, height)];
  [editStartDatePickerLabel setStringValue:@"Start date"];
  [editStartDatePickerLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editStartDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField -5, initialVPos, widthField+5, height)];
  [editStartDatePicker displayPreset:1];
  if([education startDate]) {
    var date = new Date([education startDate]);
    [editStartDatePicker setDate:date];
  }

  initialVPos = initialVPos + vInc + 10;

  editEndDatePickerLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editEndDatePickerLabel setStringValue:@"End date"];
  [editEndDatePickerLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editEndDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField-5, initialVPos, widthField+5, height)];
  [editEndDatePicker displayPreset:1];
  if([education endDate]) {
    var date = new Date([education endDate]);
    [editEndDatePicker setDate:date];
  }

  initialVPos = initialVPos + vInc + 10;

  editInstitutionNameLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editInstitutionNameLabel setStringValue:@"Institution name"];
  [editInstitutionNameLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editInstitutionNameTextField =[[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editInstitutionNameTextField setBackgroundColor:[CPColor whiteColor]];
  [editInstitutionNameTextField setEditable:YES];
  [editInstitutionNameTextField setStringValue:[education studiedInOrganizationName]];

  initialVPos = initialVPos + vInc + 5;

  editEducationDescriptionLabel = [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editEducationDescriptionLabel setStringValue:@"Description"];
  [editEducationDescriptionLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editEducationDescriptionTextField =[[LPMultiLineTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height * 2.5)];
  [editEducationDescriptionTextField setBackgroundColor:[CPColor whiteColor]];
  [editEducationDescriptionTextField setEditable:YES];
  [editEducationDescriptionTextField setStringValue:[education educationDescription]];

  initialVPos = initialVPos + vInc + vInc;

  [contentView addSubview:editStartDatePickerLabel];
  [contentView addSubview:editStartDatePicker];
  [contentView addSubview:editEndDatePickerLabel];
  [contentView addSubview:editEndDatePicker];
  [contentView addSubview:editDegreeLabel];
  [contentView addSubview:editDegreeTextField];
  [contentView addSubview:editInstitutionNameLabel];
  [contentView addSubview:editInstitutionNameTextField];
  [contentView addSubview:editEducationDescriptionLabel];
  [contentView addSubview:editEducationDescriptionTextField];


  var okButton = [[CPButton alloc] initWithFrame:CGRectMake(160,initialVPos+30, 70, 24)];
  [okButton setTitle:@"Ok"];

  var cancelButton = [[CPButton alloc] initWithFrame:CGRectMake(250,initialVPos+30, 70, 24)];
  [cancelButton setTitle:@"Cancel"];

  [contentView addSubview:okButton];
  [contentView addSubview:cancelButton];


  // actions

  [okButton setTarget:self];
  [okButton setAction:@selector(doEducationEdit:)];

  if(isCreation) {
    [cancelButton setTarget:self];
    [cancelButton setAction:@selector(cancelEducationCreation:)];

  } else {
    [cancelButton setTarget:self];
    [cancelButton setAction:@selector(cancelEducationEdit:)];

  }

}


-(void)cancelEducationEdit:(id)sender {
  [editWin close];
  [CPApp abortModal];
}

-(void)cancelEducationCreation:(id)sender {
  [editWin close];
  [CPApp abortModal];
  [self removeFromSuperview];
  [delegate removeEducation:education];
}

-(void)doEducationEdit:(id)sender {
  var date = [editStartDatePicker date];
  var dateString = date.getMonth()+1 + "/" + date.getDate() + "/" + date.getFullYear();
  [education setStartDate:dateString];

  date = [editEndDatePicker date];
  dateString = date.getMonth()+1 + "/" + date.getDate() + "/" + date.getFullYear();
  [education setEndDate:dateString];


  [education setDegreeType:[editDegreeTextField stringValue]];
  [education setStudiedInOrganizationName:[editInstitutionNameTextField stringValue]];
  [education setEducationDescription:[editEducationDescriptionTextField stringValue]];

  [editWin close];
  [CPApp abortModal];

  if(isCreation) {
    [delegate educationAdded:self];
    isCreation = NO;
  }

  [self updateEducation];
}

@end
