package com.github.oliverpavey.freedimmer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Scanner;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView textHelp = (TextView) findViewById(R.id.textHelp);
        try {
            Context ctx = getApplicationContext();
            InputStream helpResource = ctx.getResources().openRawResource(R.raw.help);
            String helpText = new Scanner(helpResource, "UTF-8")
                    .useDelimiter("\\A").next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textHelp.setText(Html.fromHtml(helpText, Html.FROM_HTML_MODE_COMPACT));
            } else {
                textHelp.setText(Html.fromHtml(helpText));
            }
        } catch (Exception e) {
            textHelp.setText(e.getClass().toString());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabCloseHelp);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
