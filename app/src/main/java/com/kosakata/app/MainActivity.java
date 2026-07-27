package com.kosakata.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private static final int REQ_AUDIO = 41;
    private BillingClient billingClient;
    private ProductDetails premiumDetails;
    private static final String PREMIUM_SKU = "premium_unlock";


    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new SpeechBridge(), "AndroidSpeech");
        webView.addJavascriptInterface(new BillingBridge(), "AndroidBilling");
        webView.loadUrl("file:///android_asset/www/index.html");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("id", "ID"));
                tts.setSpeechRate(0.9f);
                ttsReady = true;
            }
        });
        billingClient = BillingClient.newBuilder(this)
                .setListener((billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        handlePurchases(purchases);
                    }
                })
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
        connectBilling();

    }

    class SpeechBridge {
        @JavascriptInterface
        public void speak(final String text) {
            if (ttsReady && text != null) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kosakata-tts");
            }
        }

        @JavascriptInterface
        public void start() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 23 &&
                        checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
                    return;
                }
                startListening();
            });
        }
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            jsEnd();
            return;
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onResults(Bundle results) {
                    ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (list != null && !list.isEmpty()) {
                        js("onSpeechResult(" + JSONObject.quote(list.get(0)) + ")");
                    } else {
                        jsEnd();
                    }
                }
                @Override public void onError(int error) { jsEnd(); }
                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizer.startListening(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                jsEnd();
            }
        }
    }

    private void js(final String code) {
        runOnUiThread(() -> webView.evaluateJavascript(code, null));
    }

    private void jsEnd() {
        js("onSpeechEnd()");
    }


    private void connectBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryPremiumState();
                    queryPremiumDetails();
                }
            }
            @Override public void onBillingServiceDisconnected() { }
        });
    }

    private void queryPremiumState() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, purchases) -> {
                    boolean owned = false;
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase p : purchases) {
                            if (p.getProducts().contains(PREMIUM_SKU)
                                    && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                owned = true;
                                if (!p.isAcknowledged()) acknowledge(p);
                            }
                        }
                    }
                    js("onPremiumState(" + owned + ")");
                });
    }

    private void queryPremiumDetails() {
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(java.util.Collections.singletonList(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(PREMIUM_SKU)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()))
                .build();
        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !list.isEmpty()) {
                premiumDetails = list.get(0);
                ProductDetails.OneTimePurchaseOfferDetails offer = premiumDetails.getOneTimePurchaseOfferDetails();
                if (offer != null) {
                    js("onPremiumPrice(" + org.json.JSONObject.quote(offer.getFormattedPrice()) + ")");
                }
            }
        });
    }

    private void handlePurchases(java.util.List<Purchase> purchases) {
        for (Purchase p : purchases) {
            if (p.getProducts().contains(PREMIUM_SKU)
                    && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!p.isAcknowledged()) acknowledge(p);
                js("onPremiumState(true)");
            }
        }
    }

    private void acknowledge(Purchase p) {
        billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.getPurchaseToken()).build(),
                billingResult -> { });
    }

    class BillingBridge {
        @JavascriptInterface
        public void getState() {
            runOnUiThread(() -> {
                if (billingClient != null && billingClient.isReady()) queryPremiumState();
                else connectBilling();
            });
        }

        @JavascriptInterface
        public void buy() {
            runOnUiThread(() -> {
                if (premiumDetails == null || billingClient == null || !billingClient.isReady()) {
                    js("onPremiumError()");
                    return;
                }
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(java.util.Collections.singletonList(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(premiumDetails)
                                        .build()))
                        .build();
                billingClient.launchBillingFlow(MainActivity.this, flowParams);
            });
        }

        @JavascriptInterface
        public void restore() {
            runOnUiThread(() -> {
                if (billingClient != null && billingClient.isReady()) queryPremiumState();
                else connectBilling();
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (billingClient != null) billingClient.endConnection();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
