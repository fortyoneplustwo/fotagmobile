package ca.uwaterloo.cs349.fotagmobileoambrois;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.FileInputStream;

public class DisplayImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        // extract bitmap.png and set image
        try {
            FileInputStream is = this.openFileInput("bitmap.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            ImageView image = findViewById(R.id.fullscreenImage);
            image.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void closeImage(View view) {
        this.finish();
    }
}
