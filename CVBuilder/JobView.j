/*
 *  EducationView.j
 *  CVBuilder
 *
 *  Created by Antonio Garrote on 3/9/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
*/

@import <AppKit/AppKit.j>
@import <Foundation/Foundation.j>
@import "Job.j"
@import "DatePicker.j"

@implementation JobView : CPView
{

  BOOL isCreation;

  CPTextField startDateTextField;
  CPTextField endDateTextField;
  CPTextField dashTextField;
  CPTextField jobTitleTextField;
  CPTextField jobDescriptionTextField;
  CPTextField companyTextField;

  CPButton    editBtn;
  Education   education;


  CPWindow    editWin;

  DatePicker  editStartDatePicker;
  CPTextField editStartDatePickerLabel;
  DatePicker  editEndDatePicker;
  CPTextField editEndDatePickerLabel;
  CPTextField editJobTitleTextField;
  CPTextField editJobTitleLabel;
  CPTextField editCompanyName;
  CPTextField editCompanyNameLabel;
  CPTextField editJobDescriptionTextField;
  CPTextField editJobDescriptionLabel;

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

      jobTitleTextField  = [[CPTextField alloc] initWithFrame:CGRectMake(230, topOffset, formWidth, 30)];
      [jobTitleTextField setBackgroundColor:[CPColor whiteColor]];
      [jobTitleTextField setFont:[CPFont boldFontWithName:@"Arial" size:18]];

      topOffset = topOffset + height;

      companyTextField = [[CPTextField alloc] initWithFrame:CGRectMake(230, topOffset, formWidth, height)];
      [companyTextField setBackgroundColor:[CPColor whiteColor]];
      [companyTextField setFont:[CPFont fontWithName:@"Arial" size:15]];

      topOffset = topOffset + height;


      editBtn = [[CPButton alloc] initWithFrame:CGRectMake(CGRectGetWidth(frame) - 130, 30, 80, 24)];

      [editBtn setTitle:@"Edit"];
      [editBtn setImage:[[CPImage alloc] initWithContentsOfFile:@"Resources/edit.png"]];
      [editBtn setTarget:self];
      [editBtn setAction:@selector(editEducation:)];
    }
    return self;
}

-(void)updateJob {
  [startDateTextField setStringValue:[education startDate]];
  [endDateTextField setStringValue:[education endDate]];
  [jobTitleTextField setStringValue:[education jobTitleType]];
  [companyTextField setStringValue:[education employedIn]];
}

-(void)setJob:(Job)aJob
{

  education = anEducation;

  // Created the UI components

  [self setBackgroundColor:[CPColor whiteColor]];
  [self addSubview:startDateTextField];
  [self addSubview:dashTextField];
  [self addSubview:endDateTextField];
  [self addSubview:jobTitleTextField];
  [self addSubview:institutionNameTextField];

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
  [jobTitleTextField setBackgroundColor:selectedColor];
  [institutionNameTextField setBackgroundColor:selectedColor];

  // Edit button
  [self addSubview:editBtn];
}


-(void)mouseExited:(CPEvent)anEvent {
  var unselectedColor = [CPColor whiteColor];


  [self setBackgroundColor:unselectedColor];

  [startDateTextField setBackgroundColor:unselectedColor];
  [dashTextField setBackgroundColor:unselectedColor];
  [endDateTextField setBackgroundColor:unselectedColor];
  [jobTitleTextField setBackgroundColor:unselectedColor];
  [institutionNameTextField setBackgroundColor:unselectedColor];

  [editBtn removeFromSuperview];
}

-(void)editNewEducation {
  isCreation = YES;
  [self editEducation:self];
}

-(void)editEducation:(id)sender {
  editWin = [[CPWindow alloc] initWithContentRect:CGRectMake(200,100,500,270) styleMask:CPTitledWindowMask];
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


  editJobTitleLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editJobTitleLabel setStringValue:@"JobTitle"];
  [editJobTitleLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editJobTitleTextField =[[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editJobTitleTextField setBackgroundColor:[CPColor whiteColor]];
  [editJobTitleTextField setEditable:YES];
  [editJobTitleTextField setStringValue:[education jobTitleType]];

  initialVPos = initialVPos + vInc;

  editStartDatePickerLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos+5, widthLabel, height)];
  [editStartDatePickerLabel setStringValue:@"Start date"];
  [editStartDatePickerLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editStartDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField -5, initialVPos, widthField+5, height)];
  [editStartDatePicker displayPreset:1];
  if([education startDate]) {
    // @todo Format date here
    //[editStartDatePicker setDate:[CPDate initWithString:[education startDate]]];
  }

  initialVPos = initialVPos + vInc + 10;

  editEndDatePickerLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editEndDatePickerLabel setStringValue:@"End date"];
  [editEndDatePickerLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editEndDatePicker =[[DatePicker alloc] initWithFrame:CGRectMake(xPosField-5, initialVPos, widthField+5, height)];
  [editEndDatePicker displayPreset:1];
  if([education endDate]) {
    // @todo Format date here
    //[editEndDatePicker setDate:[CPDate initWithString:[education endDate]]];
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

  editInstitutionUriLabel= [[CPTextField alloc] initWithFrame:CGRectMake(xPosLabel, initialVPos, widthLabel, height)];
  [editInstitutionUriLabel setStringValue:@"Institution URI"];
  [editInstitutionUriLabel setFont:[CPFont boldFontWithName:@"Arial" size:14]];
  editInstitutionUriTextField =[[CPTextField alloc] initWithFrame:CGRectMake(xPosField, initialVPos, widthField, height)];
  [editInstitutionUriTextField setBackgroundColor:[CPColor whiteColor]];
  [editInstitutionUriTextField setEditable:YES];

  initialVPos = initialVPos + vInc;

  [contentView addSubview:editStartDatePickerLabel];
  [contentView addSubview:editStartDatePicker];
  [contentView addSubview:editEndDatePickerLabel];
  [contentView addSubview:editEndDatePicker];
  [contentView addSubview:editJobTitleLabel];
  [contentView addSubview:editJobTitleTextField];
  [contentView addSubview:editInstitutionNameLabel];
  [contentView addSubview:editInstitutionNameTextField];
  [contentView addSubview:editInstitutionUriLabel];
  [contentView addSubview:editInstitutionUriTextField];


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


  [education setJobTitleType:[editJobTitleTextField stringValue]];
  [education setStudiedInOrganizationName:[editInstitutionNameTextField stringValue]];

  [editWin close];
  [CPApp abortModal];

  debugger;

  if(isCreation) {
    [delegate educationAdded:self];
    isCreation = NO;
  }

  [self updateEducation];
}

@end
