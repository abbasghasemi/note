package ghasemi.abbas.note;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RecommendedApps {
    public static final class App {
        public final String title, icon, url, badge, badgeColor;

        App(String title, String icon, String url, String badge, String badgeColor) {
            this.title = title;
            this.icon = icon;
            this.url = url;
            this.badge = badge;
            this.badgeColor = badgeColor;
        }
    }

    public interface Callback {
        void onLoaded(List<App> apps);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private RecommendedApps() { }

    public static void load(Context context, Callback callback) {
        Context app = context.getApplicationContext();
        SharedPreferences cache = app.getSharedPreferences("recommended_apps_" + BuildConfig.FLAVOR, Context.MODE_PRIVATE);
        String saved = cache.getString("json", null);
        if (saved != null && cache.getLong("expiry", 0) > System.currentTimeMillis()) {
            callback.onLoaded(parse(saved));
            return;
        }
        EXECUTOR.execute(() -> {
            List<App> apps = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                String endpoint = "https://farasource.ir/products/v0/" + app.getPackageName()
                        + "/" + BuildConfig.FLAVOR + "/apps.json";
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder body = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            body.append(line);
                            if (body.length() > 262144) throw new IllegalStateException("Feed too large");
                        }   
                    }
                    JSONObject response = new JSONObject(body.toString());
                    long expiry = response.getLong("expiresAt");
                    if (expiry > System.currentTimeMillis()) {
                        apps = parse(body.toString());
                        cache.edit().putString("json", body.toString()).putLong("expiry", expiry).apply();
                    }
                }
            } catch (Exception ignored) {
                // The optional feed must not block the notes screen.
            } finally {
                if (connection != null) connection.disconnect();
            }
            List<App> result = apps;
            MAIN.post(() -> callback.onLoaded(result));
        });
    }

    private static List<App> parse(String json) {
        List<App> apps = new ArrayList<>();
        try {
            JSONArray items = new JSONObject(json).getJSONArray("apps");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String title = item.optString("title").trim();
                String url = item.optString("url").trim();
                if (title.isEmpty() || !url.startsWith("https://")) continue;
                String badge = item.optString("badge", "").trim();
                apps.add(new App(title, item.optString("icon", ""), url,
                        badge.isEmpty() || "null".equals(badge) ? null : badge,
                        item.optString("badgeColor", "")));
            }
        } catch (Exception ignored) { }
        return apps;
    }
}
