package com.example.ffhr.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ffhr.R;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LeafScanActivity extends AppCompatActivity {
    private ImageView imageViewLeaf;
    private TextView tvResult;
    private Module module;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaf_scan);

        imageViewLeaf = findViewById(R.id.imageViewLeaf);
        tvResult = findViewById(R.id.tvResult);
        Button btnCapture = findViewById(R.id.btnCapture);

        try {
            // Load the mobile model from the assets folder
            module = LiteModuleLoader.load(assetFilePath(this, "leaf_scanner_mobile.ptl"));
        } catch (IOException e) {
            tvResult.setText("Error loading AI model!");
        }

        btnCapture.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageViewLeaf.setImageBitmap(bitmap);
            tvResult.setText("Analyzing...");

            if (module != null) {
                // 1. Convert the image to a PyTorch Tensor
                Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

                // 2. Run the image through your model
                Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
                float[] scores = outputTensor.getDataAsFloatArray();

                // 3. Find the disease class with the highest confidence score
                int maxScoreIdx = 0;
                float maxScore = scores[0];
                for (int i = 1; i < scores.length; i++) {
                    if (scores[i] > maxScore) {
                        maxScore = scores[i];
                        maxScoreIdx = i;
                    }
                }

                // Display the predicted class index (0 to 41)
                tvResult.setText("Detected Disease Index: " + maxScoreIdx);
            }
        }
    }

    // Helper method to copy the model from assets to the device's storage so PyTorch can read it
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) return file.getAbsolutePath();
        try (InputStream is = context.getAssets().open(assetName); FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            os.flush();
        }
        return file.getAbsolutePath();
    }
}