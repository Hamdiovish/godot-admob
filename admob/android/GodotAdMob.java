package org.godotengine.godot;

import com.google.android.gms.ads.*;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;
import android.provider.Settings;
import android.graphics.Color;
import android.util.Log;
import java.util.Locale;
import android.view.Gravity;
import android.view.View;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardItem;

public class GodotAdMob extends Godot.SingletonBase
{

	private Activity activity = null; // The main activity of the game
	private int instance_id = 0;

	private InterstitialAd interstitialAd = null; // Interstitial object
	private AdView adView = null; // Banner view

	private boolean isReal = false; // Store if is real or not
	private boolean isForChildDirectedTreatment = false; // Store if is children directed treatment desired
	private boolean isPersonalized = true; // ads are personalized by default, GDPR compliance within the European Economic Area may require you to disable personalization.
	private String maxAdContentRating = ""; // Store maxAdContentRating ("G", "PG", "T" or "MA")
	private Bundle extras = null;


	private FrameLayout layout = null; // Store the layout
	private FrameLayout.LayoutParams adParams = null; // Store the layout params

	private RewardedAd rewardedAd = null; // Rewarded object

	/* Init
	 * ********************************************************************** */

	/**
	 * Prepare for work with AdMob
	 * @param boolean isReal Tell if the enviroment is for real or test
	 * @param int gdscript instance id
	 */
	public void init(boolean isReal, int instance_id) {
		this.initWithContentRating(isReal, instance_id, false, true, "");
	}
	/**
	 * Init with content rating additional options 
	 * @param boolean isReal Tell if the enviroment is for real or test
	 * @param int gdscript instance id
	 * @param boolean isPersonalized If ads should be personalized or not.
	 *  GDPR compliance within the European Economic Area requires that you
	 *  disable ad personalization if the user does not wish to opt into
	 *  ad personalization.
	 * @param String maxAdContentRating must be "G", "PG", "T" or "MA"
	 */
	public void initWithContentRating(boolean isReal, int instance_id, boolean isForChildDirectedTreatment, boolean isPersonalized, String maxAdContentRating)
	{
		this.isReal = isReal;
		this.instance_id = instance_id;
		this.isForChildDirectedTreatment = isForChildDirectedTreatment;
		this.isPersonalized = isPersonalized;
		this.maxAdContentRating = maxAdContentRating;
		if (maxAdContentRating != null && maxAdContentRating != "")
		{
			extras = new Bundle();
			extras.putString("max_ad_content_rating", maxAdContentRating);
		}
		if(!isPersonalized)
		{
			// https://developers.google.com/admob/android/eu-consent#forward_consent_to_the_google_mobile_ads_sdk
			if(extras == null)
			{
				extras = new Bundle();
			}
			extras.putString("npa", "1");
		}
		Log.d("godot", "AdMob: init with content rating options");
	}



	/**
	 * Returns AdRequest object constructed considering the parameters set in constructor of this class.
	 * @return AdRequest object
	 */
	private AdRequest getAdRequest()
	{
		AdRequest.Builder adBuilder = new AdRequest.Builder();
		AdRequest adRequest;
		if (!this.isForChildDirectedTreatment && extras != null)
		{
			adBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
		}
		if (this.isForChildDirectedTreatment)
		{
			adBuilder.tagForChildDirectedTreatment(true);
		}
		if (!isReal) {
			adBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			adBuilder.addTestDevice(getAdmobDeviceId());
		}
		adRequest = adBuilder.build();
		return adRequest;
	}

	/* Rewarded 
	 * ********************************************************************** */

