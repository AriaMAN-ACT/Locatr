package ir.ariact.locatr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetcher {

    public static final String TAG = "FlickrFetcher";
    public static final String FLICKR_REST_PATH = "https://www.flickr.com/services/rest";
    public static final String API_KEY = "79b5c28546b0c0fd5a0bdc65ac9eab18";
    public static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    public static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri.parse(FLICKR_REST_PATH)
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
//            .appendQueryParameter("user_id", "34427466731@N01")
            .build();

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();

        try {
            Log.d(TAG, "url: " + url);
            String jsonString = getUrlString(url);
            Log.d(TAG, jsonString);
            parseItems(items, jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "failed to fetch", e);
        }

        return items;
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhoto(Location location){
        return downloadGalleryItems(buildUrl(location));
    }


    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT
                .buildUpon()
                .appendQueryParameter("method", method);

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }
    private String buildUrl(Location location){
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method",SEARCH_METHOD)
                .appendQueryParameter("lat","" + location.getLatitude())
                .appendQueryParameter("lon" , ""+location.getLongitude())
                .build().toString();
    }



    private void parseItems(List<GalleryItem> items, String jsonString) throws JSONException {
        JSONObject jsonBody = new JSONObject(jsonString);
        JSONObject photosObject = jsonBody.getJSONObject("photos");

        JSONArray photoArray = photosObject.getJSONArray("photo");
        for (int i = 0; i < photoArray.length(); i++) {
            JSONObject photoObject = photoArray.getJSONObject(i);

            if (!photoObject.has("url_s"))
                continue;

            String id = photoObject.getString("id");
            String title = photoObject.getString("title");
            String url = photoObject.getString("url_s");

            GalleryItem galleryItem = new GalleryItem(id, title, url);
            items.add(galleryItem);
        }
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " with url: " + urlSpec);
            }

            int length = 2048;
            int bytesRead = 0;
            byte[] buffer = new byte[length];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } finally {
            outputStream.close();
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        String result = new String(getUrlBytes(urlSpec));
        return result;
    }

    public Bitmap getUrlBitmap(String urlSpec) throws IOException {
        byte[] bytes = getUrlBytes(urlSpec);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
