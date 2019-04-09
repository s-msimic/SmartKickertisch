package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MenuFolderActivity extends Activity {

    private FirebaseAuth mAuth;
    private Button scn_btn;

    public void onClickLogOut(View view){
        mAuth.signOut();
        Intent i = new Intent(MenuFolderActivity.this,WelcomeActivity.class);
        startActivity(i);
    }

    public void onClickScan(View view){
        Intent i = new Intent(MenuFolderActivity.this,setupGameActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_folder);
        scn_btn = (Button) findViewById(R.id.scan_btn);
        final Activity activity = this;
        scn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("just scan the kicker side you want to play");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "You cancelled the scan", Toast.LENGTH_LONG).show();
            }
            else{

                // go to new window from here after scan was successful
                Intent i = new Intent(MenuFolderActivity.this,setupGameActivity.class);
                startActivity(i);
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    };
}
