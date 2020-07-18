package com.example.test.ui.main;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.test.MainActivity;
import com.example.test.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetectFragment extends Fragment implements View.OnClickListener {

    //Consts
    //Status and permission codes
    private static int RESULT_LOAD_IMAGE = 1;
    private static int CAMERA_PERMISSION_CODE = 100;
    private static int READ_PERMISSION_CODE = 200;
    private static int WRITE_PERMISSION_CODE = 201;
    private static int GPS_PERMISSION_CODE = 300;
    private static String DET_ERROR_NO_BMP = "Load an image first!";
    private static final String ARG_SECTION_NUMBER = "section_number";

    //UI Components
    private Button loadImageButton;
    private Button takePictureButton;
    private ImageView imageView;
    private TextView textView;

    //Module with the model
    Module modelModule;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveBitmap(Bitmap bitmap, String result) throws IOException {
        // Assume block needs to be inside a Try/Catch block.
        try {
            String gps = "";
            String path = getActivity().getApplicationContext().getFilesDir().toString();
            Log.d("FlowerFinder Path", path);

            File history = new File(path + "history.csv");
            history.createNewFile();

            OutputStream fOut = null;

            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("MMddyyyyHHmmss");

            String dateTimeAppend = currentDateTime.format(dateTimeFormat);
            String filename = "FlowerFinder" + dateTimeAppend + ".jpg";

            File file = new File(path, filename);
            fOut = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression
            fOut.flush();
            fOut.close();

            FileWriter fWriter = new FileWriter(history);
            PrintWriter pWriter = new PrintWriter(fWriter);
            String newLn = filename + ", " + result + ", " + gps;
            pWriter.println(newLn);
            pWriter.close();

            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch(IOException e){
            Log.d("FlowerFinder", "Cannot write to disk");
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE ,WRITE_PERMISSION_CODE);
        }
    }

    public String fetchModelFile(DetectFragment context, String modelName) throws IOException {
        File file = new File(getActivity().getApplicationContext().getFilesDir(), modelName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = getActivity().getApplicationContext().getAssets().open(modelName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (Exception e){
            Log.d("FlowerFinder", "Model not found");
        }
        return "";
    }

    public static DetectFragment newInstance(int index) {
        DetectFragment fragment = new DetectFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Function to detect whatever is in the imageView with the model
     * Also saves to history based on detection time.
     * @return a String of the result after detection.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String detect() throws IOException {

        String result = "";
        Bitmap bitmap = null;
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, GPS_PERMISSION_CODE);

        //Getting the image from the image view
        try {
            //Read the image as Bitmap
            bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            //Here we reshape the image into 400*400
            bitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

        } catch (Exception e) {
            //load image first, no bmp loaded
            return DET_ERROR_NO_BMP;
        }

        //Input Tensor
        final Tensor input = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );

        //Calling the forward of the model to run our input
        final Tensor output = modelModule.forward(IValue.from(input)).toTensor();

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
        result = ModelClasses.MODEL_CLASSES[ms_ix];
        textView.setText(result);
        Log.d("FlowerFinder", result);
        saveBitmap(bitmap, result);

        return result;
    }

    /**
     * Checks if permissions were granted. Need to check on the fly as permissions can be
     * revoked at any time in new Android versions.
     * @param permission The permission to check.
     * @param requestCode The request code of the permission.
     */
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(),
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),
                    permission + " Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageViewModel pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);

        //Loading the model file.
        try {
            modelModule = Module.load(fetchModelFile(DetectFragment.this, "resnet18_traced.pt"));
        } catch (IOException e) {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),"Cannot find Model file.",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_PERMISSION_CODE);

        View view = inflater.inflate(R.layout.detect_layout, container, false);
        loadImageButton = view.findViewById(R.id.load_img_button);
        takePictureButton = view.findViewById(R.id.take_pic_button);
        imageView = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.result_text);
        loadImageButton.setOnClickListener(this);
        takePictureButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.load_img_button:
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_PERMISSION_CODE);
                textView.setText("");
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
            case R.id.take_pic_button:
                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                //take pic and detect
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Log.d("FlowerFinder", "intent created");
                startActivityForResult(intent, CAMERA_PERMISSION_CODE);
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This functions return the selected image from gallery
        super.onActivityResult(requestCode, resultCode, data);

        //load image
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getApplicationContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            //Setting the URI so we can read the Bitmap from the image
            imageView.setImageURI(null);
            imageView.setImageURI(selectedImage);
            try {
                detect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (requestCode == CAMERA_PERMISSION_CODE && resultCode == Activity.RESULT_OK){
            Log.d("FlowerFinder", "Here");

            //get capture
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
            //Set capture to imageView
            imageView.setImageBitmap(captureImage);
            try {
                detect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}