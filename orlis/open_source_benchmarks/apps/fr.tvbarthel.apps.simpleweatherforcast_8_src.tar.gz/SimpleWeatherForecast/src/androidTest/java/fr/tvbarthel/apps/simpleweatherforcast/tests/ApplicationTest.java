package fr.tvbarthel.apps.simpleweatherforcast.tests;

import android.app.Application;
import android.os.Bundle;
import android.os.Parcel;
import android.test.ApplicationTestCase;

import fr.tvbarthel.apps.simpleweatherforcast.openweathermap.DailyForecastModel;

public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    public void testDailyForecastModel() {
        final DailyForecastModel model = new DailyForecastModel();
        model.setDateTime(1404154265017l);
        model.setDescription("description");
        model.setHumidity(10);
        model.setMaxTemperature(20d);
        model.setMinTemperature(10d);
        model.setTemperature(15d);

        final Bundle bundleIn = new Bundle();
        final Parcel parcel = Parcel.obtain();
        bundleIn.putParcelable("test", model);
        bundleIn.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        final Bundle bundleOut = parcel.readBundle();
        bundleOut.setClassLoader(DailyForecastModel.class.getClassLoader());
        final DailyForecastModel modelFromBundle = bundleOut.getParcelable("test");

        assertNotNull(modelFromBundle);
        assertNotSame(model, modelFromBundle);
        assertEquals(model.getDateTime(), modelFromBundle.getDateTime());
        assertEquals(model.getDescription(), modelFromBundle.getDescription());
        assertEquals(model.getHumidity(), modelFromBundle.getHumidity());
        assertEquals(model.getMaxTemperature(), modelFromBundle.getMaxTemperature());
        assertEquals(model.getMinTemperature(), modelFromBundle.getMinTemperature());
        assertEquals(model.getTemperature(), modelFromBundle.getTemperature());
    }

}
