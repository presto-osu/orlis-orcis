/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */

package activities;

import android.content.Context;

import org.domogik.domodroid13.R;


public class Graphics_Manager {


    public static int Icones_Agent(String usage, int state) {
        usage = adapt_usage(usage);
        switch (state) {
            case 0: //Called for Off or Room
                //reorder by usage name for easy update
                switch (usage) {
                    case "air_conditionning":
                        return R.drawable.usage_air_off;
                    case "appliance":
                        return R.drawable.usage_appliance_off;
                    case "battery":
                        return R.drawable.usage_battery_off;
                    case "bluetooth":
                        return R.drawable.usage_bluetooth_off;
                    case "christmas_tree":
                        return R.drawable.usage_christmas_tree_off;
                    case "computer":
                        return R.drawable.usage_computer_off;
                    case "cron":
                        return R.drawable.usage_cron_off;
                    case "door":
                        return R.drawable.usage_door_off;
                    case "electricity":
                        return R.drawable.usage_electricity_off;
                    case "heating":
                        return R.drawable.usage_heating_off;
                    case "light":
                        return R.drawable.usage_light_off;
                    case "mirror":
                        return R.drawable.usage_mirror_off;
                    case "motion":
                        return R.drawable.usage_motion_off;
                    case "music":
                        return R.drawable.usage_music_off;
                    case "nanoztag":
                        return R.drawable.usage_nanoztag_off;
                    case "portal":
                        return R.drawable.usage_portal_off;
                    case "scene":
                        return R.drawable.usage_scene_off;
                    case "security_camera":
                        return R.drawable.usage_security_cam_off;
                    case "server":
                        return R.drawable.usage_server_off;
                    case "socket":
                        return R.drawable.usage_appliance_off;
                    case "shutter":
                        return R.drawable.usage_shutter_off;
                    case "telephony":
                        return R.drawable.usage_telephony_off;
                    case "temperature":
                        return R.drawable.usage_temperature_off;
                    case "tv":
                        return R.drawable.usage_tv_off;
                    case "ventilation":
                        return R.drawable.usage_ventilation_off;
                    case "water":
                        return R.drawable.usage_water_off;
                    case "water_tank":
                        return R.drawable.usage_water_tank_off;
                    case "window":
                        return R.drawable.usage_window_off;

                    //room
                    case "bathroom":
                        return R.drawable.room_bathroom;
                    case "bedroom":
                        return R.drawable.room_bedroom;
                    case "garage":
                        return R.drawable.room_garage;
                    case "kidsroom":
                        return R.drawable.room_kidsroom;
                    case "kitchen":
                        return R.drawable.room_kitchen;
                    case "office":
                        return R.drawable.room_office;
                    case "tvlounge":
                        return R.drawable.room_tvlounge;
                    case "usage":
                        return R.drawable.logo;
                    case "unknow":
                        return R.drawable.unknown;

                    //area
                    case "area":
                        return R.drawable.area_area;
                    case "attic":
                        return R.drawable.area_attic;
                    case "basement":
                        return R.drawable.area_basement;
                    case "basement2":
                        return R.drawable.area_basement2;
                    case "garden":
                        return R.drawable.area_garden;
                    case "ground_floor":
                        return R.drawable.area_ground_floor;
                    case "ground_floor2":
                        return R.drawable.area_ground_floor2;
                    case "first_floor":
                        return R.drawable.area_first_floor;
                    case "first_floor2":
                        return R.drawable.area_first_floor2;
                    case "second_floor":
                        return R.drawable.area_second_floor;
                    case "second_floor2":
                        return R.drawable.area_second_floor2;
                    case "house":
                        return R.drawable.house;
                    case "map":
                        return R.drawable.map;
                    default:
                        return R.drawable.usage_default_off;
                }

            case 1: //For median value (50%)
                //reorder by usage name for easy update
                switch (usage) {
                    case "air_conditionning":
                        return R.drawable.usage_air_50;
                    case "appliance":
                        return R.drawable.usage_appliance_50;
                    case "battery":
                        return R.drawable.usage_battery_50;
                    //todo add bluetooth 50
                    case "christmas_tree":
                        return R.drawable.usage_christmas_tree_50;
                    case "computer":
                        return R.drawable.usage_computer_50;
                    case "cron":
                        return R.drawable.usage_cron_50;
                    case "door":
                        return R.drawable.usage_door_50;
                    case "electricity":
                        return R.drawable.usage_electricity_50;
                    case "heating":
                        return R.drawable.usage_heating_50;
                    case "light":
                        return R.drawable.usage_light_50;
                    case "mirror":
                        return R.drawable.usage_mirror_50;
                    case "music":
                        return R.drawable.usage_music_50;
                    case "nanoztag":
                        return R.drawable.usage_nanoztag_50;
                    case "portal":
                        return R.drawable.usage_portal_50;
                    case "scene":
                        return R.drawable.usage_scene_50;
                    case "security_camera":
                        return R.drawable.usage_security_cam_50;
                    case "server":
                        return R.drawable.usage_server_50;
                    case "socket":
                        return R.drawable.usage_appliance_50;
                    case "shutter":
                        return R.drawable.usage_shutter_50;
                    case "telephony":
                        return R.drawable.usage_telephony_50;
                    case "temperature":
                        return R.drawable.usage_temperature_50;
                    case "tv":
                        return R.drawable.usage_tv_50;
                    case "ventilation":
                        return R.drawable.usage_ventilation_50;
                    case "water":
                        return R.drawable.usage_water_50;
                    case "water_tank":
                        return R.drawable.usage_water_tank_50;
                    case "window":
                        return R.drawable.usage_window_50;

                    //room
                    case "bathroom":
                        return R.drawable.room_bathroom;
                    case "bedroom":
                        return R.drawable.room_bedroom;
                    case "garage":
                        return R.drawable.room_garage;
                    case "kidsroom":
                        return R.drawable.room_kidsroom;
                    case "kitchen":
                        return R.drawable.room_kitchen;
                    case "office":
                        return R.drawable.room_office;
                    case "tvlounge":
                        return R.drawable.room_tvlounge;
                    case "usage":
                        return R.drawable.logo;
                    case "unknow":
                        return R.drawable.unknown;

                    //area
                    case "area":
                        return R.drawable.area_area;
                    case "attic":
                        return R.drawable.area_attic;
                    case "basement":
                        return R.drawable.area_basement;
                    case "basement2":
                        return R.drawable.area_basement2;
                    case "garden":
                        return R.drawable.area_garden;
                    case "ground_floor":
                        return R.drawable.area_ground_floor;
                    case "ground_floor2":
                        return R.drawable.area_ground_floor2;
                    case "first_floor":
                        return R.drawable.area_first_floor;
                    case "first_floor2":
                        return R.drawable.area_first_floor2;
                    case "second_floor":
                        return R.drawable.area_second_floor;
                    case "second_floor2":
                        return R.drawable.area_second_floor2;
                    case "house":
                        return R.drawable.house;
                    case "map":
                        return R.drawable.map;
                    default:
                        return R.drawable.usage_default_50;
                }

            case 2: //For on
                //reorder by usage name for easy update
                switch (usage) {
                    case "air_conditionning":
                        return R.drawable.usage_air_on;
                    case "appliance":
                        return R.drawable.usage_appliance_on;
                    case "battery":
                        return R.drawable.usage_battery_on;
                    case "bluetooth":
                        return R.drawable.usage_bluetooth_on;
                    case "christmas_tree":
                        return R.drawable.usage_christmas_tree_on;
                    case "computer":
                        return R.drawable.usage_computer_on;
                    case "cron":
                        return R.drawable.usage_cron_on;
                    case "door":
                        return R.drawable.usage_door_on;
                    case "electricity":
                        return R.drawable.usage_electricity_on;
                    case "heating":
                        return R.drawable.usage_heating_on;
                    case "light":
                        return R.drawable.usage_light_on;
                    case "mirror":
                        return R.drawable.usage_mirror_on;
                    case "motion":
                        return R.drawable.usage_motion_on;
                    case "music":
                        return R.drawable.usage_music_on;
                    case "nanoztag":
                        return R.drawable.usage_nanoztag_on;
                    case "portal":
                        return R.drawable.usage_portal_on;
                    case "scene":
                        return R.drawable.usage_scene_on;
                    case "security_camera":
                        return R.drawable.usage_security_cam_on;
                    case "server":
                        return R.drawable.usage_server_on;
                    case "socket":
                        return R.drawable.usage_appliance_on;
                    case "shutter":
                        return R.drawable.usage_shutter_on;
                    case "telephony":
                        return R.drawable.usage_telephony_on;
                    case "temperature":
                        return R.drawable.usage_temperature_on;
                    case "tv":
                        return R.drawable.usage_tv_on;
                    case "ventilation":
                        return R.drawable.usage_ventilation_on;
                    case "water":
                        return R.drawable.usage_water_on;
                    case "water_tank":
                        return R.drawable.usage_water_tank_on;
                    case "window":
                        return R.drawable.usage_window_on;

                    //room
                    case "bathroom":
                        return R.drawable.room_bathroom;
                    case "bedroom":
                        return R.drawable.room_bedroom;
                    case "garage":
                        return R.drawable.room_garage;
                    case "kidsroom":
                        return R.drawable.room_kidsroom;
                    case "kitchen":
                        return R.drawable.room_kitchen;
                    case "office":
                        return R.drawable.room_office;
                    case "tvlounge":
                        return R.drawable.room_tvlounge;
                    case "usage":
                        return R.drawable.logo;
                    case "unknow":
                        return R.drawable.unknown;

                    //area
                    case "area":
                        return R.drawable.area_area;
                    case "attic":
                        return R.drawable.area_attic;
                    case "basement":
                        return R.drawable.area_basement;
                    case "basement2":
                        return R.drawable.area_basement2;
                    case "garden":
                        return R.drawable.area_garden;
                    case "ground_floor":
                        return R.drawable.area_ground_floor;
                    case "ground_floor2":
                        return R.drawable.area_ground_floor2;
                    case "first_floor":
                        return R.drawable.area_first_floor;
                    case "first_floor2":
                        return R.drawable.area_first_floor2;
                    case "second_floor":
                        return R.drawable.area_second_floor;
                    case "second_floor2":
                        return R.drawable.area_second_floor2;
                    case "house":
                        return R.drawable.house;
                    case "map":
                        return R.drawable.map;
                    default:
                        return R.drawable.usage_default_on;
                }

            case 3: //For undefined
                //reorder by usage name for easy update
                switch (usage) {
                    case "air_conditionning":
                        return R.drawable.usage_air_undefined;
                    case "appliance":
                        return R.drawable.usage_appliance_undefined;
                    case "battery":
                        return R.drawable.usage_battery_undefnied;
                    case "bluetooth":
                        return R.drawable.usage_bluetooth_undefined;
                    case "christmas_tree":
                        return R.drawable.usage_christmas_tree_undefined;
                    case "computer":
                        return R.drawable.usage_computer_undefined;
                    case "cron":
                        return R.drawable.usage_cron_undefined;
                    case "door":
                        return R.drawable.usage_door_undefined;
                    case "electricity":
                        return R.drawable.usage_electricity_undefined;
                    case "heating":
                        return R.drawable.usage_heating_undefined;
                    case "light":
                        return R.drawable.usage_light_undefined;
                    case "mirror":
                        return R.drawable.usage_mirror_undefined;
                    case "music":
                        return R.drawable.usage_music_undefined;
                    case "nanoztag":
                        return R.drawable.usage_nanoztag_undefined;
                    case "portal":
                        return R.drawable.usage_portal_undefined;
                    case "scene":
                        return R.drawable.usage_scene_undefined;
                    case "security_camera":
                        return R.drawable.usage_security_cam_undefined;
                    case "server":
                        return R.drawable.usage_server_undefined;
                    case "socket":
                        return R.drawable.usage_appliance_undefined;
                    case "shutter":
                        return R.drawable.usage_shutter_undefined;
                    case "telephony":
                        return R.drawable.usage_telephony_undefined;
                    case "temperature":
                        return R.drawable.usage_temperature_undefined;
                    case "tv":
                        return R.drawable.usage_tv_undefined;
                    case "ventilation":
                        return R.drawable.usage_ventilation_undefined;
                    case "water":
                        return R.drawable.usage_water_undefined;
                    case "water_tank":
                        return R.drawable.usage_water_tank_undefined;
                    case "window":
                        return R.drawable.usage_window_undefined;

                    //room
                    case "bathroom":
                        return R.drawable.room_bathroom;
                    case "bedroom":
                        return R.drawable.room_bedroom;
                    case "garage":
                        return R.drawable.room_garage;
                    case "kidsroom":
                        return R.drawable.room_kidsroom;
                    case "kitchen":
                        return R.drawable.room_kitchen;
                    case "office":
                        return R.drawable.room_office;
                    case "tvlounge":
                        return R.drawable.room_tvlounge;
                    case "usage":
                        return R.drawable.logo;
                    case "unknow":
                        return R.drawable.unknown;

                    //area
                    case "area":
                        return R.drawable.area_area;
                    case "attic":
                        return R.drawable.area_attic;
                    case "basement":
                        return R.drawable.area_basement;
                    case "basement2":
                        return R.drawable.area_basement2;
                    case "garden":
                        return R.drawable.area_garden;
                    case "ground_floor":
                        return R.drawable.area_ground_floor;
                    case "ground_floor2":
                        return R.drawable.area_ground_floor2;
                    case "first_floor":
                        return R.drawable.area_first_floor;
                    case "first_floor2":
                        return R.drawable.area_first_floor2;
                    case "second_floor":
                        return R.drawable.area_second_floor;
                    case "second_floor2":
                        return R.drawable.area_second_floor2;
                    case "house":
                        return R.drawable.house;
                    case "map":
                        return R.drawable.map;
                    default:
                        return R.drawable.usage_default_undefined;
                }
        }
        return R.drawable.icon;
    }


