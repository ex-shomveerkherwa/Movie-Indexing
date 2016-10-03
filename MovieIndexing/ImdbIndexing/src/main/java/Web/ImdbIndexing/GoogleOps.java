package Web.ImdbIndexing;

import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GoogleOps {

	private static Pattern patternDomainName;
	  private Matcher matcher;
	  private static final String DOMAIN_NAME_PATTERN = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
	  static {
		patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
	  }

	  public static void main(String[] args) {
		System.out.println("Enter movie name");
		Scanner scanner = new Scanner(System.in);
		String movieInput = scanner.nextLine();
		GoogleOps obj = new GoogleOps();
		Set<String> result = obj.getDataFromGoogle(movieInput);
		
		System.out.println();
		System.out.println("Queried from "+result.size() +" links mentioned below");
		if(result.size() > 0){
			for(String temp : result){
				System.out.println(temp);
			}
		}
		
		
	  }

	  private Set<String> getDataFromGoogle(String query) {
		Set<String> result = new HashSet<String>();
		String request = "https://www.google.com/search?q=" + query + "&num=5";
		//System.out.println("Sending request..." + request);
		try {
			// need http protocol, set this as a Google bot agent :)
			Document doc = Jsoup
				.connect(request)
				.userAgent(
				  "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
				.timeout(5000).get();

			// get all links
			Elements links = doc.select("a[href]");
			for (Element link : links) {

				String temp = link.attr("href");
				if(temp.startsWith("/url?q=")){
                    //use regex to get domain name
					String webSite = getDomainName(temp);
					
					if(webSite.equalsIgnoreCase("www.imdb.com")) {
						temp = temp.replace("/url?q", "");
						int length = temp.lastIndexOf("/");
						temp = temp.substring(1, length);
						Document imdbDoc = Jsoup.connect(temp).get();
						
						String title = imdbDoc.getElementsByClass("title_wrapper").text();
						
						String plot = imdbDoc.getElementsByClass("summary_text").text();
						
						Elements ratingLinks = imdbDoc.getElementsByClass("ratingValue").select("strong[title]").select("span[itemprop]");
						System.out.println("Title       \t"+title);
						System.out.println("imdb Rating \t" + ratingLinks.get(0).text());
						System.out.println("Plot        \t"+plot);
						result.add(webSite);
						break;
					}
					result.add(webSite);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	  }
	  
	  public String getDomainName(String url){
			String domainName = "";
			matcher = patternDomainName.matcher(url);
			if (matcher.find()) {
				domainName = matcher.group(0).toLowerCase().trim();
			}
			return domainName;
		  }
}
