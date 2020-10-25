#import <GoogleMobileAds/GADBannerView.h>
#import "app_delegate.h"
#include "godotAdmob.h"

#define BANNER_ENABLE_DELAY 5
 
@interface AdmobBanner: NSObject <GADBannerViewDelegate> {
    GodotAdmob *delegate;
    GADBannerView *bannerView;
    bool initialized;
    bool isReal;
    bool isOnTop;
    NSString *adUnitId;
    ViewController *rootController;
}

- (void)initialize:(GodotAdmob*)delegate_ptr :(BOOL)is_real;
- (void)loadBanner:(NSString*)bannerId :(BOOL)is_on_top;
- (void)showBanner;
- (void)hideBanner;
- (void)disableBanner;
- (void)enableBanner;
- (void)resize;
- (int)getBannerWidth;
- (int)getBannerHeight;

@end
