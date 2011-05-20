package eu.tagmobitrans;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class tag extends Activity {
	final static String URL = "http://tag.mobitrans.fr/";
	final static int nbLinkInFooter = 5;//nb de liens en bas de page, histoire de parser tout les liens sans taper dedans
	
	EditText arret;
	Button recherche;
	TextView text;
	ListView afficherDonnee;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        arret= (EditText)findViewById(R.id.arret);
        text= (TextView)findViewById(R.id.information);
        afficherDonnee=(ListView)findViewById(R.id.afficher_donnee);
        
        recherche= (Button)findViewById(R.id.rechercher);
        recherche.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(arret.getText().length()==0)
					return;
				TagVar tv = TagVar.getInstance(tag.this);
				if(!tv.isInitialyzed())
					tv.parseIndex(URL);
			/*Displaying data*/
				SimpleAdapter mSchedule = new SimpleAdapter (tag.this.getBaseContext(), tv.parsePostPostArret(URL, arret.getText().toString()), R.layout.arret, new String[] {"nom", "heure"}, new int[] {R.id.nom, R.id.heure});
		        afficherDonnee.setAdapter(mSchedule);
			}
		});        
    }
}