	/**
	 * Load a Rewarded 
	 * @param String id AdMod Rewarded  ID
	 */
	public void loadRewarded(final String id) 
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				rewardedAd = new RewardedAd(activity, id);
				RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() 
				{
		            @Override
		            public void onRewardedAdLoaded() {
						Log.w("godot", "AdMob: onRewardedAdLoaded");
						GodotLib.calldeferred(instance_id, "_on_rewarded_ad_loaded", new Object[] { });
		            }

		           @Override
		            public void onRewardedAdFailedToLoad(int errorCode) {
						Log.w("godot", "AdMob: onRewardedAdFailedToLoad. errorCode: " + errorCode);
						GodotLib.calldeferred(instance_id, "_on_rewarded_ad_failed_to_load", new Object[] { errorCode });
		            }
		        };
				rewardedAd.loadAd(getAdRequest(), adLoadCallback);

			}
		});
	}

	/**
	 * Show a Rewarded 
	 */
	public void showRewarded() {
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (rewardedAd.isLoaded()) 
				{
					RewardedAdCallback adCallback = new RewardedAdCallback() 
					{
		                @Override
		                public void onRewardedAdOpened() {
							Log.w("godot", "AdMob: onRewardedAdOpened");
							GodotLib.calldeferred(instance_id, "_on_rewarded_ad_opened", new Object[] { });
		                }

		                @Override
		                public void onRewardedAdClosed() {
		                	Log.w("godot", "AdMob: onRewardedAdClosed");
							GodotLib.calldeferred(instance_id, "_on_rewarded_ad_closed", new Object[] { });
		                }

		                @Override
		                public void onUserEarnedReward(RewardItem reward) {
							Log.w("godot", "AdMob: " + String.format(" onRewarded! currency: %s amount: %d", reward.getType(), reward.getAmount()));
							GodotLib.calldeferred(instance_id, "_on_rewarded", new Object[] { reward.getType(), reward.getAmount() });
		                }

		                @Override
		                public void onRewardedAdFailedToShow(int errorCode) {
		                }
		            };
	            rewardedAd.show(activity, adCallback);
 
				}
			}
		});
	}


	/* Banner
	 * ********************************************************************** */

	/**
	 * Load a banner
	 * @param String id AdMod Banner ID
	 * @param boolean isOnTop To made the banner top or bottom
	 */
	public void loadBanner(final String id, final boolean isOnTop)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				layout = ((Godot) activity).layout;
				adParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT
				);
				if(isOnTop) adParams.gravity = Gravity.TOP;
				else adParams.gravity = Gravity.BOTTOM;
				
				if (adView != null)
				{
					layout.removeView(adView); // Remove the old view
				}

				adView = new AdView(activity);
				adView.setAdUnitId(id);

				adView.setBackgroundColor(Color.TRANSPARENT);

				adView.setAdSize(AdSize.SMART_BANNER);
				adView.setAdListener(new AdListener()
				{
					@Override
					public void onAdLoaded() {
						Log.w("godot", "AdMob: onAdLoaded");
						GodotLib.calldeferred(instance_id, "_on_admob_ad_loaded", new Object[]{ });
					}

					@Override
					public void onAdFailedToLoad(int errorCode)
					{
						String	str;
						String callbackFunctionName = "_on_admob_banner_failed_to_load";
						switch(errorCode) {
							case AdRequest.ERROR_CODE_INTERNAL_ERROR:
								str	= "ERROR_CODE_INTERNAL_ERROR";
								break;
							case AdRequest.ERROR_CODE_INVALID_REQUEST:
								str	= "ERROR_CODE_INVALID_REQUEST";
								break;
							case AdRequest.ERROR_CODE_NETWORK_ERROR:
								str	= "ERROR_CODE_NETWORK_ERROR";
								callbackFunctionName = "_on_admob_network_error";
								break;								
							case AdRequest.ERROR_CODE_NO_FILL:
								str	= "ERROR_CODE_NO_FILL";
								break;
							default:
								str	= "Code: " + errorCode;
								break;
						}
						Log.w("godot", "AdMob: onAdFailedToLoad -> " + str);
						Log.w("godot", "AdMob: callbackfunction -> " + callbackFunctionName);
						
						GodotLib.calldeferred(instance_id, callbackFunctionName, new Object[]{ });
					}
				});
				layout.addView(adView, adParams);

				// Request
				adView.loadAd(getAdRequest());
			}
		});
	}

	/**
	 * Show the banner
	 */
	public void showBanner()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (adView.getVisibility() == View.VISIBLE) return;
				adView.setVisibility(View.VISIBLE);
				adView.resume();
				Log.d("godot", "AdMob: Show Banner");
			}
		});
	}

	/**
	 * Resize the banner
	 *
	 */
	public void resize()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (layout == null || adView == null || adParams == null)
				{
					return;
				}

				layout.removeView(adView); // Remove the old view

				// Extract params

				int gravity = adParams.gravity;
				FrameLayout	layout = ((Godot)activity).layout;
				adParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT
				);
				adParams.gravity = gravity;
				AdListener adListener = adView.getAdListener();
				String id = adView.getAdUnitId();

				// Create new view & set old params
				adView = new AdView(activity);
				adView.setAdUnitId(id);
				adView.setBackgroundColor(Color.TRANSPARENT);
				adView.setAdSize(AdSize.SMART_BANNER);
				adView.setAdListener(adListener);

				// Add to layout and load ad
				layout.addView(adView, adParams);

				// Request
				adView.loadAd(getAdRequest());

				Log.d("godot", "AdMob: Banner Resized");
			}
		});
	}




	/**
	 * Hide the banner
	 */
	public void hideBanner()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (adView.getVisibility() == View.GONE) return;
				adView.setVisibility(View.GONE);
				adView.pause();
				Log.d("godot", "AdMob: Hide Banner");
			}
		});
	}

	/**
	 * Get the banner width
	 * @return int Banner width
	 */
	public int getBannerWidth()
	{
		return AdSize.SMART_BANNER.getWidthInPixels(activity);
	}

	/**
	 * Get the banner height
	 * @return int Banner height
	 */
	public int getBannerHeight()
	{
		return AdSize.SMART_BANNER.getHeightInPixels(activity);
	}

	/* Interstitial
	 * ********************************************************************** */

	/**
	 * Load a interstitial
	 * @param String id AdMod Interstitial ID
	 */
	public void loadInterstitial(final String id)
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				interstitialAd = new InterstitialAd(activity);
				interstitialAd.setAdUnitId(id);
		        interstitialAd.setAdListener(new AdListener()
				{
					@Override
					public void onAdLoaded() {
						Log.w("godot", "AdMob: onAdLoaded");
						GodotLib.calldeferred(instance_id, "_on_interstitial_loaded", new Object[] { });
					}

					@Override
					public void onAdFailedToLoad(int errorCode) {
						Log.w("godot", "AdMob: onAdFailedToLoad(int errorCode) - error code: " + Integer.toString(errorCode));
						Log.w("godot", "AdMob: _on_interstitial_not_loaded");
						GodotLib.calldeferred(instance_id, "_on_interstitial_not_loaded", new Object[] { });
					}

					@Override
					public void onAdOpened() {
						Log.w("godot", "AdMob: onAdOpened()");
					}

					@Override
					public void onAdLeftApplication() {
						Log.w("godot", "AdMob: onAdLeftApplication()");
					}

					@Override
					public void onAdClosed() {
						GodotLib.calldeferred(instance_id, "_on_interstitial_close", new Object[] { });
						Log.w("godot", "AdMob: onAdClosed");
					}
				});



				interstitialAd.loadAd(getAdRequest());
			}
		});
	}

	/**
	 * Show the interstitial
	 */
	public void showInterstitial()
	{
		activity.runOnUiThread(new Runnable()
		{
			@Override public void run()
			{
				if (interstitialAd.isLoaded()) {
					interstitialAd.show();
				} else {
					Log.w("w", "AdMob: showInterstitial - interstitial not loaded");
				}
			}
		});
	}

	/* Utils
	 * ********************************************************************** */

	/**
	 * Generate MD5 for the deviceID
	 * @param String s The string to generate de MD5
	 * @return String The MD5 generated
	 */
	private String md5(final String s)
	{
		try {
			// Create MD5 Hash
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2) h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch(NoSuchAlgorithmException e) {
			//Logger.logStackTrace(TAG,e);
		}
		return "";
	}

	/**
	 * Get the Device ID for AdMob
	 * @return String Device ID
	 */
	private String getAdmobDeviceId()
	{
		String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		String deviceId = md5(android_id).toUpperCase(Locale.US);
		return deviceId;
	}

	/* Definitions
	 * ********************************************************************** */

	/**
	 * Initilization Singleton
	 * @param Activity The main activity
	 */
 	static public Godot.SingletonBase initialize(Activity activity)
 	{
 		return new GodotAdMob(activity);
 	}

	/**
	 * Constructor
	 * @param Activity Main activity
	 */
	public GodotAdMob(Activity p_activity) {
		registerClass("AdMob", new String[] {
			"init",
			"initWithContentRating",
			// banner
			"loadBanner", "showBanner", "hideBanner", "getBannerWidth", "getBannerHeight", "resize",
			// Interstitial
			"loadInterstitial", "showInterstitial",
			// Rewarded 
			"loadRewarded", "showRewarded"
		});
		activity = p_activity;
	}
}