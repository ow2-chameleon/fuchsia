package org.ow2.chameleon.fuchsia.examples.jaxws.weather;

/**
 * Created with IntelliJ IDEA.
 * User: jeremy
 * Date: 29/08/13
 * Time: 13:05
 * To change this template use File | Settings | File Templates.
 */
public interface WeatherForeCastWS {

    enum XDayPrevision {ONE_DAY, TWO_DAY, THREE_DAY, FOUR_DAY, FIVE_DAY}

    String getActualWeather(String location);

    String getWeatherPrevisions(String location, XDayPrevision prevision);
}
