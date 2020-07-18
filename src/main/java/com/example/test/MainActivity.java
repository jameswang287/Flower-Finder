//package com.example.test;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.pytorch.IValue;
//import org.pytorch.Module;
//import org.pytorch.Tensor;
//import org.pytorch.torchvision.TensorImageUtils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class MainActivity extends AppCompatActivity {
//    private static int RESULT_LOAD_IMAGE = 1;
//    private static int CAMERA_PERMISSION_CODE = 100;
//    private static int READ_PERMISSION_CODE = 200;
//    private static int WRITE_PERMISSION_CODE = 201;
//    private static int GPS_PERMISSION_CODE = 300;
//    private static String DET_ERROR_NO_BMP = "Load an image first!";
//
//    //Variables
//    //UI Components
//    private Button loadImageButton;
//    private Button takePictureButton;
//    private ImageView imageView;
//    private TextView textView;
//
//    //Module with the model
//    Module modelModule;
//
//    //History IO
//    File historyFile;
//    FileOutputStream historyOutStream;
//
//
//    /**
//     * Function to detect whatever is in the imageView with the model
//     * Also saves to history based on detection time.
//     * @return a String of the result after detection.
//     */
//    public String detect(){
//        String result = "";
//        Bitmap bitmap = null;
//        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, GPS_PERMISSION_CODE);
//
//        //Getting the image from the image view
//        try {
//            //Read the image as Bitmap
//            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//
//            //Here we reshape the image into 400*400
//            bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
//
//        } catch (Exception e) {
//            //load image first, no bmp loaded
//            return DET_ERROR_NO_BMP;
//        }
//
//        //Input Tensor
//        final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
//                bitmap,
//                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//                TensorImageUtils.TORCHVISION_NORM_STD_RGB
//        );
//        //Calling the forward of the model to run our input
//        final Tensor output = modelModule.forward(IValue.from(input)).toTensor();
//        final float[] score_arr = output.getDataAsFloatArray();
//        // Fetch the index of the value with maximum score
//        float max_score = -Float.MAX_VALUE;
//        int ms_ix = -1;
//        for (int i = 0; i < score_arr.length; i++) {
//            if (score_arr[i] > max_score) {
//                max_score = score_arr[i];
//                ms_ix = i;
//            }
//        }
//        //Fetching the name from the list based on the index
//        // result = ModelClasses.MODEL_CLASSES[ms_ix];
//        textView.setText(result);
//        return result;
//    }
//
//    /**
//     * Checks if permissions were granted. Need to check on the fly as permissions can be
//     * revoked at any time in new Android versions.
//     * @param permission The permission to check.
//     * @param requestCode The request code of the permission.
//     */
//    public void checkPermission(String permission, int requestCode)
//    {
//        // Checking if permission is not granted
//        if (ContextCompat.checkSelfPermission(
//                MainActivity.this,
//                permission)
//                == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(
//                            MainActivity.this,
//                            new String[] { permission },
//                            requestCode);
//        }
//        else {
//            Toast.makeText(MainActivity.this,
//                            permission + " Permission already granted",
//                            Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        //Loading the model file.
//        try {
//            modelModule = Module.load(fetchModelFile(MainActivity.this, "resnet18_traced.pt"));
//        } catch (IOException e) {
//            Toast toast = Toast.makeText(getApplicationContext(),"Cannot find Model file.",Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        //Init/link UI Components
//        loadImageButton = (Button) findViewById(R.id.load_img_button);
//        takePictureButton = (Button) findViewById(R.id.take_pic_button);
//        imageView = (ImageView) findViewById(R.id.imageView);
//        textView = findViewById(R.id.result_text);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_PERMISSION_CODE);
//            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_PERMISSION_CODE);
//
//        }
//
//        //Set click listeners
//        loadImageButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                textView.setText("");
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//                startActivityForResult(i, RESULT_LOAD_IMAGE);
//
//            }
//        });
//
//        takePictureButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
//                //take pic and detect
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                Log.d("FlowerFinder", "intent created");
//                startActivityForResult(intent, CAMERA_PERMISSION_CODE);
//
//                //Writing the detected class in to the text view of the layout
//                //textView.setText(detected_class);
//
//
//            }
//        });
//
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //This functions return the selected image from gallery
//        super.onActivityResult(requestCode, resultCode, data);
//
//        //load image
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
//            Uri selectedImage = data.getData();
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//
//
//            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
//            //Setting the URI so we can read the Bitmap from the image
//            imageView.setImageURI(null);
//            imageView.setImageURI(selectedImage);
//            detect();
//
//        }
//
//        if (requestCode == CAMERA_PERMISSION_CODE && resultCode == RESULT_OK){
//            Log.d("FlowerFinder", "Here");
//
//            //get capture
//            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
//            //Set capture to imageView
//            imageView.setImageBitmap(captureImage);
//            detect();
//        }
//
//    }
//
//    public static String fetchModelFile(Context context, String modelName) throws IOException {
//        File file = new File(context.getFilesDir(), modelName);
//        if (file.exists() && file.length() > 0) {
//            return file.getAbsolutePath();
//        }
//
//        try (InputStream is = context.getAssets().open(modelName)) {
//            try (OutputStream os = new FileOutputStream(file)) {
//                byte[] buffer = new byte[4 * 1024];
//                int read;
//                while ((read = is.read(buffer)) != -1) {
//                    os.write(buffer, 0, read);
//                }
//                os.flush();
//            }
//            return file.getAbsolutePath();
//        }
//    }
//
//}

package com.example.test;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

    }

}