package org.github.kshashov.navback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class OsmService {
    private static String token = null;
    private static HttpClient httpClient;

    @SneakyThrows
    @Autowired
    public OsmService() {
        updateHttpClient();
    }

    private void updateHttpClient() throws URISyntaxException {
        CookieManager cookieHandler = new CookieManager();
        HttpCookie cookie = new HttpCookie("_osm_totp_token", token);
        cookie.setPath("/");
        cookie.setVersion(0);
        cookie.setDomain("https://render.openstreetmap.org");
        cookieHandler.getCookieStore().add(new URI("https://render.openstreetmap.org"), cookie);
        this.httpClient = HttpClient.newBuilder().cookieHandler(cookieHandler).build();
    }

//    public String getToken() {
//        if (token == null) {
//            refreshToken();
//        }
//        return token;
//    }

    @SneakyThrows
    public byte[] getTile(Window coordinates, Integer scale) {
        URI uri = new URI("https://render.openstreetmap.org/cgi-bin/export?bbox=" + coordinates.left + "," + coordinates.bottom + "," + coordinates.right + "," + coordinates.top + "&scale=" + scale + "&format=png");
        HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofMinutes(5))
                .build(), HttpResponse.BodyHandlers.ofByteArray());
        // TODO check for error and token
        if (response.statusCode() == 400) {
            refreshToken();
            return getTile(coordinates, scale);
        }

        return response.body();
    }

    @SneakyThrows
    private void refreshToken() {
        URI uri = new URI("https://www.openstreetmap.org/#map=8/41.8368/104.5450&layers=N");
        HttpResponse<InputStream> response = httpClient.send(HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofMinutes(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        List<String> headers = response.headers().map().get("Set-Cookie");
        for (String header: headers) {
            String[] pairs = header.split(";");
            for (String pair: pairs) {
                String[] items = pair.split("=");
                if (items[0].equals("_osm_totp_token")) {
                    token = items[1];
                }
            }
        }

        updateHttpClient();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class Window {
        @NonNull
        Double top;
        @NonNull
        Double left;
        @NonNull
        Double bottom;
        @NonNull
        Double right;
    }
}
