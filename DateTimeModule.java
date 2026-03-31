import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

public class DateTimeModule {

    public static Map<String, BuiltinFunction> load() {
        Map<String, BuiltinFunction> funcs = new HashMap<>();

        // ============================================================
        // CURRENT TIME COMPONENTS
        // ============================================================

        funcs.put("year", (args, line) -> {
            checkArgs("year", 0, args, line);
            return new IntValue(LocalDateTime.now().getYear());
        });

        funcs.put("month", (args, line) -> {
            checkArgs("month", 0, args, line);
            return new IntValue(LocalDateTime.now().getMonthValue());
        });

        funcs.put("day", (args, line) -> {
            checkArgs("day", 0, args, line);
            return new IntValue(LocalDateTime.now().getDayOfMonth());
        });

        funcs.put("hour", (args, line) -> {
            checkArgs("hour", 0, args, line);
            return new IntValue(LocalDateTime.now().getHour());
        });

        funcs.put("minute", (args, line) -> {
            checkArgs("minute", 0, args, line);
            return new IntValue(LocalDateTime.now().getMinute());
        });

        funcs.put("second", (args, line) -> {
            checkArgs("second", 0, args, line);
            return new IntValue(LocalDateTime.now().getSecond());
        });

        funcs.put("millisecond", (args, line) -> {
            checkArgs("millisecond", 0, args, line);
            return new IntValue(LocalDateTime.now().getNano() / 1_000_000);
        });

        // ============================================================
        // DATE INFO
        // ============================================================

        // now() — full formatted string "2026-03-22 14:30:45"
        funcs.put("now", (args, line) -> {
            checkArgs("now", 0, args, line);
            return new StringValue(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });

        // date() — just date part "2026-03-22"
        funcs.put("date", (args, line) -> {
            checkArgs("date", 0, args, line);
            return new StringValue(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        // time() — just time part "14:30:45"
        funcs.put("time", (args, line) -> {
            checkArgs("time", 0, args, line);
            return new StringValue(LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });

        // dayOfWeek() — "Monday", "Tuesday" etc.
        funcs.put("dayOfWeek", (args, line) -> {
            checkArgs("dayOfWeek", 0, args, line);
            return new StringValue(LocalDate.now().getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        });

        // dayOfYear() — 1-365/366
        funcs.put("dayOfYear", (args, line) -> {
            checkArgs("dayOfYear", 0, args, line);
            return new IntValue(LocalDate.now().getDayOfYear());
        });

        // weekOfYear() — 1-52
        funcs.put("weekOfYear", (args, line) -> {
            checkArgs("weekOfYear", 0, args, line);
            return new IntValue(LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear()));
        });

        // isWeekend() — true if Saturday or Sunday
        funcs.put("isWeekend", (args, line) -> {
            checkArgs("isWeekend", 0, args, line);
            DayOfWeek dow = LocalDate.now().getDayOfWeek();
            return new BooleanValue(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        });

        // isWeekday() — true if Monday-Friday
        funcs.put("isWeekday", (args, line) -> {
            checkArgs("isWeekday", 0, args, line);
            DayOfWeek dow = LocalDate.now().getDayOfWeek();
            return new BooleanValue(dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY);
        });

        // isLeapYear(year) — boolean
        funcs.put("isLeapYear", (args, line) -> {
            checkArgs("isLeapYear", 1, args, line);
            int year = args.get(0).asInt();
            return new BooleanValue(Year.isLeap(year));
        });

        // daysInMonth(month, year) — 28/29/30/31
        funcs.put("daysInMonth", (args, line) -> {
            checkArgs("daysInMonth", 2, args, line);
            int month = args.get(0).asInt();
            int year  = args.get(1).asInt();
            if (month < 1 || month > 12)
                throw new NovaRuntimeException("daysInMonth() month must be 1-12", line);
            return new IntValue(YearMonth.of(year, month).lengthOfMonth());
        });

        // ============================================================
        // TIMESTAMPS
        // ============================================================

        // timestamp() — epoch seconds
        funcs.put("timestamp", (args, line) -> {
            checkArgs("timestamp", 0, args, line);
            return new LongValue(System.currentTimeMillis() / 1000L);
        });

        // fromTimestamp(secs) — formatted date string from epoch seconds
        funcs.put("fromTimestamp", (args, line) -> {
            checkArgs("fromTimestamp", 1, args, line);
            long secs = args.get(0).asLong();
            LocalDateTime dt = LocalDateTime.ofEpochSecond(
                secs, 0, ZoneOffset.systemDefault().getRules()
                    .getOffset(java.time.Instant.now()));
            return new StringValue(dt.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });

        // ============================================================
        // ARITHMETIC
        // ============================================================

        // daysBetween(y1, m1, d1, y2, m2, d2) — days between two dates
        funcs.put("daysBetween", (args, line) -> {
            checkArgs("daysBetween", 6, args, line);
            LocalDate d1 = LocalDate.of(
                args.get(0).asInt(), args.get(1).asInt(), args.get(2).asInt());
            LocalDate d2 = LocalDate.of(
                args.get(3).asInt(), args.get(4).asInt(), args.get(5).asInt());
            return new LongValue(Math.abs(ChronoUnit.DAYS.between(d1, d2)));
        });

        // addDays(y, m, d, n) — returns new date string n days ahead
        funcs.put("addDays", (args, line) -> {
            checkArgs("addDays", 4, args, line);
            LocalDate date = LocalDate.of(
                args.get(0).asInt(), args.get(1).asInt(), args.get(2).asInt());
            date = date.plusDays(args.get(3).asLong());
            return new StringValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        // addMonths(y, m, d, n) — returns new date string n months ahead
        funcs.put("addMonths", (args, line) -> {
            checkArgs("addMonths", 4, args, line);
            LocalDate date = LocalDate.of(
                args.get(0).asInt(), args.get(1).asInt(), args.get(2).asInt());
            date = date.plusMonths(args.get(3).asLong());
            return new StringValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        // addYears(y, m, d, n) — returns new date string n years ahead
        funcs.put("addYears", (args, line) -> {
            checkArgs("addYears", 4, args, line);
            LocalDate date = LocalDate.of(
                args.get(0).asInt(), args.get(1).asInt(), args.get(2).asInt());
            date = date.plusYears(args.get(3).asLong());
            return new StringValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        // ============================================================
        // FORMATTING
        // ============================================================

        // format(day, month, year) — "22/03/2026"
        funcs.put("format", (args, line) -> {
            checkArgs("format", 3, args, line);
            int d = args.get(0).asInt();
            int m = args.get(1).asInt();
            int y = args.get(2).asInt();
            return new StringValue(
                String.format("%02d/%02d/%04d", d, m, y));
        });

        // formatTime(hour, min, sec) — "14:30:45"
        funcs.put("formatTime", (args, line) -> {
            checkArgs("formatTime", 3, args, line);
            int h  = args.get(0).asInt();
            int mi = args.get(1).asInt();
            int s  = args.get(2).asInt();
            return new StringValue(
                String.format("%02d:%02d:%02d", h, mi, s));
        });

        // formatFull(day, month, year, hour, min, sec) — "22/03/2026 14:30:45"
        funcs.put("formatFull", (args, line) -> {
            checkArgs("formatFull", 6, args, line);
            int d  = args.get(0).asInt();
            int mo = args.get(1).asInt();
            int y  = args.get(2).asInt();
            int h  = args.get(3).asInt();
            int mi = args.get(4).asInt();
            int s  = args.get(5).asInt();
            return new StringValue(
                String.format("%02d/%02d/%04d %02d:%02d:%02d", d, mo, y, h, mi, s));
        });

        return funcs;
    }

    private static void checkArgs(String name, int expected, List<Value> args, int line) {
        if (args.size() != expected)
            throw new ArgumentException(name, expected, args.size(), line);
    }
}