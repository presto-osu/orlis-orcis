package im.r_c.android.clearweather.model;

import java.io.Serializable;

/**
 * ClearWeather
 * Created by richard on 16/5/2.
 */
public class County implements Serializable {
    public static final String KEY_NAME = "name";
    public static final String KEY_NAME_EN = "name_en";
    public static final String KEY_CITY = "city";
    public static final String KEY_PROVINCE = "province";
    public static final String KEY_CODE = "code";

    private String name;

    private String nameEn;

    private String city;

    private String province;

    private String code;

    public County() {
    }

    public County(String name, String nameEn, String city, String province, String code) {
        this.name = name;
        this.nameEn = nameEn;
        this.city = city;
        this.province = province;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "County{" +
                "name='" + name + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        County county = (County) o;

        if (name != null ? !name.equals(county.name) : county.name != null) return false;
        if (nameEn != null ? !nameEn.equals(county.nameEn) : county.nameEn != null) return false;
        if (city != null ? !city.equals(county.city) : county.city != null) return false;
        if (province != null ? !province.equals(county.province) : county.province != null)
            return false;
        return code != null ? code.equals(county.code) : county.code == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (nameEn != null ? nameEn.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }
}
