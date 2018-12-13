import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.*;

public class DangDangCrawl {
    public static void main(String[] args) {
        Map<String,Object> bookMap = new HashMap<String, Object>();
        String url = "http://search.dangdang.com/?medium=01&category_path=01.00.00.00.00.00&key3=";
        String isbn = "9787121201677";
        Document listdoc = null;
        try {
            listdoc = Jsoup.connect(url+isbn).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取书籍列表
        Elements booklist = listdoc.getElementById("search_nature_rg").select("li");
//        System.out.println(booklist);
        String bookurl = null;
        String bookimg = null;
        //从书籍列表元素中第一个元素获取书籍详情url和封面img
        for (Element e:booklist){
            bookurl = e.select("a").attr("href");
            bookimg = e.select("img").attr("src");
            break;
        }
        bookMap.put("img",bookimg);
        Document bookdoc = null;
        //获取书籍详情页面Document对象
        try {
            bookdoc = Jsoup.connect(bookurl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //获取书籍信息Element
        Element bookInfo = bookdoc.getElementById("product_info");
        //获取书标题
        String bookTitle = bookInfo.select("h1").text();
        System.out.println(bookTitle);
        bookMap.put("title",bookTitle);
        Elements bookPub = bookInfo.getElementsByClass("name_info").next().select("span");
        //只需要前三列内容
        if(bookPub.size()>=3) {
            bookMap.put("author", bookPub.get(0).text());
            bookMap.put("publish", bookPub.get(1).text());
            bookMap.put("date", bookPub.get(2).text());
        }
        //获取商品详情
        Elements bookdetail = bookdoc.getElementById("detail_describe").select("li");
        Elements bookType = null;
        for (Element e:bookdetail) {
            //提取商品类别
            if(e.getElementById("detail-category-path")!=null){
                bookType = e.getElementById("detail-category-path").select("span");
            }else {
                System.out.println(e.text());
            }
        }
        List types = new ArrayList();
        for (Element e:bookType) {
            System.out.println(e.text());
            types.add(e.text());
        }
        bookMap.put("type",types);
        //由于异步加载的url用到script脚本中的变量，故在此处提取变量
        Elements escript = bookdoc.getElementsByTag("script");
        String[] elScriptList = escript.get(1).data().toString().split("var");
        //以"为分隔符
        String[] strTypeList = elScriptList[2].split("\"");
        Map map = new HashMap<String,String>();
        for (int i =0;i<strTypeList.length;i++) {
            if(strTypeList[i].equals("productId")){
                map.put("productId",strTypeList[i+2]);
            }
            if(strTypeList[i].equals("describeMap")){
                map.put("describeMap",strTypeList[i+2]);
            }
            if(strTypeList[i].equals("categoryPath")){
                map.put("categoryPath",strTypeList[i+2]);
            }
        }
        //网页上动态加载的内容url
        String ajaxurl = "http://product.dangdang.com/index.php?r=callback%2Fdetail&productId="
                +map.get("productId")+"&templateType=publish&describeMap="+
                map.get("describeMap")+"&shopId=0&categoryPath="+map.get("categoryPath");
        HttpGet client = new HttpGet(ajaxurl);
        CloseableHttpClient httpCient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpCient.execute(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity httpEntity = httpResponse.getEntity();
        String body = null;
        try {
            body = EntityUtils.toString(httpEntity,"utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String转Json
        JSONObject jsonObject = new JSONObject(body);

        String htmlcontent = jsonObject.getJSONObject("data").get("html").toString();
        Document content = Jsoup.parse(htmlcontent);
        String bookcontent = null;
        if(content.getElementById("content")!= null){
            bookcontent = content.getElementById("content").getElementsByClass("descrip").text();
        }

        bookMap.put("content",bookcontent);
        System.out.println(bookMap);
    }
}
