package org.itishka.pointim.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.itishka.pointim.BuildConfig;
import org.itishka.pointim.R;
import org.itishka.pointim.model.imgur.Token;
import org.itishka.pointim.network.ImgurConnectionManager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class ImgurAuthFragment extends Fragment {

    WebView mWebView;

    public ImgurAuthFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View newView = inflater.inflate(R.layout.fragment_imgur_auth, container, false);
        mWebView = (WebView) newView.findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("My Webview", url);
                Uri uri = Uri.parse(url);
                if (url.startsWith(BuildConfig.IMGUR_REDIRECT_URL + "?")) {
                    processCode(uri.getQueryParameter("code"));
                    view.loadData("", "text/html", "UTF-8");
                    return true;
                }
                return !"api.imgur.com".equals(uri.getHost());
            }
        });
        load();
        return newView;
    }

    private void load() {
        Uri auth = new Uri.Builder()
                .scheme("https")
                .authority("api.imgur.com")
                .path("/oauth2/authorize")
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", BuildConfig.IMGUR_ID)
                .build();
        mWebView.loadUrl(auth.toString());
    }

    private void processCode(String code) {
        ImgurConnectionManager.getInstance().imgurAuthService.getToken(
                BuildConfig.IMGUR_ID,
                BuildConfig.IMGUR_SECRET,
                "authorization_code", code,
                new Callback<Token>() {
                    @Override
                    public void success(Token token, Response response) {
                        if (token.access_token != null) {
                            Toast.makeText(getActivity(), getString(R.string.toast_authorized), Toast.LENGTH_SHORT).show();
                            ImgurConnectionManager.getInstance().updateAuthorization(getActivity(), token);
                            getActivity().finish();
                        } else {
                            if (!isDetached()) {
                                Toast.makeText(getActivity(), getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                load();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (!isDetached()) {
                            Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
                            load();
                        }
                    }
                });
    }
}
