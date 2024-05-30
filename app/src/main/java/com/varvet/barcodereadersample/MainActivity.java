package com.varvet.barcodereadersample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class MainActivity extends AppCompatActivity {
    WebView resultView;
    Button scan_barcode_button;
    TextView result_textview;

    public static int BARCODE_READER_REQUEST_CODE = 2;
//    private static int BARCODE_READER_REQUEST_CODE = 1;
    private final String BASE_URL = "http://192.168.1.209:4000/interface/";
    private String results = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (WebView) findViewById(R.id.resultView);
        result_textview = (TextView) findViewById(R.id.result_textview);
        Button scanButton = (Button) findViewById(R.id.scan_barcode_button);
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
                    result_textview.setText(barcode.displayValue.toString());
                    VolleyLog.d("Resultview", "Status: " + barcode.displayValue.toString());
                    results = barcode.displayValue.toString();

                    HurlStack hurlStack = new HurlStack() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        protected HttpURLConnection createConnection(URL url) throws IOException {
                            HttpURLConnection httpURLConnection = (HttpURLConnection) super.createConnection(url);
                            return httpURLConnection;
                        }
                    };

                    final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
                    //requestQueue.add(request);

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, addJsonParams(results),new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            scan_barcode_button.setVisibility(View.INVISIBLE);
                                deleteCache(getApplicationContext());
                                parseResponseAllData(response);
                            //Log.d("onResponse", response.toString());
                            //Toast.makeText(VolleyMethods.this, response.toString(), Toast.LENGTH_LONG).show(); // Test
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

                        /**
                         * Passing some request headers
                         */
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            return headers;
                        }


                    };

                    requestQueue.add(jsonObjectRequest);

                    // GET METHOD
/*                   StringRequest request = new StringRequest(Request.Method.GET, BASE_URL, new Response.Listener<String>()   {
                    @Override
                    public void onResponse(String  response) {
                        String temp=null;
                        //Document document = Jsoup.parse(response);
                        Document document = Jsoup.parseBodyFragment(response);
                        Element body = document.body();
                        Elements paragraphs = body.getElementsByTag("td");
                        for (Element paragraph : paragraphs) {
                            if(paragraph.text().contains("https://") ) {
                                //System.out.println(paragraph.text());
                                temp = temp + paragraph.text() + "\n";
                            }
                        }

                           resultView.setText(temp);

                        //resultView.setText(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", error.toString());
                    }
                });
                queue.add(request);*/


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

    public Bitmap ConvertToImage(String image){
        try{
            InputStream stream = new ByteArrayInputStream(Base64.decode(image.getBytes(), Base64.DEFAULT));
            return BitmapFactory.decodeStream(stream);
        }
        catch (Exception e) {
            return null;
        }
    }

    /* parseResponseAllData(JSONOBject)
    * parse data and create strings and create web site from them
    */
    public void parseResponseAllData(JSONObject response) {

        JSONObject jsonString =null;
        JSONObject jsonVideo=null;
        JSONObject jsonImage=null;
        try{
            // Parse data strings etc. name and text
            jsonString = new JSONObject((response.toString()));
            Object resultName = jsonString.get("name");
            Object resultText = jsonString.get("text");
            Object resultExplanation = jsonString.get("explanation");
            //resultView.setBackgroundColor(Color.TRANSPARENT);

            // Parse Image
            jsonImage = new JSONObject(response.toString());
            Object resultImage = jsonImage.get("image");
            String image = resultImage.toString();
            //Bitmap myBitmap = this.ConvertToImage(image);
            String base64Image = image;

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

            WebSettings settings = resultView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            resultView.getSettings().setAllowFileAccess(true);
            resultView.setVerticalScrollBarEnabled(true);
            resultView.setHorizontalScrollBarEnabled(true);

            //Add data to output HTML
            String content = "<HTML>"
                    + "<h1>Hi you! It's me Mario..</h1>\n"
                    + "<ul>"
                    + "<li>"
                    + "Person:" + resultName
                    + "<br>"
                    + "<li>"
                    + "Text:" + resultText
                    + "<br>"
                    + "<li>"
                    + "Explanation:" + resultExplanation
                    + "</ul>"
                    + "<img src=\"data:image/jpeg;base64," + base64Image + "\"" + "\"width=\"300\" height=\"300\" \" />"
                    + "<br>"
                    + "<video controls>\n" +
                        "<source type=\"video/mp4\" src=\"data:video/webm;base64," + videoData + "\"width=\"300\" height=\"300\"" + "\">"
                    + "</video>"
                    + "</HTML>";
            resultView.loadDataWithBaseURL(BASE_URL, content, "text/html", "utf-8", null);
            resultView.setVisibility(View.VISIBLE);
            resultView.reload();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Toast.makeText(VolleyMethods.this, "" + e.toString(), Toast.LENGTH_LONG).show(); // Test
    }
}

