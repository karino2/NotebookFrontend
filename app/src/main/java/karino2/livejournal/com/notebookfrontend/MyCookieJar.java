package karino2.livejournal.com.notebookfrontend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by _ on 2017/12/16.
 */
public class MyCookieJar implements CookieJar {

    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
    }

    public String getXSRFVal(HttpUrl url) {
        List<Cookie> cookies = loadForRequest(url);
        for (Cookie cookie : cookies) {
            if (cookie.name().equals("_xsrf")) {
                return cookie.value();
            }
        }
        return "";
    }
}
