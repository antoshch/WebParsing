package com.example.worker.webparsing.descriptionAxtivityWebwiev;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.worker.webparsing.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

public class DescriptionActivity extends Activity {

    ProgressDialog mProgressDialog;
    private WebChromeClient.CustomViewCallback mFullscreenViewCallback;
    private FrameLayout mFullScreenContainer;
    private View mFullScreenView;
    private WebView mWebView;

    private static final int SUCCESS = 1;
    private static final int NETWORK_ERROR = 2;
    private static final int ERROR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        mWebView = (WebView) findViewById(R.id.web_view);
        mFullScreenContainer = (FrameLayout) findViewById(R.id.fullscreen_container);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(mWebChromeClient);

        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setPadding(0, 0, 0, 0);

        WebSettings settings = mWebView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        new getHtml().execute();
    }

    private class getHtml extends AsyncTask<String, Void, Integer> {
        Elements tfa;
        Element title;
        String titles;
        String cop;
        Elements comments;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(DescriptionActivity.this);
            mProgressDialog.setMessage("Загрузка...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {

                Uri urls = getIntent().getData();
                String url = urls.toString();

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
                        .cookie("auth", "token")
                        .timeout(10000)
                        .get();

                title = doc.select(".c1-post h2").first();
                titles=title.toString().replace("img", "imgs");

                try {
                    Elements link = doc.select(".imgleft img[src]");
                    String imageUrl = link.attr("src");
                    imageUrl = "http://fratria.ru/download" + imageUrl.substring(imageUrl.indexOf("download") + 8);

                    tfa = link.attr("src", imageUrl);
                } catch (Exception e) {
                e.printStackTrace();
            }
                tfa = doc.select(".c1-post-data");
                cop = tfa.toString().replaceFirst("<br>©", "©").replace("style", "");
                comments = doc.select(".comments").select(".comments-heading");

                return SUCCESS;
            } catch (UnknownHostException e) {
                Log.e("Unknown Host Exception", "Network error", e);
                return NETWORK_ERROR;
            } catch (IOException e) {
                Log.e("IO Exception", "Failed to load HTML", e);
                return ERROR;
            } catch (Exception e) {
                Log.e("Exception", "An exception occured", e);
                return ERROR;
            }
        }

        private String populateHTML(int resourceID) {
            String html;
            html = readTextFromResource(resourceID);
            html = html.replace("_TITLE_", titles);
            html = html.replace("_CONTENT_", cop);
            return html;
        }

        private String readTextFromResource(int resourceID) {
            InputStream raw = getResources().openRawResource(resourceID);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int i;
            try {
                i = raw.read();
                while (i != -1) {
                    stream.write(i);
                    i = raw.read();
                }
                raw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stream.toString();
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 2) {
                Toast.makeText(
                        getApplicationContext(),
                        "Ошибка сетевого подключения. Проверьте соединение с интернетом и попробуйте снова.",
                        Toast.LENGTH_LONG).show();
            } else if (result == 3) {
                Toast.makeText(getApplicationContext(),
                        "Не известная ошибка. Загрузка прервана.",
                        Toast.LENGTH_LONG).show();
            } else if (result == 1) {

                mWebView.loadDataWithBaseURL("http://fratria.ru/","<!DOCTYPE HTML>"
                        + populateHTML(R.raw.html), "text/html", "en_US", null);
            }

            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.loadUrl("about:blank");
        mWebView.stopLoading();
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.destroy();
        mWebView = null;
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        @SuppressWarnings("deprecation")
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mFullScreenView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mFullScreenView = view;
            mWebView.setVisibility(View.GONE);

            mFullScreenContainer.setVisibility(View.VISIBLE);
            mFullScreenContainer.addView(view);
            mFullscreenViewCallback = callback;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mFullScreenView == null) {
                return;
            }
            mWebView.setVisibility(View.VISIBLE);

            mFullScreenContainer.setVisibility(View.GONE);

            mFullScreenContainer.removeView(mFullScreenView);
            mFullscreenViewCallback.onCustomViewHidden();
            mFullScreenView = null;
        }
    };

    @Override
    public void onBackPressed(){
        if (mFullScreenView != null){
            mWebChromeClient.onHideCustomView();
        }else{
            finish();
        }
    }

}