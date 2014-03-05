package org.theboar.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

public class NewsStore implements INewsStore {

	public NewsStore(){
		//
	}
	
	public HeadlineList getStubHeadlines(){
		File DataStorage = new File(
				Environment.getExternalStorageDirectory(),"Boar News");
		if (!DataStorage.exists()) {
			if (!DataStorage.mkdirs())
				Log.d("Print","failed to create directory");
		} else Log.d("Print","Directory Exists");

		File[] files = DataStorage.listFiles();

//		for (int i = 0; i < files.length; i++) Log.d("Print","" + files[i].getName());
		//-------------------------Start Searching and adding image files----------------------------
		//-------------------------MAKE decoding ASYNCTASK----------------------------
		
		HeadlineList headlines = new HeadlineList(files.length);
		
		for (int i = 0; i < files.length; i++) {
			// Load image
			String fileName = files[i].getName();
			Drawable d = null;
			try {
				FileInputStream fis = new FileInputStream(DataStorage
						+ File.separator + fileName);
				if (fileName.endsWith(".jpg")) {
					d = Drawable.createFromStream(fis,"news");
				}
			}
			catch (Exception e) {
				Log.d("Print","ERROR ERROR");
			}
			// Create new headline
			Headline headline = new Headline();
			headline.setHeadlineTitle(files[i].getName());
			headline.setLowResImage(d);
			// Add headline to list
			headlines.addHeadline(headline);
		}
		return headlines;
		
	}
	
	@Override
	public int requestRefresh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean storeHeadline(IHeadline newHeadline) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IHeadlineList getHeadlines(Date dateFrom, Date dateTo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getHeadlines(int lastN) {
		HeadlineList list = new HeadlineList(lastN);
		//RequestTask1 rt = new RequestTask1(list);
		//rt.execute("http://www.theboar.org/?json=1");
		
		// Load synchronously
		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost("http://theboar.org/?json=1");
		// Depends on your web service
		httppost.setHeader("Content-type", "application/json");

		InputStream inputStream = null;
		String result = null;
		try {
		    HttpResponse response = httpclient.execute(httppost);           
		    HttpEntity entity = response.getEntity();

		    inputStream = entity.getContent();
		    // json is UTF-8 by default
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		    StringBuilder sb = new StringBuilder();

		    String line = null;
		    while ((line = reader.readLine()) != null)
		    {
		        sb.append(line + "\n");
		    }
		    result = sb.toString();
		} catch (Exception e) { 
		    // Oops
		}
		finally {
		    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
		}
		
		// Now parse JSON
		//Create JSON Object
		if(result != null){
	    	try {
				JSONObject jObject = new JSONObject(result);
				JSONArray jArray = jObject.getJSONArray("posts");
				for(int i = 0; i < jArray.length(); i++){
					JSONObject story = jArray.getJSONObject(i);
					Headline head = new Headline();
					String storyTitle = story.getString("title");
					// Replace HTML codes with correct characters
					// @TODO: More comprehensive code to do this
					storyTitle = storyTitle.replace("&#8217;", "�");
					storyTitle = storyTitle.replace("&#8216;", "�");
					storyTitle = storyTitle.replace("&#8218;", "�");
					storyTitle = storyTitle.replace("&#8220;", "�");
					storyTitle = storyTitle.replace("&#8221;", "�");
					storyTitle = storyTitle.replace("&#8222;", "�");
					head.setHeadlineTitle(storyTitle);
					//String imageURL = story.getJSONArray("attachments").getJSONObject(0).getJSONObject("images").getJSONObject("full").getString("url");
					String imageURL = story.getJSONObject("thumbnail_images").getJSONObject("medium").getString("url");
					
					head.setLowResImage(getSmartDrawableFromUrl(imageURL));
					
					//String imageURL = "http://theboar.org/wp-content/uploads/2014/02/Dating.jpg";
					//
					String dateTimeString = story.getString("date");
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss", Locale.ENGLISH);
					Date datePublished = df.parse(dateTimeString);
					head.setDatePublished(datePublished);
					head.setAuthor(story.getJSONObject("author").getString("name"));
					Log.v(this.toString(), "STORY: " + story.getString("title"));
					Log.v(this.toString(), "IMG URL: " + imageURL);
					head.setPageUrl(story.getString("url"));
					list.addHeadline(head);
				}
				list.setDoneLoading(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return list;
		//return list;
	}

	@Override
	public IHeadlineList getFavourites(int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getNew(int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList getHeadlines(String category, int lastN) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IHeadlineList basicSearch(String query, int lastN) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static Drawable drawableFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return new BitmapDrawable(x);
    }
	
	public static Bitmap bitmapFromUrl(String url) throws IOException {
        Bitmap x;

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();

        x = BitmapFactory.decodeStream(input);
        return x;
    }
	
	public Drawable getSmartDrawableFromUrl(String url){
		Drawable dr = null;
		String sdState = Environment.getExternalStorageState();
		if( sdState.equalsIgnoreCase(Environment.MEDIA_MOUNTED)){
			// Now check local storage for pre-cached images
			File DataStorage = new File(
					Environment.getExternalStorageDirectory(),"WarwickBoar/images/url-cache");
			if (!DataStorage.exists()) {
				if (!DataStorage.mkdirs())
					Log.d("Print","failed to create directory");
			} else Log.d("Print","Directory Exists");

			String fileName = Uri.parse(url).getLastPathSegment();
			Log.v(this.toString(), "FILE NAME:: " + fileName);
			File[] files = DataStorage.listFiles();
			
			for(int i = 0; i < files.length; i++){
				// Image is not in SD card cache
				if(fileName.equalsIgnoreCase(files[i].getName())){
					// Now use the image that was cached or just downloaded to create a drawable
					FileInputStream fis;
					try {
						Log.i(this.toString(),"LOADING " + fileName + "FROM FILE...");
						fis = new FileInputStream(DataStorage
								+ File.separator + files[i].getName());
						dr = Drawable.createFromStream(fis,"news2");
						return dr;
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			// Then download the image
			downloadFile(url,fileName,"/WarwickBoar/images/url-cache/");
		}
		
		// Use old method in case of pathetic failure or SD card being unavailable
		try {
			return drawableFromUrl(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dr;
	}
	
	public void downloadFile(String fileURL, String fileName, String directory) {
        try {
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + directory);
            dir.mkdirs();
            File file = new File(dir, fileName);

            FileOutputStream f = new FileOutputStream(file);

            InputStream in = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (Exception e) {
            Log.d("Downloader", e.getMessage());
        }

    }

}