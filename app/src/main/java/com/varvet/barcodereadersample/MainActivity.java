package com.varvet.barcodereadersample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.varvet.barcodereadersample.barcode.BarcodeCaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    WebView resultView;
    RequestQueue requestQueue;
 //   ImageView imageView;
 //   VideoView videoView;
    Button scan_barcode_button;
    TextView result_textview;

    public static int BARCODE_READER_REQUEST_CODE = 2;
//    private static int BARCODE_READER_REQUEST_CODE = 1;
    private final String BASE_URL = "http://192.168.1.251:4000/interface/";
    private String results = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (WebView) findViewById(R.id.resultView);
        result_textview = (TextView) findViewById(R.id.result_textview);
        Button scanButton = (Button) findViewById(R.id.scan_barcode_button);
 //       imageView = (ImageView) findViewById(R.id.imageView);
 //       videoView = (VideoView) findViewById(R.id.videoView);
        scan_barcode_button = (Button) findViewById(R.id.scan_barcode_button);


        //requestQueue = Volley.newRequestQueue(getApplicationContext());
        //When clicking on this button, the GET method is shown
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }

        });

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //       val p = barcode.cornerPoints
                    result_textview.setText(barcode.displayValue.toString());
                    VolleyLog.d("Resultview", "Status: " + barcode.displayValue.toString());
                    results = barcode.displayValue.toString();

                    // POST METHOD
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                    //results = "Jani Arvas";

                    HurlStack hurlStack = new HurlStack() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        protected HttpURLConnection createConnection(URL url) throws IOException {
                            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                            try {
                                /*httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                                httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                                */
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return httpsURLConnection;
                        }
                    };



                    // Disable SSL certificate checking
                    //disableSSLVerification();
                    //final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
                    //requestQueue.add(request);
                    // Instantiate the cache
