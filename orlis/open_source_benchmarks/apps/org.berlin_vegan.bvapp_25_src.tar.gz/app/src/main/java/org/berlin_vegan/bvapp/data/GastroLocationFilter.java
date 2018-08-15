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


package org.berlin_vegan.bvapp.data;


public class GastroLocationFilter {
    private boolean vegan = true;
    private boolean vegetarian = true;
    private boolean omnivore = true;

    private boolean fastFood = true;
    private boolean restaurant = true;
    private boolean iceCafe = true;
    private boolean cafe = true;

    private boolean dog;
    private boolean childChair;
    private boolean handicappedAccessible;
    private boolean catering;
    private boolean delivery;
    private boolean organic;
    private boolean wlan;
    private boolean glutenFree;

    public boolean isVegan() {
        return vegan;
    }

    public void setVegan(boolean vegan) {
        this.vegan = vegan;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public boolean isOmnivore() {
        return omnivore;
    }

    public void setOmnivore(boolean omnivore) {
        this.omnivore = omnivore;
    }

    public boolean isFastFood() {
        return fastFood;
    }

    public void setFastFood(boolean fastFood) {
        this.fastFood = fastFood;
    }

    public boolean isRestaurant() {
        return restaurant;
    }

    public void setRestaurant(boolean restaurant) {
        this.restaurant = restaurant;
    }

    public boolean isIceCafe() {
        return iceCafe;
    }

    public void setIceCafe(boolean iceCafe) {
        this.iceCafe = iceCafe;
    }

    public boolean isCafe() {
        return cafe;
    }

    public void setCafe(boolean cafe) {
        this.cafe = cafe;
    }

    public boolean isDog() {
        return dog;
    }

    public void setDog(boolean dog) {
        this.dog = dog;
    }

    public boolean isChildChair() {
        return childChair;
    }

    public void setChildChair(boolean childChair) {
        this.childChair = childChair;
    }

    public boolean isCatering() {
        return catering;
    }

    public void setCatering(boolean catering) {
        this.catering = catering;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public boolean isWlan() {
        return wlan;
    }

    public void setWlan(boolean wlan) {
        this.wlan = wlan;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        this.glutenFree = glutenFree;
    }

    public boolean isHandicappedAccessible() {
        return handicappedAccessible;
    }

    public void setHandicappedAccessible(boolean handicappedAccessible) {
        this.handicappedAccessible = handicappedAccessible;
    }

    public boolean matchToFilter(Location location) {
        GastroLocation gastro;
        if (location instanceof GastroLocation) {
            gastro = (GastroLocation) location;
        } else {
            return false;
        }

        boolean match = true;
        // check vegan state
        if (!matchVeganState(gastro)) {
            match = false;
        }
        // check location type
        if (!matchGastroType(gastro)) {
            match = false;
        }
        // check additional option
        if (dog && gastro.getDog() != 1) {
            match = false;
        }
        if (childChair && gastro.getChildChair() != 1) {
            match = false;
        }

        if (handicappedAccessible && gastro.getHandicappedAccessible() != 1) {
            match = false;
        }
        if (catering && gastro.getCatering() != 1) {
            match = false;
        }
        if (delivery && gastro.getDelivery() != 1) {
            match = false;
        }
        if (organic && gastro.getOrganic() != 1) {
            match = false;
        }
        if (wlan && gastro.getWlan() != 1) {
            match = false;
        }
        if (glutenFree && gastro.getGlutenFree() != 1) {
            match = false;
        }

        return match;
    }

    private boolean matchVeganState(GastroLocation gastro) {
        return vegan && gastro.getVegan() == GastroLocation.VEGAN
                || vegetarian && gastro.getVegan() == GastroLocation.VEGETARIAN_VEGAN_DECLARED
                || omnivore && gastro.getVegan() == GastroLocation.OMNIVORE_VEGAN_DECLARED;
    }

    private boolean matchGastroType(GastroLocation gastro) {
        return restaurant && gastro.isRestaurant()
                || fastFood && gastro.isFastFood()
                || iceCafe && gastro.isIceCafe()
                || cafe && gastro.isCafe();
    }
}
