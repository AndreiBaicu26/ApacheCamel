package project;

public class Result {

    private int totalHolidays;
    private String holidayMonth;

    public Result(int totalHolidays, String holidayMonth) {
        this.totalHolidays = totalHolidays;
        this.holidayMonth = holidayMonth;
    }

    public int getTotalHolidays() {
        return totalHolidays;
    }

    public String getHolidayMonth() {
        return holidayMonth;
    }

    @Override
    public String toString() {
        return "Result{" +
                "totalHolidays=" + totalHolidays +
                ", holidayMonth='" + holidayMonth + '\'' +
                '}';
    }
}
