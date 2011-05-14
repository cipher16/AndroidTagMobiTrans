package eu.tagmobitrans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import android.widget.Toast;

public class tag extends Activity {
    /** Called when the activity is first created. */
	EditText arret;
	Button recherche;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arret= (EditText)findViewById(R.id.arret);
        recherche= (Button)findViewById(R.id.rechercher);
        setContentView(R.layout.main);
        
        recherche.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getHoraire();
			}
		});
    }
    
    public void getHoraire()
    {
    	String page="";
    	String time="";
        HttpClient cli = new DefaultHttpClient();
    	HttpGet siteVar   = new HttpGet("http://tag.mobitrans.fr/index.php");
    	HttpResponse resp;
    	String res, content=new String(),ret = new String();
		boolean inForm=false;
    	try {
			resp = cli.execute(siteVar);
			BufferedReader read = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			while((res=read.readLine())!=null)
			{
			   if(res.matches(".*<form name=\"f_arret\" .*")){inForm=true;}
			   if(inForm==true&&res.matches(".*</form>.*")){inForm=false;break;}
			   if(inForm==true){content+=res;}
			}
			Pattern p = Pattern.compile("<input type=\"hidden\" name=\"p\" value=\"(.*)\">");
			Matcher m = p.matcher(content);
			if(m.find()){page=m.group(1);}
			p = Pattern.compile("<input type=\"hidden\" name=\"I\" value=\"(.*)\">");
			m = p.matcher(content);
			if(m.find()){time=m.group(1);}
		}catch (Exception e) {
        	Toast toast=Toast.makeText(this, "An error Occured"+e.getMessage(), Toast.LENGTH_LONG);  
        	toast.show();
        }
//    	arret:grand pr%E9x (unable to decode value)
//    	p:15            x
//    	I:m00417i   
//    	selecteur:1     x
//    	s_arret:Valider x
    	
    	HttpPost site   = new HttpPost("http://tag.mobitrans.fr/index.php");
    	
    	ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("s_arret", "Valider"));
    	nvps.add(new BasicNameValuePair("selecteur", "1"));
    	/*A recuperer*/
    	nvps.add(new BasicNameValuePair("I", time));
    	nvps.add(new BasicNameValuePair("p", page));
    	nvps.add(new BasicNameValuePair("arret", arret.getText().toString()));

        try {

        	site.setEntity(new UrlEncodedFormEntity(nvps, "iso-8859-15"));
         resp = cli.execute(site);
         BufferedReader read = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
         while((res=read.readLine())!=null)
         {
//        	 System.out.println(res);
//          if(res.matches(".*<table class=.depart.*")){dTab=true;}
//          if(dTab==true&&res.matches(".*</table>.*")){dTab=false;break;}
//          if(dTab==true){content+=res;}
//         }
//         Pattern p = Pattern.compile("<td[^>]*>([^<]*)</td><td[^>]*>([^<]*)</td><td[^>]*>([^<]*)</td>");
//         Matcher m = p.matcher(content);
//         while(m.find())
//         {
//
         }
        }
        catch (Exception e) {
        	Toast toast=Toast.makeText(this, "An error Occured"+e.getMessage(), Toast.LENGTH_LONG);  
        	toast.show();
        }
    }
}