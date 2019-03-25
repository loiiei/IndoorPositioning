package com.example.loinguyen.indoorposition;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.loinguyen.indoorposition.Bean.IBeacon;
import com.example.loinguyen.indoorposition.Database.DBManager;

public class FingerPrintDB extends AppCompatActivity {

    EditText editXCoord, editYCoord, editRssi1, editRssi2, editRssi3, editMajor;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);

        final DBManager db = new DBManager(this);

      //  db.addIBeacon(iBeacon);
        editXCoord = findViewById(R.id.x);
        editYCoord = findViewById(R.id.y);
        editRssi1 = findViewById(R.id.rssi1);
        editRssi2 = findViewById(R.id.rssi2);
        editRssi3 = findViewById(R.id.rssi3);
        editMajor = findViewById(R.id.major);
        btn = findViewById(R.id.btn_save);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IBeacon iBeacon = createIBeacon();
                if(iBeacon != null){
                    db.addIBeacon(iBeacon);
                }
                editXCoord.setText("");
                editYCoord.setText("");
                editRssi1.setText("");
                editRssi2.setText("");
                editRssi3.setText("");
                editMajor.setText("");
            }
        });
    }
    private IBeacon createIBeacon()
    {
        float x = Float.parseFloat(editXCoord.getText().toString());
        float y = Float.parseFloat(editYCoord.getText().toString());
        float rssi1  = Float.parseFloat(editRssi1.getText().toString());
        float rssi2  = Float.parseFloat(editRssi2.getText().toString());
        float rssi3  = Float.valueOf(editRssi3.getText().toString());
        int major  = Integer.valueOf(editMajor.getText().toString());
        IBeacon iBeacon = new IBeacon(x, y, rssi1, rssi2, rssi3, major);
        return iBeacon;
    }
}
