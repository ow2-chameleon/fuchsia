package org.ow2.chameleon.fuchsia.examples.jaxws.weather.client;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.fuchsia.examples.jaxws.weather.WeatherForeCastWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 29/08/13
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
@Component
@Instantiate
public class WeatherForeCastWSClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private WeatherForeCastWS m_weatherForeCastWS;

    @Bind(id="weatherWS",optional = true)
    public void bindWeatherService(WeatherForeCastWS weatherForeCastWS, ServiceReference ref) {
        logger.debug("Bind weather service !!!");

        m_weatherForeCastWS = weatherForeCastWS;
        useService(m_weatherForeCastWS);
    }

    @Unbind(id="weatherWS")
    public void unbindWeatherService(WeatherForeCastWS weatherForeCastWS) {
        logger.debug("Unbind helloWS service !!!");
        m_weatherForeCastWS = null;
    }

    @Validate
    public void start() {
        logger.debug("Start HelloWS client !!!");
    }

    @Invalidate
    public void stop() {
        logger.debug("Stop HelloWS client !!!");
        m_weatherForeCastWS = null;
    }

    protected void useService(WeatherForeCastWS weatherForeCastWS) {
        String weather = weatherForeCastWS.getActualWeather("38000");
        logger.info("Weather time from 38000 Grenoble is : " +weather);

        String weather2 = weatherForeCastWS.getWeatherPrevisions("38000", WeatherForeCastWS.XDayPrevision.FOUR_DAY);
        logger.info("Weather 4 days forecast from 38000 Grenoble is : " + weather2);
    }
}
