#import "app_delegate.h"
#import <GoogleMobileAds/GADRewardBasedVideoAdDelegate.h>
#import "AdmobBanner.h"
#include "godotAdmob.h"

@interface AdmobRewarded: NSObject <GADRewardBasedVideoAdDelegate> {
    GodotAdmob *delegate;
    bool initialized;
    bool isReal;
    ViewController *rootController;
}

- (void)initialize:(GodotAdmob*)delegate_ptr :(BOOL)is_real;
- (void)loadRewardedVideo:(NSString*)rewardedId;
- (void)showRewardedVideo;

@end
