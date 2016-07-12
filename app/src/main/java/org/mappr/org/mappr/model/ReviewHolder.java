package org.mappr.org.mappr.model;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.json.JSONObject;

/**
 * Created by codeyasam on 6/23/16.
 */
public class ReviewHolder {

    private float rating;
    private String comment;
    private String submitDate;
    private MapprEndUser endUser;

    public static ReviewHolder instantiateJSONReview(JSONObject userObj, JSONObject reviewObj) {
        try {
            ReviewHolder rh = new ReviewHolder();
            rh.setEndUser(MapprEndUser.instantiateJSONUser(userObj));
            rh.setRating(Float.parseFloat(reviewObj.getString("rating")));
            rh.setComment(reviewObj.getString("comment"));
            rh.setSubmitDate(reviewObj.getString("submit_date"));

            return rh;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MapprEndUser getEndUser() {
        return endUser;
    }

    public void setEndUser(MapprEndUser endUser) {
        this.endUser = endUser;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    private String manageLetterS(int qty, String name) {
        String prompt = qty + " " + name;
        if (qty > 1) {
            prompt += "s";
        }
        return prompt + " ago";
    }

    public String getPassedTime() {
        try {
            DateTime date = new DateTime(Integer.parseInt(submitDate) * 1000L);
            String passedTime = "";
            System.out.println(date);
            int days = Days.daysBetween(date, new DateTime()).getDays();
            System.out.println(days);
            if (Years.yearsBetween(date, new DateTime()).getYears() > 0) {
                passedTime = manageLetterS(Weeks.weeksBetween(date, new DateTime()).getWeeks(), "year");
            } else if (Months.monthsBetween(date, new DateTime()).getMonths() > 0) {
                passedTime = manageLetterS(Months.monthsBetween(date, new DateTime()).getMonths(), "month");
            } else if (Weeks.weeksBetween(date, new DateTime()).getWeeks() > 0) {
                passedTime = manageLetterS(Weeks.weeksBetween(date, new DateTime()).getWeeks(),  "week");
            } else if (Days.daysBetween(date, new DateTime()).getDays() > 0) {
                passedTime = manageLetterS(Days.daysBetween(date, new DateTime()).getDays(), "day");
                System.out.println("poop");
            } else if (Hours.hoursBetween(date, new DateTime()).getHours() > 0) {
                passedTime = manageLetterS(Hours.hoursBetween(date, new DateTime()).getHours(), "hour");
            } else if (Minutes.minutesBetween(date, new DateTime()).getMinutes() > 0) {
                passedTime = manageLetterS(Minutes.minutesBetween(date, new DateTime()).getMinutes(), "minute");
            } else if (Seconds.secondsBetween(date, new DateTime()).getSeconds() > 0) {
                passedTime = manageLetterS(Seconds.secondsBetween(date, new DateTime()).getSeconds(), "second");
            }
            //System.out.println("poop");
            return passedTime;
        } catch (Exception e) {
            e.getMessage();
        }

        return null;
    }
}
