package com.encryption;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

public class BigViewActivity extends AppCompatActivity {

    private SimpleDraweeView img_big;

    private String currPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_view);

        boolean hasParams = getIntentParams();

        if (! hasParams) {
            return;
        }

        img_big = (SimpleDraweeView) findViewById(R.id.img_big);
        img_big.setImageURI(Uri.parse("file://" + currPath));
    }

    /**
     * 获取上个界面传递的参数
     *
     * @return
     */
    private boolean getIntentParams() {
        Intent intent = getIntent();

        currPath = intent.getStringExtra("path");
        Log.i("TEST", "----------> currPage=" + currPath);

        return ! TextUtils.isEmpty(currPath);
    }


}
