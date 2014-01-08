
package com.letsface.simplecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class PictureConfirmActivity extends Activity implements OnClickListener {

    public static final String EXTRA_IMAGE = "extra_image";

    private Bitmap getIntentImage() {
        Intent intent = getIntent();
        return intent.getParcelableExtra(EXTRA_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bitmap bitmap = getIntentImage();
        if (bitmap == null)
            finish();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_confirm);

        ImageView image = (ImageView) findViewById(R.id.still_preview);
        image.setImageBitmap(bitmap);

        findViewById(R.id.action_confirm).setOnClickListener(this);
        findViewById(R.id.action_retake).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.action_confirm) {
            confirm();
        } else if (id == R.id.action_retake) {
            retake();
        }
    }

    private void confirm() {
        setResult(RESULT_OK);
        finish();
    }

    private void retake() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
