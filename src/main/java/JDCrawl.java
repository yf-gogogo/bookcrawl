import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class JDCrawl {
    public static void main(String[] args) {
        String url = "https://search.jd.com/bookadvsearch?isbn=";
        String isbn = "9787121201677";
        Document doc = null;
        try {
            doc = Jsoup.connect(url+isbn).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element goodsElment = doc.getElementById("J_goodsList");

        Elements goodsList = goodsElment.select("li");
        for (Element element:goodsList){
            Elements pimgs = element.getElementsByClass("p-img");
//            System.out.println(pimgs);
            String bookurl = pimgs.select("a").attr("href");
//            System.out.println(bookurl);
            Document bookdoc = null;
            try {
                bookdoc = Jsoup.connect("https:"+bookurl).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements bookType = bookdoc.getElementById("crumb-wrap").getElementsByClass("crumb fl clearfix").select("a");
            Element bookTitle = bookdoc.getElementById("name");
            Elements bookPub = bookdoc.getElementById("parameter2").select("li");
            Element bookContent = bookdoc.getElementById("J-detail-content").getElementById("detail-tag-id-3");
//            System.out.println(bookType);
            for (Element e:bookType) {
                System.out.print(e.text()+'>');
            }
            System.out.println();
            System.out.println(bookTitle.getElementsByClass("sku-name").text());
            System.out.println(bookTitle.getElementsByClass("p-author").text());
            for (Element e:bookPub) {
                System.out.println(e.text());
            }
            if(bookContent!= null){
                System.out.println(bookContent.text());
            }
            break;
        }
    }
}
