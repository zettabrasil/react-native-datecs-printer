
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNDatecsPrinterSpec.h"

@interface DatecsPrinter : NSObject <NativeDatecsPrinterSpec>
#else
#import <React/RCTBridgeModule.h>

@interface DatecsPrinter : NSObject <RCTBridgeModule>
#endif

@end
