package eu.trentorise.smartcampus.dt.custom.data;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

	private String url;
	private String user;
	private String password;
	private ImageView imageView = null;

	public ImageDownloaderTask(ImageView imageView) {
		this.imageView = imageView;
	}

	public void setCredentials(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		url = urls[0];

		Bitmap bm = DTHelper.eventsImagesCache.get(url);
		if (bm == null) {
			bm = downloadImage(url);
			if (bm != null) {
				DTHelper.eventsImagesCache.put(url, bm);
			}
		}

		return bm;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null) {
			imageView.setImageBitmap(result);
		}
	}

	private Bitmap downloadImage(String url) {
		Bitmap bm = null;

		if (url == null || url.length() <= 0) {
			return bm;
		}

		try {
			DefaultHttpClient client = new DefaultHttpClient();

			if (this.user != null && this.user.length() > 0 && this.password != null && this.password.length() > 0) {
				client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
						new UsernamePasswordCredentials(user, password));
			}

			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			InputStream bis = entity.getContent();

			bm = BitmapFactory.decodeStream(bis);

			// bis.close();
			// is.close();
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "Error getting the image from server: " + e.getMessage().toString());
		}

		return bm;
	}
}