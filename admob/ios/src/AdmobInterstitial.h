#import <GoogleMobileAds/GADInterstitial.h>
#import "app_delegate.h"
#include "godotAdmob.h"

@interface AdmobInterstitial: NSObject <GADInterstitialDelegate> {
    GodotAdmob *delegate;
    GADInterstitial *interstitial;
    bool initialized;
    bool isReal;
    ViewController *rootController;
}

- (void)initialize:(GodotAdmob*)delegate_ptr :(BOOL)is_real;
- (void)loadInterstitial:(NSString*)interstitialId;
- (void)showInterstitial;

@end
