package org.mappr.org.mappr.model;

import org.json.JSONObject;

/**
 * Created by codeyasam on 9/10/16.
 */
public class ScheduleHolder {

    private static final String[] day_no_string = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private String opening_hour;
    private String closing_hour;
    private String day_no;

    public String prompt;

    public ScheduleHolder(String prompt) {
        this.prompt = prompt;
    }

    public ScheduleHolder(String opening_hour, String closing_hour, String day_no) {
        this.opening_hour = opening_hour;
        this.closing_hour = closing_hour;
        this.day_no = day_no;
    }

    public static ScheduleHolder instantiateJSONSchedule(JSONObject jsonSchedule) {
        try {
            ScheduleHolder sh = new ScheduleHolder(jsonSchedule.getString("opening_hour"),
                    jsonSchedule.getString("closing_hour"),
                    jsonSchedule.getString("day_no"));
            return sh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDay_no() {
        return day_no;
    }

    public void setDay_no(String day_no) {
        this.day_no = day_no;
    }

    public String getClosing_time() {
        return closing_hour;
    }

    public void setClosing_time(String closing_time) {
        this.closing_hour = closing_time;
    }

    public String getOpening_time() {
        return opening_hour;
    }

    public void setOpening_time(String opening_time) {
        this.opening_hour = opening_time;
    }

    private String timeTo12Hour(String mTime) {
        try {
            String[] mTimeArr = mTime.split(":");
            String period = Integer.parseInt(mTimeArr[0]) > 11 ? "PM" : "AM";

            if (Integer.parseInt(mTimeArr[0]) > 12) {
                mTimeArr[0] = String.valueOf(Integer.parseInt(mTimeArr[0]) - 12);
            } else if (mTimeArr[0].equals("00")) {
                mTimeArr[0] = String.valueOf(Integer.parseInt(mTimeArr[0]) + 12);
            } else if (Integer.parseInt(mTimeArr[0]) < 12) {
                mTimeArr[0] = String.valueOf(Integer.parseInt(mTimeArr[0]));
            }

            return mTimeArr[0] + ":" + mTimeArr[1] + period;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTimeSelection() {
        String timeSelection = "";
        if (opening_hour.equals(closing_hour)) {
            timeSelection = "CLOSED";
        } else if (opening_hour.equals("00:00:00") && closing_hour.equals("23:59:00")) {
            timeSelection = "24HOURS";
        } else {
            timeSelection = timeTo12Hour(opening_hour) + " - " + timeTo12Hour(closing_hour);
        }
        return timeSelection;
    }

    public String getDayString() {
        return day_no_string[Integer.parseInt(day_no)];
    }
}
