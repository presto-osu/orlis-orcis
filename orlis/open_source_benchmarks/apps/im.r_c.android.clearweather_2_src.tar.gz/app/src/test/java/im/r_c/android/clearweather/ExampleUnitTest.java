package im.r_c.android.clearweather;

import org.junit.Test;

import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.model.WeatherInfo;
import im.r_c.android.clearweather.util.WeatherInfoFetcher;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testFetchingWeatherInfo() throws Exception {
        County county = new County();
        county.setCode("CN101190201");
        WeatherInfo info = WeatherInfoFetcher.fetch(county);
        assert info != null;
        System.out.println(info.toString());
    }
}
