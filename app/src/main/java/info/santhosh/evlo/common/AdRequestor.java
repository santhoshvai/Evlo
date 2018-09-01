package info.santhosh.evlo.common;

import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.net.MalformedURLException;
import java.net.URL;

import info.santhosh.evlo.BuildConfig;

public class AdRequestor {
    private AdView adView;
    private ConsentInformation consentInformation;
    private static final String TAG = "AdRequestor";
    ConsentForm form;

    public AdRequestor(AdView adView, AdListener adListener) {
        this.adView = adView;
        adView.setAdListener(adListener);
        consentInformation = ConsentInformation.getInstance(adView.getContext());
    }

    public void start() {
        String[] publisherIds = {BuildConfig.AD_PUB_ID};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
               processConsent(consentStatus);
            }

            @Override
            public void onFailedToUpdateConsentInfo(String reason) {
                Log.e(TAG, "failedToUpdateConsentInfo due to " + reason);
            }
        });
    }

    private void processConsent(ConsentStatus consentStatus) {
        boolean isEU = ConsentInformation.getInstance(adView.getContext()).isRequestLocationInEeaOrUnknown();
        if (!isEU) {
            startAds(ConsentStatus.PERSONALIZED);
        } else {
            if (consentStatus == ConsentStatus.UNKNOWN) showConsentForm();
            else {
                startAds(consentStatus);
            }
        }
    }

    public void showConsentForm() {
        URL privacyUrl;
        try {
            privacyUrl = new URL("https://www.santhosh.info/Evlo/privacy_policy.html");
        } catch (MalformedURLException e) {
            Crashlytics.logException(e);
            return;
        }
        ConsentFormListener consentFormListener = new ConsentFormListener() {
            @Override
            public void onConsentFormLoaded() {
                form.show();
            }

            @Override
            public void onConsentFormOpened() {
                // Consent form was displayed.
            }

            @Override
            public void onConsentFormClosed(
                    ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                if (!userPrefersAdFree) {
                    ConsentInformation.getInstance(adView.getContext())
                            .setConsentStatus(consentStatus);
                    startAds(consentStatus);
                }
            }

            @Override
            public void onConsentFormError(String errorDescription) {
                // Consent form error.
            }
        };
        form = new ConsentForm.Builder(adView.getContext(), privacyUrl)
                .withListener(consentFormListener)
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
//                .withAdFreeOption()
                .build();
        form.load();
    }

    private void startAds(ConsentStatus consentStatus) {
        if (adView != null) {
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
            boolean canServePersonalisedAds = consentStatus == ConsentStatus.PERSONALIZED;
            if (!canServePersonalisedAds) {
                Bundle extras = new Bundle();
                extras.putString("npa", "1");
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
            }
            adView.loadAd(adRequestBuilder.build());
        }
    }
}
