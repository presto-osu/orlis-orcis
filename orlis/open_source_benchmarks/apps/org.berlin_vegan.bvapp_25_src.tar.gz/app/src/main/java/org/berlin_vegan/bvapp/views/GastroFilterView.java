/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.data.GastroLocationFilter;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.data.Locations;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * custom view for gastro location filtering
 * todo optimized the view hierarchy http://trickyandroid.com/protip-inflating-layout-for-your-custom-view/
 */
public class GastroFilterView extends LinearLayout {

    @Bind(R.id.vegan_checkbox)
    CheckBox veganCheckbox;
    @Bind(R.id.vegetarian_checkbox)
    CheckBox vegetarianCheckbox;
    @Bind(R.id.omnivor_checkbox)
    CheckBox omnivoreCheckbox;
    @Bind(R.id.restaurant_checkbox)
    CheckBox restaurantCheckbox;
    @Bind(R.id.fast_food_checkbox)
    CheckBox fastFoodCheckbox;
    @Bind(R.id.ice_cafe_checkbox)
    CheckBox iceCafeCheckbox;
    @Bind(R.id.cafe_checkbox)
    CheckBox cafeCheckbox;
    @Bind(R.id.organic_checkbox)
    CheckBox organicCheckbox;
    @Bind(R.id.gluten_free_checkbox)
    CheckBox glutenFreeCheckbox;
    @Bind(R.id.hc_accessible_checkbox)
    CheckBox hc_accessibleCheckbox;
    @Bind(R.id.child_chair_checkbox)
    CheckBox childChairCheckbox;
    @Bind(R.id.dog_checkbox)
    CheckBox dogCheckbox;
    @Bind(R.id.wlan_checkbox)
    CheckBox wlanCheckbox;
    @Bind(R.id.delivery_checkbox)
    CheckBox deliveryCheckbox;
    @Bind(R.id.catering_checkbox)
    CheckBox cateringCheckbox;
    @Bind(R.id.result_textview)
    TextView resultTextView;
    private Locations locations;

    public GastroFilterView(Context context) {
        super(context);
        initLayout();
    }

    public GastroFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public GastroFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout();
    }

    private void initLayout() {
        inflate(getContext(), R.layout.gastro_filter, this);
        ButterKnife.bind(this);
    }

    public void init(Locations locations, GastroLocationFilter filter) {
        this.locations = locations;
        veganCheckbox.setChecked(filter.isVegan());
        vegetarianCheckbox.setChecked(filter.isVegetarian());
        omnivoreCheckbox.setChecked(filter.isOmnivore());
        restaurantCheckbox.setChecked(filter.isRestaurant());
        fastFoodCheckbox.setChecked(filter.isFastFood());
        iceCafeCheckbox.setChecked(filter.isIceCafe());
        cafeCheckbox.setChecked(filter.isCafe());
        organicCheckbox.setChecked(filter.isOrganic());
        glutenFreeCheckbox.setChecked(filter.isGlutenFree());
        hc_accessibleCheckbox.setChecked(filter.isHandicappedAccessible());
        childChairCheckbox.setChecked(filter.isChildChair());
        dogCheckbox.setChecked(filter.isDog());
        wlanCheckbox.setChecked(filter.isWlan());
        deliveryCheckbox.setChecked(filter.isDelivery());
        cateringCheckbox.setChecked(filter.isCatering());
        updateResult(filter);
    }

    public GastroLocationFilter getCurrentFilter() {
        final GastroLocationFilter filter = new GastroLocationFilter();
        filter.setVegan(veganCheckbox.isChecked());
        filter.setVegetarian(vegetarianCheckbox.isChecked());
        filter.setOmnivore(omnivoreCheckbox.isChecked());
        filter.setRestaurant(restaurantCheckbox.isChecked());
        filter.setFastFood(fastFoodCheckbox.isChecked());
        filter.setIceCafe(iceCafeCheckbox.isChecked());
        filter.setCafe(cafeCheckbox.isChecked());
        filter.setOrganic(organicCheckbox.isChecked());
        filter.setGlutenFree(glutenFreeCheckbox.isChecked());
        filter.setHandicappedAccessible(hc_accessibleCheckbox.isChecked());
        filter.setChildChair(childChairCheckbox.isChecked());
        filter.setDog(dogCheckbox.isChecked());
        filter.setWlan(wlanCheckbox.isChecked());
        filter.setDelivery(deliveryCheckbox.isChecked());
        filter.setCatering(cateringCheckbox.isChecked());
        return filter;
    }

    @OnClick({R.id.vegan_checkbox, R.id.vegetarian_checkbox, R.id.omnivor_checkbox, R.id.restaurant_checkbox, R.id.fast_food_checkbox,
            R.id.ice_cafe_checkbox, R.id.cafe_checkbox, R.id.organic_checkbox, R.id.gluten_free_checkbox, R.id.hc_accessible_checkbox,
            R.id.child_chair_checkbox, R.id.dog_checkbox, R.id.wlan_checkbox, R.id.delivery_checkbox, R.id.catering_checkbox})
    public void onFilterOptionChanged() {
        final GastroLocationFilter filter = getCurrentFilter();
        updateResult(filter);
    }

    // todo optimize, we need only size
    private void updateResult(GastroLocationFilter filter) {
        final List<Location> result = this.locations.getFilterResult(filter);
        resultTextView.setText(getContext().getString(R.string.gastro_filter_result, result.size()));
    }
}
