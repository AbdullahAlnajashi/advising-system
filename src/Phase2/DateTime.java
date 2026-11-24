package Phase2;

public class DateTime implements IDateTime {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public DateTime(int year,
                    int month,
                    int day,
                    int hour,
                    int minute) {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException("month must be 1-12: " + month);
        if (day < 1 || day > daysInMonth(year, month))
            throw new IllegalArgumentException("invalid day " + day + " for month " + month);
        if (hour < 0 || hour > 23)
            throw new IllegalArgumentException("hour must be 0-23: " + hour);
        if (minute < 0 || minute > 59)
            throw new IllegalArgumentException("minute must be 0-59: " + minute);


        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getDay() {
        return day;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public String format() {

        return String.format(java.util.Locale.ROOT, "%02d/%02d/%04d %02d:%02d", month, day, year, hour, minute);
    }

    @Override
    public int compareTo(IDateTime other) {

        if (year != other.getYear())
            return Integer.compare(year, other.getYear());

        if (month != other.getMonth())
            return Integer.compare(month, other.getMonth());

        if (day != other.getDay())
            return Integer.compare(day, other.getDay());

        if (hour != other.getHour())
            return Integer.compare(hour, other.getHour());

        return Integer.compare(minute, other.getMinute());
    }

    private static int daysInMonth(int year, int month) {
        switch (month) {
            case 2:
                boolean leap = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
                return leap ? 29 : 28;
            case 4: case 6: case 9: case 11:
                return 30;
            default:
                return 31;
        }
    }
}