    public static String Names_Agent(Context context, String usage) {
        //Use to translate value in current language
        //For example if an a room is named kitchen,
        //in French The text display will be cuisine
        int resId;
        String result = usage;
        String packageName = context.getPackageName();
        String search = usage;
        if (search.equals("air conditioning"))
            search = "conditioning";

        resId = context.getResources().getIdentifier(search, "string", packageName);
        if (resId != 0) {
            try {
                result = context.getString(resId);
            } catch (Exception e) {
                result = usage;
            }
        }
        return result;
    }

    public static int Names_conditioncodes(Context context, int code) {
        //used to get the translate text from yahoo weather infocode.
        try {
            //wi_yahoo_
            return context.getResources().getIdentifier("wi_yahoo_" + code, "string", context.getPackageName());
            //return context.getResources().getIdentifier("info" + code, "string", context.getPackageName());
        } catch (Exception e) {
            return R.string.info48;
        }
    }

    public static int Map_Agent(String usage, int state) {
        usage = adapt_usage(usage);
        switch (state) {
            case 0:
                //reorder by usage name for easy update
            /*
             * TODO add missing usage
			 * air_conditionning
			 * heating
			 * mirror
			 * motion
			 * music
			 * nanoztag
			 * portal
			 * scene
			 * shutter
			 * water
			 * window
			 */
            /*
            try{
				return context.getResources().getIdentifier("map_usage_"+usage+"_off", "drawable", context.getPackageName());
			} catch (Exception e){
				return R.drawable.map_led_off;
			}
			 */
                switch (usage) {
                    case "appliance":
                        return R.drawable.map_usage_appliance_off;
                    case "battery":
                        return R.drawable.map_usage_battery_off;
                    case "christmas_tree":
                        return R.drawable.map_usage_christmas_tree_off;
                    case "computer":
                        return R.drawable.map_usage_computer_off;
                    case "cron":
                        return R.drawable.map_usage_cron_off;
                    case "door":
                        return R.drawable.map_usage_door_off;
                    case "electricity":
                        return R.drawable.map_usage_electricity_off;
                    case "light":
                        return R.drawable.map_usage_light_off;
                    case "security_camera":
                        return R.drawable.map_usage_security_cam_off;
                    case "server":
                        return R.drawable.map_usage_server_off;
                    case "socket":
                        return R.drawable.map_usage_appliance_off;
                    case "telephony":
                        return R.drawable.map_usage_telephony_off;
                    case "temperature":
                        return R.drawable.map_usage_temperature;
                    case "tv":
                        return R.drawable.map_usage_tv_off;
                    case "ventilation":
                        return R.drawable.map_usage_ventilation_off;
                    case "water_tank":
                        return R.drawable.map_usage_water_tank;
                    default:
                        return R.drawable.map_led_off;
                }

            case 1:
                //reorder by usage name for easy update
                switch (usage) {
                    case "appliance":
                        return R.drawable.map_usage_appliance_on;
                    case "battery":
                        return R.drawable.map_usage_battery_on;
                    case "christmas_tree":
                        return R.drawable.map_usage_christmas_tree_on;
                    case "computer":
                        return R.drawable.map_usage_computer_on;
                    case "cron":
                        return R.drawable.map_usage_cron_on;
                    case "door":
                        return R.drawable.map_usage_door_on;
                    case "electricity":
                        return R.drawable.map_usage_electricity_on;
                    case "light":
                        return R.drawable.map_usage_light_on;
                    case "security_camera":
                        return R.drawable.map_usage_security_cam_on;
                    case "server":
                        return R.drawable.map_usage_server_on;
                    case "socket":
                        return R.drawable.map_usage_appliance_on;
                    case "telephony":
                        return R.drawable.map_usage_telephony_off;
                    case "temperature":
                        return R.drawable.map_usage_temperature;
                    case "tv":
                        return R.drawable.map_usage_tv_on;
                    case "ventilation":
                        return R.drawable.map_usage_ventilation_on;
                    case "water_tank":
                        return R.drawable.map_usage_water_tank;
                    default:
                        return R.drawable.map_led_on;
                }
        }
        return R.drawable.map_led_off;
    }

