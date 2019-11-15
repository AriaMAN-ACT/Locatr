package ir.ariact.locatr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

public class SearchTask extends AsyncTask<Location, Void, Void> {
    private ImageView imageView;
    GalleryItem item;
    Bitmap bitmap;

    public SearchTask(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Void doInBackground(Location... locations) {
        FlickrFetcher flickrFetcher = new FlickrFetcher();
        List<GalleryItem> itemList = flickrFetcher.searchPhoto(locations[0]);
        if (itemList.size() == 0)
            return null;
        item = itemList.get(0);
        try {
            byte[] bytes = flickrFetcher.getUrlBytes(item.getUrl());
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        imageView.setImageBitmap(bitmap);
    }
}