/*                    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

                    // Set up the network to use HttpURLConnection as the HTTP client.
                    Network network = new BasicNetwork(new HurlStack());

                    // Instantiate the RequestQueue with the cache and network.
                    requestQueue = new RequestQueue(cache, network);
*/

                  JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, addJsonParams(results),
                          new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            scan_barcode_button.setVisibility(View.INVISIBLE);
                            //result_textview.setVisibility(View.INVISIBLE);
                                 deleteCache(getApplicationContext());
                                 parseResponseAllData(response);
                            //Log.d("onResponse", response.toString());
                            //Toast.makeText(VolleyMethods.this, response.toString(), Toast.LENGTH_LONG).show(); // Test
                            //result_textview.setText("");
                            //result_textview.setText(response.toString());
                            //parseResponseImage(response);
                            //parseResponseVideo(response);
                            //parseResponseAllData(response);
                        }
                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    VolleyLog.d("onErrorResponse", "Error: " + error.getMessage());
                                    result_textview.setText("");
                                    result_textview.setText(error.getMessage());
                                    //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                                }
                            }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            return headers;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);
                } //else
                    //resultView.setText(R.string.no_barcode_captured);
            }
            // Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
            //         CommonStatusCodes.getStatusCodeString(resultCode)))
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception ignored) {}
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }

    public JSONObject addJsonParams(String sUser) {
        JSONObject jsonobject = new JSONObject();
        try {
            Log.d("addJsonParams", "addJsonParams");
            //jsonobject.put("id", "");
            jsonobject.put("name", sUser); // sUserId
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobject;
    }

/*    public void parseResponseImage(JSONObject response) {
        Boolean bIsSuccess = false; // Write according to your logic this is demo.
        JSONObject json=null;
        try{
             json = new JSONObject(response.toString());
             //resultView.setText(json.toString());
             resultView.loadDataWithBaseURL(null, json.toString(), "text/html", "utf-8", null);
                //JSONObject jsonObject = new JSONObject(response.toString());
            //bIsSuccess = jsonObject.getBoolean("success");

            Object result = json.get("data");
            String image = result.toString();
            Bitmap myBitmap = this.ConvertToImage(image);
            //Bitmap decodedByte = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(myBitmap);
            imageView.setVisibility(View.VISIBLE);
            //resultView.setText(result.toString());
        } catch (JSONException e) {
                e.printStackTrace();
        }
            //Toast.makeText(VolleyMethods.this, "" + e.toString(), Toast.LENGTH_LONG).show(); // Test
    }

 */
    public Bitmap ConvertToImage(String image){
        try{
            InputStream stream = new ByteArrayInputStream(Base64.decode(image.getBytes(), Base64.DEFAULT));
            return BitmapFactory.decodeStream(stream);
        }
        catch (Exception e) {
            return null;
        }
    }
    public void parseResponseVideo(JSONObject response) {
        Boolean bIsSuccess = false; // Write according to your logic this is demo.
        JSONObject json=null;
        try{
            json = new JSONObject(response.toString());
            Object result = json.get("data");
            String image = result.toString();
            String name = null;
            byte[] decodedBytes = Base64.decode(image.getBytes(), Base64.DEFAULT);
            try {
                File newfile;
                File filepath = new File(getApplicationContext().getFilesDir(), "BarcodeReaderSample" + File.separator + "videos");
                File dir = new File(filepath.getAbsolutePath()  + "/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                newfile = new File(dir, "save_" + System.currentTimeMillis() + ".mp4");
                if (!newfile.exists()){
                    newfile.createNewFile();
                }
                OutputStream out = new FileOutputStream(newfile);
                name = newfile.toString();
                out.write(decodedBytes);
                out.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            VideoView videoHolder = new VideoView(this);
            videoHolder.setMediaController(new MediaController(this));
            //videoView = (VideoView) findViewById(R.id.videoView);
            //String UrlPath="android.resource://"+getPackageName()+"/raw/" + name;
            //videoHolder.setVideoURI(Uri.parse("/sdcard/convertVideo.avi"));
            videoHolder.setVideoURI(Uri.parse(name));
            setContentView(videoHolder);
            videoHolder.start();
            //resultView.setText(result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Toast.makeText(VolleyMethods.this, "" + e.toString(), Toast.LENGTH_LONG).show(); // Test
    }

    public void parseResponseAllData(JSONObject response) {

        JSONObject jsonName = null;
        JSONObject jsonText = null;
        JSONObject jsonExplanation = null;
        JSONObject jsonVideo=null;
        JSONObject jsonImage=null;
        //result_textview.setText("parse response all data!");

        try{
            jsonName = new JSONObject((response.toString()));
            Object resultName = jsonName.get("name");
            jsonText = new JSONObject((response.toString()));
            Object resultText = jsonText.get("text");
            jsonExplanation = new JSONObject((response.toString()));
            Object resultExplanation = jsonExplanation.get("explanation");
            resultView.setBackgroundColor(Color.TRANSPARENT);
            //result_textview.setText("Person:" + resultName.toString() + "\n" + "Text:" + resultText.toString() + "\n" + "Website:" + resultWebsite.toString());

            // Parse Image
            jsonImage = new JSONObject(response.toString());
            Object resultImage = jsonImage.get("image");
            String image = resultImage.toString();
            Bitmap myBitmap = this.ConvertToImage(image);
            String base64Image = image;

            /*
            imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(myBitmap);
            imageView.setBackgroundColor(Color.TRANSPARENT);
            imageView.setVisibility(View.VISIBLE);
            */

            // Parse video
            jsonVideo = new JSONObject(response.toString());
            Object result= jsonVideo.get("video");
            String videoData = result.toString();
            String name = null;
            //byte[] decodedBytes = Base64.decode(videoData.getBytes(), Base64.DEFAULT); // Old behaviour
            byte[] decodedBytes = videoData.getBytes();
            try {
                File newfile;
                File filepath = new File(getApplicationContext().getFilesDir(), "BarcodeReaderSample" + File.separator + "videos");
                File dir = new File(filepath.getAbsolutePath()  + "/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                newfile = new File(dir, "save_" + System.currentTimeMillis() + ".mp4");
                if (!newfile.exists()){
                    newfile.createNewFile();
                }
                OutputStream out = new FileOutputStream(newfile);
                name = newfile.toString();
                out.write(decodedBytes);
                out.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            //videoView = (VideoView) findViewById(R.id.videoView);
            //MediaController mc = new MediaController(this);
            WebSettings settings = resultView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            resultView.getSettings().setAllowFileAccess(true);
            resultView.setVerticalScrollBarEnabled(true);
            resultView.setHorizontalScrollBarEnabled(true);

            //Add data to output HTML
            String content = "<HTML>"
                    + "<h1>It's me Mario..</h1>\n"
                    + "<ul>"
                    + "<li>"
                    + "Person:" + resultName.toString()
                    + "<br>"
                    + "<li>"
                    + "Text:" + resultText.toString()
                    + "<br>"
                    + "<li>"
                    + "Explanation:" + resultExplanation.toString()
                    + "</ul>"
                    + "<img src=\"data:image/jpeg;base64," + base64Image + "\"" + "\"width=\"300\" height=\"300\" \" />"
                    + "<br>"
                    + "<video controls>\n" +
                        "<source type=\"video/mp4\" src=\"data:video/webm;base64," + videoData + "\"width=\"300\" height=\"300\"" + "\">"
                    + "</video>"
                    + "</HTML>";
            resultView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
            resultView.setVisibility(View.VISIBLE);
            resultView.reload();

        } catch (JSONException e) {
            e.printStackTrace();
            result_textview.setText("Exception: " + e.toString());
        }
        //Toast.makeText(VolleyMethods.this, "" + e.toString(), Toast.LENGTH_LONG).show(); // Test
    }
}