    public static int getStringIdentifier(Context context, String name) {
        //To avoid space or - in name in strings.xml
        name = name.replace(" ", "_");
        name = name.replace("-", "_");
        name = name.replace(":", "_");
        name = name.replace("(", "");
        name = name.replace(")", "");
        //To get a drawable R.Drawable
        //context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        //To get a string from R.String
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }

    private static String adapt_usage(String usage) {
        if (usage != null) {
            //todo adapt for 0.4 and + use final dt_type (open_close) for example to simplify.

            //Dt_type are in table table_feature in column device_feature_model_id before first "."
            //information are in json device_types sensor and command name of each plugin
            //So a lot of things to look at
            //BLUEZ "available"
            if (usage.toLowerCase().equals("available") || usage.contains("bluez"))
                usage = "bluetooth";
            //CID "callerid", "name", "blacklisted", "blacklist"
            if (usage.contains("callerid"))
                usage = "telephony";
            //DAIKCODE "set_power", "set_setpoint", "set_mode", "set_vertical_swing", "set_horizontal_swing",
            //"set_speedfan", "set_powerfull", "set_silent", "set_home_leave", "set_sensor",
            //"set_start_time", "set_stop_time", "power", "vertical_swing", "horizontal_swing", "powerfull"
            //"silent", "home_leave", "sensor", "setpoint", "setmode", "speedfan", "starttime", "stoptime"
            if (usage.contains("swing") || usage.contains("fan"))
                usage = "ventilation";
            //DISKFREE "get_total_space", "get_percent_used", "get_free_space", "get_used_space"
            if (usage.contains("diskfree") || usage.toLowerCase().equals("get_total_space") || usage.toLowerCase().equals("get_percent_used")
                    || usage.toLowerCase().equals("get_free_space") || usage.toLowerCase().equals("get_used_space"))
                usage = "server";
            //GENERIC "temperature", "humidity", "rgb_color", "rgb_command", "osd_command", "osd_text", "osd_row", "osd_column", "osd_delay"
            // "set_rgb_color", "send_text", "pressure", "co2", "tvoc", "insolation",
            //GEOLOC "position_degrees"
            //IPX800 "state", "input", "count"
            //IRTRANS "send_bintimings", "send_raw", "send_hexa", "code_ir","ack_ir_cmd"
            //K8056 "sensor_switch_relay", "cmd_switch_relay"
            //KARTOTZ "send_msg", "wakeup", "sleep", "left_ear", "right_ear", "msg_status", "error_send"
            //MIRROR
            if (usage.toLowerCase().equals("present"))
                usage = "nanoztag";
            if (usage.toLowerCase().equals("activated"))
                usage = "mirror";
            //MQTT "sensor_temperature", "sensor_humidity", "sensor_battery", "sensor_luminosity","sensor_pressure"
            //"sensor_power", "sensor_energy", "sensor_water", "sensor_count", "sensor_uv", "sensor_windspeed"
            //"sensor_rainfall", "sensor_outflow", "sensor_voltage", "sensor_current", "command_mqtt_onoff",
            // "command_mqtt_string", "sensor_mqtt_temperature", "sensor_mqtt_humidity", "sensor_mqtt_number",
            // "sensor_mqtt_onoff", "sensor_mqtt_openclose", "sensor_mqtt_string"
            if (usage.toLowerCase().equals("sensor_battery"))
                usage = "battery";
            if (usage.contains("power") || usage.contains("energy") || usage.contains("voltage") || usage.toLowerCase().equals("sensor_current"))
                usage = "electricity";
            if (usage.toLowerCase().equals("sensor_rainfall") || usage.toLowerCase().equals("sensor_water"))
                usage = "water";
            //NABAZTAG "send_msg", "wakeup", "sleep", "left_ear", "right_ear", "msg_status", "error_send"
            //NOTIFY "msg_status", "error_send"
            //NUTSERVE "test_battery_start", "test_battery_start_deep", "ups_status", "ups_event", "input_voltage", "output_voltage"
            //"battery_voltage", "battery_charge", "ack_command",
            //ONEWIRE "temperature", "humidity", "serial", "gpio",
            if (usage.toLowerCase().equals("test_battery_start") || usage.toLowerCase().equals("test_battery_start_deep") || usage.toLowerCase().equals("battery_voltage") || usage.toLowerCase().equals("battery_charge"))
                usage = "battery";
            if (usage.contains("thermometer"))
                usage = "temperature";
            //ONEWIRED "1-wire_cmd_output, "1-wire temperature", "1-wire humidity", "1-wire luminosity", "1-wire pressure",
            // "1-wire voltage", "1-wire counter", "1-wire counter diff", "1-wire input", "1-wire output"
            // "onewire output switch cmd", "onewire temperature", "onewire humidity", "onewire luminosity", "onewire pressure",
            // "onewire voltage", "onewire counter", "onewire counter diff", "onewire input switch", "onewire input openclose",
            // "onewire output switch",
            //PING "ping"
            if (usage.contains("ping"))
                usage = "computer";
            //PLCBUS "set_level_bin", "level_bin"
            //RFXBNZ "sensor_temperature", "sensor_humidity", "sensor_battery", "sensor_luminosity", "sensor_pressure", "sensor_power"
            // "sensor_energy", "sensor_water", "sensor_count", "sensor_uv", "sensor_windspeed", "sensor_rainfall", "sensor_outflow"
            // "sensor_voltage", "sensor_current", "sensor_homeeasy_switch", "sensor_homeeasy_openclose", "sensor_x10_switch"
            // "sensor_ati_remote", "sensor_generic"
            //SCRIPT "sensor_script_action", "sensor_script_onoff", "sensor_script_info_number","sensor_script_info_temperature",
            // "sensor_script_info_humidity", "sensor_script_info_binary", "sensor_script_info", "sensor_script_info_switch",
            // "sensor_script_info_openclose", "sensor_script_info_string",
            // "run_script_info", "cmd_script_action", "cmd_script_onoff"
            if (usage.contains("openclose"))
                usage = "door";
            if (usage.contains("script"))
                usage = "scene";
            //TELEINFO "adco", "optarif", "isousc", "base", "iinst", "imax", "motdetat", "hchc", "hchp"
            //"ptec", "papp", "hhphc", "iinst1", "iinst2", "iinst3", "imax1", "imax2", "imax3", "adps"
            //"ejphn", "ejphpm", "pejp", "bbrhcjb", "bbrhpjb", "bbrhcjw", "bbrhpjw", "bbrhcjr", "bbrhpjr"
            if (usage.contains("teleinfo") || usage.toLowerCase().equals("adco") || usage.toLowerCase().equals("optarif") || usage.toLowerCase().equals("isousc") || usage.toLowerCase().equals("base")
                    || usage.toLowerCase().equals("iinst") || usage.toLowerCase().equals("imax") || usage.toLowerCase().equals("motdetat")
                    || usage.toLowerCase().equals("hchc") || usage.toLowerCase().equals("hchp") || usage.toLowerCase().equals("ptec")
                    || usage.toLowerCase().equals("papp") || usage.toLowerCase().equals("hhphc") || usage.toLowerCase().equals("iinst1")
                    || usage.toLowerCase().equals("iinst2") || usage.toLowerCase().equals("iinst3") || usage.toLowerCase().equals("imax1")
                    || usage.toLowerCase().equals("imax2") || usage.toLowerCase().equals("imax3") || usage.toLowerCase().equals("adps")
                    || usage.toLowerCase().equals("ejphn") || usage.toLowerCase().equals("ejphpm") || usage.toLowerCase().equals("pejp")
                    || usage.toLowerCase().equals("bbrhcjb") || usage.toLowerCase().equals("bbrhpjb") || usage.toLowerCase().equals("bbrhcjw")
                    || usage.toLowerCase().equals("bbrhpjw") || usage.toLowerCase().equals("bbrhcjr") || usage.toLowerCase().equals("bbrhpjr")
                    )
                usage = "electricity";
            //RFXBNZ "temperature", "humidity", "battery", "luminosity", "pressure", "power", "energy", "water", "windspeed", "rainfall", "outflow",
            // "voltage", "current", "homeeasy S_switch", "homeeasy_openclose", "x10_switch", "ati_remote", "generic"
            //RFXCOM "temperature", "humidity", "battery", "rssi", "switch_lighting_2", "rssi_lighting_2","open_close", "rssi_open_close", "smoke"
            if (usage.toLowerCase().equals("battery"))
                usage = "battery";
            if (usage.contains("lighting"))
                usage = "light";
            if (usage.contains("open_close"))
                usage = "door";
            //VDEVICE "set_virtual_number", "set_virtual_binary", "set_virtual_string","set_virtual_temperature",
            // "set_virtual_humidity", "set_virtual_percent", "set_virtual_switch", "set_virtual_openclose",
            // "set_virtual_startstop", sensor are same as commands that's easier for this part.
            if (usage.contains("video"))
                usage = "security_camera";
            //VELBUS "level_bin", "level_range", "temp", "power", "energy", "input", "set_level_range", "set_level_bin"
            if (usage.toLowerCase().equals("temp"))
                usage = "temperature";
            //WEATHER "current_barometer_value", "current_feels_like", "current_humidity", "current_last_updated", "current_station", "current_temperature"
            //"current_text", "current_code", "current_visibility","current_wind_direction", "current_wind_speed", "current_sunrise", "current_sunset"
            //"forecast_0_day", "forecast_0_temperature_high", "forecast_0_temperature_low", "forecast_0_condition_text", "forecast_0_condition_code"
            //"forecast_1_day", "forecast_1_temperature_high", "forecast_1_temperature_low", "forecast_1_condition_text", "forecast_1_condition_code"
            //"forecast_2_day", "forecast_2_temperature_high", "forecast_2_temperature_low", "forecast_2_condition_text", "forecast_2_condition_code"
            //"forecast_3_day", "forecast_3_temperature_high", "forecast_3_temperature_low", "forecast_3_condition_text", "forecast_3_condition_code"
            //"forecast_4_day","forecast_4_temperature_high", "forecast_4_temperature_low","forecast_4_condition_text","forecast_4_condition_code"
            if (usage.toLowerCase().contains("temperature") || usage.toLowerCase().equals("current_feels_like"))
                usage = "temperature";
            if (usage.toLowerCase().contains("humidity"))
                usage = "water";
            //TODO change this on with a sun up and down icon
            if (usage.toLowerCase().contains("current_sunrise") || usage.toLowerCase().equals("current_sunset"))
                usage = "cron";
            //WOL "wol"
            if (usage.toLowerCase().equals("wol"))
                usage = "computer";
            //Yi
            if (usage.toLowerCase().equals("yi"))
                usage = "security_camera";
            if (usage.toLowerCase().contains("motion"))
                usage = "motion";
            // ZWAVE "ctrl_status", "node_status", "power_failure", "switch_state", "tamper_sensor"
            // "energy", "power", "switch_state", "energy", "energy_k", "power", "opening_sensor",
            // "power_applied", "battery_level", "low_battery", "tamper_event", "temperature_c",
            // "temperature-k", "temperature_f", "battery_level", "humidity", "relative_humidity"
            // "level", "motion_sensor_level", "luminance", "sensor_alarm", "thermostat_setpoint"
            // "binary_sensor", "motion_sensor_binary", "status", "power_m", "power_k", "energy_m"
            // "energy_previous_reading", "energy_previous_reading_m", "energy_previous_reading_k"
            // "energy_production_k", "instant_energy_production_k", "total_energy_production_k"
            // "energy_production_today-k", "total_production_time_k", "counter", "indicator_str"
            // "locked_state", "alarm_str_report", "power_management_alarm", "smoke_alarm",
            // "carbonmonoxide_alarm", "carbondioxide_alarm", "heat_alarm", "flood_alarm"
            // "fan_mode", "fan_state", "thermostat_mode", "thermostat_operating_state", "thermostat-setpoint-c"
            // "thermostat-setpoint-f", "thermostat-setpoint-k", "toggle_switch_binary", "increase",
            // "decrease", "bright", "dim",
            if (usage.toLowerCase().equals("battery_level") || usage.toLowerCase().equals("battery-level") || usage.toLowerCase().equals("low_battery") || usage.toLowerCase().equals("low-battery"))
                usage = "battery";
            if (usage.toLowerCase().contains("thermostat"))
                usage = "temperature";
            if (usage.toLowerCase().contains("opening_sensor") || usage.contains("opening.sensor") || usage.contains("opening-sensor") || usage.contains("door-window-sensor"))
                usage = "door";
            return usage;

        }else{
            return "";
        }

    }

}
