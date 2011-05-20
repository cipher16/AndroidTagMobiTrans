package eu.tagmobitrans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class tag extends Activity {
	final static String URL = "http://tag.mobitrans.fr/";
	EditText arret;
	Button recherche;
	TextView text;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        arret= (EditText)findViewById(R.id.arret);
        text= (TextView)findViewById(R.id.information);
        recherche= (Button)findViewById(R.id.rechercher);
        recherche.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(arret.getText().length()==0)
					return;
				TagVar tv = TagVar.getInstance(tag.this);
				if(!tv.isInitialyzed())
					tv.parseIndex(URL);
				tv.parsePostPostArret(URL, arret.getText().toString());
			}
		});
    }
}