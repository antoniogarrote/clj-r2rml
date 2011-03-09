/*
 *  Backend.j
 *  TestXib
 *
 *  Created by Antonio Garrote on 3/6/11.
 *  Copyright Universidad de Salamanca 2011. All rights reserved.
*/

@import <Foundation/Foundation.j>

var configuration;

@implementation Backend : CPObject
{
}

+(void)setGlobalConfiguration:(CPDictionary)aConfiguration
{
	configuration = aConfiguration;
}

+(CPString)apiEndpoint
{
	return [configuration valueForKey:@"apiEndpoint"];
}
@end
