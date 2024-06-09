package de.t2consult.atlassian.jira.recyclebin;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

        public static Date date(int year, int month, int day) {
            Calendar working = GregorianCalendar.getInstance();
            working.set(year, month-1, day, 0, 0, 0);
            return working.getTime();
        }

}
