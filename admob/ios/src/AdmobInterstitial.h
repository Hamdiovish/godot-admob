#import <GoogleMobileAds/GADInterstitial.h>
#import "app_delegate.h"
#include "godotAdmob.h"

@interface AdmobInterstitial: NSObject <GADInterstitialDelegate> {
    GodotAdmob *delegate;
    GADInterstitial *interstitial;
    bool initialized;
    bool isReal;
    int instanceId;
    ViewController *rootController;
}

- (void)initialize:(GodotAdmob*)delegate_ptr :(BOOL)is_real: (int)instance_id;
- (void)loadInterstitial:(NSString*)interstitialId;
- (void)showInterstitial;

@end
