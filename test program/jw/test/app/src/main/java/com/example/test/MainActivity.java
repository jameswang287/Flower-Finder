package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import androidx.annotation.NonNull;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static int RESULT_LOAD_IMAGE = 1;
    Button takePicture;
    Uri image_uri;


    /**
     * ya know, this opens the camera
     */
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    /**
     * This method is called when the user responds to the permissions request pop up
     * @param requestCode same as parent
     * @param permissions same as parent
     * @param grantResults same as parent
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    //permissions were granted
                    openCamera();
                }else{
                    //permissions denied :(
                    Toast.makeText(this, "Permissions Denied. You succ.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Our Buttons!
        Button buttonLoadImage = (Button) findViewById(R.id.button);
        Button detectButton = (Button) findViewById(R.id.detect);
        takePicture = (Button) findViewById(R.id.take_picture_button);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0){
                //If Android OS version is >= Marshmallow, request permissions.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    if(checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_DENIED) {
                        //Request permissions that were not allowed
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //Request permissions via pop up
                        requestPermissions(permission, PERMISSION_CODE);

                    }else {
                        //permissions already granted
                        openCamera();
                    }
                } else {
                    //os is < marshmallow
                    openCamera();
                }
            }
        });

        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TextView textView = findViewById(R.id.result_text);
                textView.setText("");
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        detectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Bitmap bitmap = null;
                Module module = null;
                //Getting the image from the image view
                ImageView imageView = (ImageView) findViewById(R.id.image);
                try {
                    //Read the image as Bitmap
                    bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                    //Here we reshape the image into 400*400
                    bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                    //Loading the model file.
                    module = Module.load(fetchModelFile(MainActivity.this, "resnet18_traced.pt"));
                } catch (IOException e) {
                    finish();
                }

                //Input Tensor
                final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
                        bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                        TensorImageUtils.TORCHVISION_NORM_STD_RGB
                );

                //Calling the forward of the model to run our input
                final Tensor output = module.forward(IValue.from(input)).toTensor();


                final float[] score_arr = output.getDataAsFloatArray();

                // Fetch the index of the value with maximum score
                float max_score = -Float.MAX_VALUE;
                int ms_ix = -1;
                for (int i = 0; i < score_arr.length; i++) {
                    if (score_arr[i] > max_score) {
                        max_score = score_arr[i];
                        ms_ix = i;
                    }
                }

                //Fetching the name from the list based on the index
                String detected_class = ModelClasses.MODEL_CLASSES[ms_ix];

                //Writing the detected class in to the text view of the layout
                TextView textView = findViewById(R.id.result_text);
                textView.setText(detected_class);

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This functions return the selected image from gallery
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.image);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            //Setting the URI so we can read the Bitmap from the image
            imageView.setImageURI(null);
            imageView.setImageURI(selectedImage);
        }
        else if (resultCode == RESULT_OK){
            ImageView imageView = (ImageView) findViewById(R.id.image);
            //set the image captured to our ImageView
            imageView.setImageURI(null);
            imageView.setImageURI(image_uri);
        }

    }

    /**
     * Gets the file stream of the model.
     * @param context gets file context
     * @param modelName name of the model file to find
     * @return filestream for the model
     * @throws IOException
     */
    public static String fetchModelFile(Context context, String modelName) throws IOException {
        File file = new File(context.getFilesDir(), modelName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(modelName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}