#import "ThreadSelfManager.h"
#include <stdlib.h>

@implementation ThreadSelfManager

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;
@synthesize workerManager = _workerManager;
@synthesize threadId = _threadId;

RCT_EXPORT_METHOD(postMessage: (NSString *)message)
{
  if (self.workerManager == nil) {
    NSLog(@"No worker manager defined - abort sending thread message");
    return;
  }

  NSString *eventName = [NSString stringWithFormat:@"Thread%i", self.threadId];

  [self.workerManager checkAndSendEvent:eventName body:message];
}

@end
