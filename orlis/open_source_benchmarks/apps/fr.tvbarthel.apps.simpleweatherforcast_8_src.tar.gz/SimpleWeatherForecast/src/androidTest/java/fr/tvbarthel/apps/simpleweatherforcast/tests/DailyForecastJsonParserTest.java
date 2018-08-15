package fr.tvbarthel.apps.simpleweatherforcast.tests;

import android.test.ActivityInstrumentationTestCase2;

import java.util.ArrayList;

import fr.tvbarthel.apps.simpleweatherforcast.MainActivity;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastJsonParser;
import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastModel;


public class DailyForecastJsonParserTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String JSON = "{\"cod\":\"200\",\"message\":0.1003,\"city\":{\"id\":1851632,\"name\":\"Shuzenji\",\"coord\":{\"lon\":138.933334,\"lat\":34.966671},\"country\":\"JP\",\"population\":0},\"cnt\":14,\"list\":[{\"dt\":1404180000,\"temp\":{\"day\":16.45,\"min\":16.45,\"max\":16.45,\"night\":16.45,\"eve\":16.45,\"morn\":16.45},\"pressure\":930.7,\"humidity\":98,\"weather\":[{\"id\":804,\"main\":\"Clouds\",\"description\":\"overcast clouds\",\"icon\":\"04n\"}],\"speed\":0.8,\"deg\":347,\"clouds\":92},{\"dt\":1404266400,\"temp\":{\"day\":21.55,\"min\":16.63,\"max\":21.55,\"night\":16.96,\"eve\":19.33,\"morn\":16.63},\"pressure\":930.53,\"humidity\":76,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.01,\"deg\":162,\"clouds\":92,\"rain\":2.5},{\"dt\":1404352800,\"temp\":{\"day\":20.98,\"min\":14.72,\"max\":20.98,\"night\":14.72,\"eve\":15.98,\"morn\":16.75},\"pressure\":928.43,\"humidity\":75,\"weather\":[{\"id\":502,\"main\":\"Rain\",\"description\":\"heavy intensity rain\",\"icon\":\"10d\"}],\"speed\":0.96,\"deg\":178,\"clouds\":92,\"rain\":17},{\"dt\":1404439200,\"temp\":{\"day\":21.41,\"min\":15.29,\"max\":21.67,\"night\":16.84,\"eve\":18.22,\"morn\":15.29},\"pressure\":922.69,\"humidity\":82,\"weather\":[{\"id\":502,\"main\":\"Rain\",\"description\":\"heavy intensity rain\",\"icon\":\"10d\"}],\"speed\":0.51,\"deg\":316,\"clouds\":24,\"rain\":26},{\"dt\":1404525600,\"temp\":{\"day\":21.13,\"min\":13.23,\"max\":21.13,\"night\":13.23,\"eve\":20.04,\"morn\":16.36},\"pressure\":927.65,\"humidity\":78,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":0.86,\"deg\":68,\"clouds\":56,\"rain\":1},{\"dt\":1404612000,\"temp\":{\"day\":23.08,\"min\":14.38,\"max\":24.08,\"night\":16.02,\"eve\":21.92,\"morn\":14.38},\"pressure\":930.21,\"humidity\":67,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":0.91,\"deg\":79,\"clouds\":0},{\"dt\":1404698400,\"temp\":{\"day\":23.4,\"min\":18.1,\"max\":25.39,\"night\":20.96,\"eve\":25.39,\"morn\":18.1},\"pressure\":970.03,\"humidity\":0,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.36,\"deg\":20,\"clouds\":90,\"rain\":1.03},{\"dt\":1404784800,\"temp\":{\"day\":23.43,\"min\":18.07,\"max\":26.83,\"night\":21.36,\"eve\":26.83,\"morn\":18.07},\"pressure\":967.12,\"humidity\":0,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.36,\"deg\":32,\"clouds\":89,\"rain\":0.93},{\"dt\":1404871200,\"temp\":{\"day\":20.7,\"min\":18.57,\"max\":22.7,\"night\":21.18,\"eve\":22.7,\"morn\":18.57},\"pressure\":966.18,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":1.21,\"deg\":8,\"clouds\":100,\"rain\":11.23},{\"dt\":1404957600,\"temp\":{\"day\":25.86,\"min\":20.38,\"max\":29.33,\"night\":22.3,\"eve\":29.33,\"morn\":20.38},\"pressure\":949.15,\"humidity\":0,\"weather\":[{\"id\":503,\"main\":\"Rain\",\"description\":\"very heavy rain\",\"icon\":\"10d\"}],\"speed\":1.78,\"deg\":297,\"clouds\":11,\"rain\":158.78},{\"dt\":1405044000,\"temp\":{\"day\":19.41,\"min\":19.41,\"max\":25.47,\"night\":20.56,\"eve\":25.47,\"morn\":20.5},\"pressure\":964.14,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":1.03,\"deg\":333,\"clouds\":93,\"rain\":5.32},{\"dt\":1405130400,\"temp\":{\"day\":24.53,\"min\":20.42,\"max\":25.54,\"night\":21.41,\"eve\":25.54,\"morn\":20.42},\"pressure\":965.98,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":1.16,\"deg\":2,\"clouds\":0,\"rain\":5.55},{\"dt\":1405216800,\"temp\":{\"day\":24.89,\"min\":21.16,\"max\":26.11,\"night\":22.59,\"eve\":26.11,\"morn\":21.16},\"pressure\":965.62,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":0.88,\"deg\":20,\"clouds\":30,\"rain\":4.25},{\"dt\":1405303200,\"temp\":{\"day\":23.03,\"min\":19.5,\"max\":25.87,\"night\":22.62,\"eve\":25.87,\"morn\":19.5},\"pressure\":966.47,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":1.18,\"deg\":6,\"clouds\":98,\"rain\":3.07}]}";
    
    private MainActivity mMainActivity;

    public DailyForecastJsonParserTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivity = getActivity();
    }

    public void testSyncParsing() {
        final ArrayList<DailyForecastModel> models = DailyForecastJsonParser.parse(JSON);

        assertNotNull(models);
        assertEquals(14, models.size());
    }

    public void testAsyncParsing() {
        new DailyForecastJsonParser(){
            @Override
            protected void onPostExecute(ArrayList<DailyForecastModel> dailyForecastModels) {
                super.onPostExecute(dailyForecastModels);

                assertNotNull(dailyForecastModels);
                assertEquals(14, dailyForecastModels.size());
            }
        }.execute(JSON);
    }
}
