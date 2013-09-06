package org.ow2.chameleon.fuchsia.examples.jaxws.weather.impl;

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.everest.client.EverestClient;
import org.ow2.chameleon.everest.services.EverestService;
import org.ow2.chameleon.everest.services.IllegalActionOnResourceException;
import org.ow2.chameleon.everest.services.Path;
import org.ow2.chameleon.everest.services.ResourceNotFoundException;
import org.ow2.chameleon.fuchsia.examples.jaxws.weather.WeatherForeCastWS;
import org.ow2.chameleon.syndication.FeedEntry;
import org.ow2.chameleon.syndication.FeedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 29/08/13
 * Time: 13:19
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate
@Provides(specifications = WeatherForeCastWS.class)
public class WeatherForeCastWSImpl implements WeatherForeCastWS{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Requires(optional = false)
    EverestService m_everestService;

    private EverestClient m_everestClient;

    @Requires(optional=true,filter="(org.ow2.chameleon.syndication.feed.url=*)")
    private FeedReader reader;

    @ServiceProperty(name="service.exported.interfaces", value="*")
    private String exportedInterfaces;

    @ServiceProperty(name="service.exported.configs", value="org.apache.cxf.ws")
    private String exportedConfigs;

    @ServiceProperty(name="org.apache.cxf.ws.address", value="http://localhost:9090/Weather?wsdl")
    private String wsAddress;

    public WeatherForeCastWSImpl() {
        m_everestClient = new EverestClient(m_everestService);
    }

    public String getActualWeather(String location) {

        logger.info("Invoking: getActualWeather(" + location + ")");
        location="38000"; //TODO FIX ME overide location
        String location1 = new ZipCodeConverter(location).getWoeid();
        // Create the instance
        try {
            m_everestClient.create(Path.from("/ipojo/factory/org.ow2.chameleon.syndication.rome.reader/null").toString()).with("instance.name","rssReader").with("feed.url", "http://weather.yahooapis.com/forecastrss?w=" + location1 + "&u=c").doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }

        List<FeedEntry> list = reader.getEntries();
        String rssFeed = list.get(0).content();
        String result = parseRssFeed(rssFeed);

        //delete the instance
        try {
            m_everestClient.delete(Path.from("/ipojo/instance/"+"rssReader").toString()).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }
        logger.info("ACTUAL WEATHER : " + result);

        return result;
    }

    public String getWeatherPrevisions(String location, XDayPrevision prevision) {

        logger.info("Invoking: getWeatherPrevisions(" + location);
        prevision=XDayPrevision.FOUR_DAY;
        location="38000";
        String location1 = new ZipCodeConverter(location).getWoeid();
        // Create the instance
        try {
            m_everestClient.create(Path.from("/ipojo/factory/org.ow2.chameleon.syndication.rome.reader/null").toString()).with("instance.name","rssReader").with("feed.url","http://weather.yahooapis.com/forecastrss?w="+location1+"&u=c").doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }

        List<FeedEntry> list = reader.getEntries();
        String rssFeed = list.get(0).content();
        List<String> forecast = parseRssFeedForeCast(rssFeed);

        //delete the instance
        try {
            m_everestClient.delete(Path.from("/ipojo/instance/"+"rssReader").toString()).doIt();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalActionOnResourceException e) {
            e.printStackTrace();
        }

        logger.info("ForeCast : " + forecast.toString());

        switch(prevision) {

            case ONE_DAY :
                return forecast.get(0);
            case TWO_DAY:
                return forecast.get(0)+"\n"+forecast.get(1);
            case THREE_DAY:
                return forecast.get(0)+"\n"+forecast.get(1)+"\n"+forecast.get(2);
            case FOUR_DAY:
                return forecast.get(0)+"\n"+forecast.get(1)+"\n"+forecast.get(2)+"\n"+forecast.get(3);
            case FIVE_DAY:
                return forecast.toString();
            default :
                return forecast.toString();
        }
    }

    /**
     * Parser for actual conditions
     * @param feed
     * @return
     */
    private String parseRssFeed(String feed) {
        String[] result = feed.split("<br />");
        String[] result2 = result[2].split("<BR />");

        return result2[0];
    }

    /**
     * Parser for forecast
     * @param feed
     * @return
     */
    private List<String> parseRssFeedForeCast(String feed) {
        String[] result = feed.split("<br />");
        List<String> returnList = new ArrayList<String>();
        String[] result2 = result[2].split("<BR />");

        returnList.add(result2[3]+"\n");
        returnList.add(result[3]+"\n");
        returnList.add(result[4]+"\n");
        returnList.add(result[5]+"\n");
        returnList.add(result[6]+"\n");

        return returnList;
    }
}
