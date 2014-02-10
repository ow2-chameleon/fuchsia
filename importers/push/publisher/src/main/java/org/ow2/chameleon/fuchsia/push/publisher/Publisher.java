package org.ow2.chameleon.fuchsia.push.publisher;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Instantiate
public class Publisher extends HttpServlet {

    @Requires
    HttpService http;

    private static final Logger log= LoggerFactory.getLogger(Publisher.class);

    BundleContext context;

    private static String PUBLISHER_URL="/publisher/main";

    public Publisher(){

    }

    public Publisher(BundleContext context){
        this.context=context;
    }

    @Validate
    public void start(){
        try {

            http.registerServlet(PUBLISHER_URL,new Publisher(),null,null);

        } catch (Exception e) {

            log.error("Failed to publish Publisher URL",e);

        }
    }

    @Invalidate
    public void stop(){
        http.unregister(PUBLISHER_URL);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Thread.currentThread().setContextClassLoader(SyndFeed.class.getClassLoader());

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("atom_1.0");
        feed.setTitle("Sample Feed (created with ROME)");
        feed.setLink("http://www.example.com");

        List<SyndLink> links=new ArrayList<SyndLink>();

        SyndLinkImpl hubLink=new SyndLinkImpl();
        hubLink.setRel("hub");
        hubLink.setHref("http://localhost:8080/hub/main");

        links.add(hubLink);

        feed.setLinks(links);
        feed.setDescription("This feed has been created using ROME");
        feed.setFeedType("atom_0.3");//rss_2.0

        List entries = new ArrayList();
        SyndEntry entry;
        SyndContent description;

        entry = new SyndEntryImpl();
        entry.setTitle("ROME v1.0");
        entry.setLink("http://wiki.java.net/bin/view/Javawsxml/Rome01");
        entry.setPublishedDate(new Date());
        description = new SyndContentImpl();
        description.setType("text/plain");
        description.setValue("Initial release of ROME");
        entry.setDescription(description);
        entries.add(entry);

        feed.setEntries(entries);

        SyndFeedOutput output = new SyndFeedOutput();

        try {
            output.output(feed,resp.getWriter());
            resp.getWriter().close();
        } catch (FeedException e) {
            log.error("Failed to create feed",e);
        }

        resp.getWriter().write(feed.createWireFeed().toString());

    }

    /*

    public class ServerPublishServlet extends  HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            String url=req.getParameter("url");

            ContentNotification cn=new ContentNotification("publish",url);

            CloseableHttpClient httpclient = HttpClients.createDefault();

            String hub = "http://localhost:8080/hub";

            HttpPost httpPost = new HttpPost(hub);

            httpPost.setEntity(new UrlEncodedFormEntity(cn.toRequesParameters()));

            System.out.println("publisher --> Sending new post to the HUB:"+ hub);

            CloseableHttpResponse response1 = httpclient.execute(httpPost);

            System.out.println("publisher --> got response:"+response1.getStatusLine().getStatusCode());

        }
    }

    */

}
