package eu.tagmobitrans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

public class TagVar {
	final String UA = "Mozilla/5.0 (X11; Linux i686) AppleWebKit/534.36 (KHTML, like Gecko) Ubuntu/11.04";
	
	ArrayList<String> urls;
	private String Tag_GET_i,Tag_GET_rd;
	private String Tag_POST_p,Tag_POST_I;
	static TagVar instance;
	private Activity activity;
	
	public enum Urlid {
		PROCHAIN_PASSAGE (13),
		FAVORIS (31),
		TRAFIC (19);
		
		private int id;
		Urlid(int id) {
			this.id=id;
		}
		public int getId()
		{
			return this.id;
		}
	}
	public static TagVar getInstance(Activity a)
	{
		if(instance==null)
			instance=new TagVar(a);
		return instance;
	}
	public boolean isInitialyzed()
	{
		return urls.size()>0;
	}
	private TagVar(Activity a) {
		activity=a;
		urls = new ArrayList<String>();
	}
	private boolean setVars()
	{
		if(urls.size()==0)
			return false;
		Pattern p = Pattern.compile("p=([0-9])+&m=([0-9]+)&I=([0-9A-Za-z]+)&rd=([0-9]+)$");
		Matcher m = p.matcher(urls.get(0));
		if(m.find()&&m.groupCount()>=4)
		{
			Tag_GET_i=m.group(3);
			Tag_GET_rd=m.group(4);
			return true;
		}	
		return false;
	}
	
	private void displayToast(String message)
	{
		Toast toast=Toast.makeText(activity, message, Toast.LENGTH_LONG);  
		toast.show();
	}

	/**
	 * To get vars I and rd
	 * @param url
	 * @param text
	 */
	synchronized public void parseIndex(String url)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest getRequest = new HttpGet(url);
		getRequest.setHeader("User-Agent",  UA);
		try {
			HttpResponse response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==200)
			{
				HttpEntity entity = response.getEntity();
				HtmlCleaner hc = new HtmlCleaner();
				TagNode tn = hc.clean(entity.getContent());
				/*Only the first would be enough but ...*/
				for (TagNode node : tn.getElementsByName("a", true)) {
					urls.add(node.getAttributeByName("href"));
				}
				setVars();
			}
		} catch (Exception e) {
			displayToast("[ParseIndex]Exception "+e.getMessage());
		}
	}
	/**
	 * To get needed hidden fields
	 * @param url
	 * @param text
	 */
	synchronized private void parsePrePostArret(String url)
	{
		if(urls.isEmpty())
			return;
		url+=urls.get(Urlid.PROCHAIN_PASSAGE.ordinal());//add stored parameters
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest getRequest = new HttpGet(url);
		getRequest.setHeader("User-Agent",  UA);
		try {
			HttpResponse response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==200)
			{
				HttpEntity entity = response.getEntity();
				HtmlCleaner hc = new HtmlCleaner();
				TagNode tn = hc.clean(entity.getContent());
				/*form f_arret must be found*/
				for (TagNode node : tn.getElementsByAttValue("name", "f_arret", true, true)[0].getElementsByName("input", false)) {
					if(node.hasAttribute("name")&&node.getAttributeByName("name").equals("p"))
						Tag_POST_p=node.getAttributeByName("value");
					if(node.hasAttribute("name")&&node.getAttributeByName("name").equals("I"))
						Tag_POST_I=node.getAttributeByName("value");
				}
			}
		} catch (Exception e) {
			displayToast("[ParsePrePostArret]Exception "+e.getMessage());
		}
	}
	/**
	 * Parse response of arret request
	 * @param url
	 */
	synchronized public ArrayList<HashMap<String, String>> parsePostPostArret(String url,String arret)
	{
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		if(urls.isEmpty())
			return listItem;
		parsePrePostArret(url);
		url+=urls.get(Urlid.PROCHAIN_PASSAGE.ordinal());//add stored parameters
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost site = new HttpPost(url);
		site.setHeader("User-Agent",  UA);
    	ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("s_arret", "Valider"));
    	nvps.add(new BasicNameValuePair("selecteur", "1"));//here we choose to get only arret
    	nvps.add(new BasicNameValuePair("I", Tag_POST_I));
    	nvps.add(new BasicNameValuePair("p", Tag_POST_p));
    	nvps.add(new BasicNameValuePair("arret", arret));
        try {
        	site.setEntity(new UrlEncodedFormEntity(nvps, "iso-8859-15"));
        	HttpResponse response = client.execute(site);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==200)
			{
				HttpEntity entity = response.getEntity();
				HtmlCleaner hc = new HtmlCleaner();
				TagNode tn = hc.clean(entity.getContent());
				for (Object o : tn.evaluateXPath("//div[@class='corpsL']//div")) {
					
					HashMap<String, String> map;
			        map = new HashMap<String, String>();
			        map.put("nom", ((TagNode) o).getElementsByName("a", false)[0].getText()+
			        		" Ligne : "+((TagNode) o).getElementsByName("span", false)[1].getText());
			        map.put("heure", getHoraire(tag.URL+((TagNode) o).getElementsByName("a", false)[0].getAttributeByName("href")));
			        listItem.add(map);
				}
			}
        }catch(Exception e)
        {
        	displayToast("[ParsePostArret]Exception "+e.getMessage());
        }
        return listItem;
	}
	
	synchronized public String getHoraire(String url)
	{
		String horaire="";
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest getRequest = new HttpGet(url);
		getRequest.setHeader("User-Agent",  UA);
		getRequest.setHeader("Accept-Charset","iso-8859-15");
		try {
			HttpResponse response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==200)
			{
				HttpEntity entity = response.getEntity();
				HtmlCleaner hc = new HtmlCleaner();
				TagNode tn = hc.clean(entity.getContent());
				
				Pattern p = Pattern.compile("(Prochain[^<\n\t]*|Vers[^<\n\t]*)",Pattern.MULTILINE);
				Matcher m;
				for (Object o : tn.evaluateXPath("//div[@class='corpsL']")) {
					m=p.matcher(((TagNode)o).getText().toString());
					while(m.find())
					{
						if(m.group(1).startsWith("Vers"))
							horaire+="\n";
						else
							horaire+="\t";
						horaire+= m.group(1)+ "\n";
					}
				}
			}
		} catch (Exception e) {
			displayToast("[getHoraire]Exception "+e.getMessage());
		}
		return (horaire.length()==0?"Aucun horaire n'a pu être trouvé":horaire);
	}
}
