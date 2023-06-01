package ru.kirill.bl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;

public class StepTracker {
    private static class Converter {
        private static final double KILOMETERS_IN_STEP = 0.00075;
        private static final double CALORIES_IN_STEP = 50;
        private static final double KILOCALORIES_IN_CALORIES = 0.001;
        public static double getKilometersFromSteps(int steps) {
            return steps * KILOMETERS_IN_STEP;
        }

        public static double getCaloriesFromSteps(int steps) {
            return steps * CALORIES_IN_STEP;
        }

        public static double getKilocaloriesInSteps(int steps) {
            return getCaloriesFromSteps(steps) * KILOCALORIES_IN_CALORIES;
        }
    }
    private static class MonthData implements Comparable<MonthData> {
        private final int month;
        private final int day;

        public MonthData(int month, int day) {
            this.month = month;
            this.day = day;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MonthData monthData = (MonthData) o;
            return month == monthData.month && day == monthData.day;
        }

        @Override
        public int hashCode() {
            return Objects.hash(month, day);
        }

        @Override
        public int compareTo(MonthData o) {
            int months = month - o.month;
            int days = day - o.day;
            if (months == 0 && days == 0) {
                return 0;
            } else if (months < 0) {
                return -1;
            } else if (months == 0 && days < 0) {
                return -1;
            }
            return 1;
        }
    }
    private int goal;
    private static final String MONTH_DATA_FORMAT = "MMMM yyyy";
    private static final String DAY_DATA_FORMAT = "dd MMMM yyyy";
    private static final Calendar calendar = new GregorianCalendar(Locale.forLanguageTag("ru"));
    private Map<MonthData, Integer> stepsByDay;

    public StepTracker() {
        goal = 10000;
        this.stepsByDay = new TreeMap<>();
    }

    public StepTracker(int stepsGoal) {
        this();
        this.goal = stepsGoal;
    }

    public String getStatistic(String monthName) {
        var currentMonthCalendar = getCalendar(monthName, 1);

        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append(String.format("""
                                        📆 СТАТИСТИКА ЗА %s 📆
                """, monthName.toUpperCase()));
        int daysInMonth = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int maxStreak = 0; // лучшая серия перевыполнения цели подряд
        int sumOfSteps = 0; // общее кол-во шагов
        int maxStepsByDay = 0; // максимально шагов за день
        int averageStepsByDay; // среднее кол-во шагов в день
        double coveredDistanceInKilometers; // пройденная дистанция в километрах
        double kiloCaloriesBurned; // сожжённые калории

        int currentStreak = 0;
        Date lastDate = null;
        int stepsByThisDay = 0;
        for (int i = 0; i < daysInMonth; i++) {
            try {
                MonthData monthDataKey = new MonthData(
                        currentMonthCalendar.get(Calendar.MONTH), currentMonthCalendar.get(Calendar.DAY_OF_MONTH));
                stepsByThisDay = stepsByDay.get(monthDataKey);
            } catch (NullPointerException e) {
                // если нет шагов за день - то это 0
                stepsByThisDay = 0;
            }
                sumOfSteps += stepsByThisDay;

                if (maxStepsByDay < stepsByThisDay) {
                    maxStepsByDay = stepsByThisDay;
                }

                if ((Objects.isNull(lastDate)
                || Math.abs(lastDate.compareTo(currentMonthCalendar.getTime())) < 86400000L)
                && stepsByThisDay > goal) {
                    // разница между датами < 1 дня
                    currentStreak++;
                    lastDate = currentMonthCalendar.getTime();
                } else {
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak;
                    }
                    currentStreak = 0;
                }

//                resultMessage.append(DateFormat.getDateInstance(
//                        SimpleDateFormat.LONG, Locale.of("ru")).format(calendar));
                resultMessage.append(String.format("%d день: %s, ", i + 1, stepsByThisDay));
                if (i > 0 && i % 11 == 0)
                {
                    resultMessage.append("\n");
                }

                currentMonthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        averageStepsByDay = sumOfSteps / daysInMonth;
        coveredDistanceInKilometers = Converter.getKilometersFromSteps(sumOfSteps);
        kiloCaloriesBurned = Converter.getKilocaloriesInSteps(sumOfSteps);

        resultMessage.delete(resultMessage.length() - 2, resultMessage.length()); // удаляем запятую
        resultMessage.append(String.format("""
                
                
                Общее количество шагов за месяц: %d
                Максимально пройденное количество шагов в месяце: %d
                Среднее количество шагов за месяц: %d
                Пройденная дистанция (в км): %.2f
                Количество сожжённых килокалорий: %.2f
                Лучшая серия: %d
                ===============================================================================
                """, sumOfSteps, maxStepsByDay, averageStepsByDay, coveredDistanceInKilometers,
                kiloCaloriesBurned, maxStreak));
        return resultMessage.toString();
    }

    public void saveStepsByMonthDay(String month, int day, int steps, boolean isAddingSteps) throws IllegalArgumentException {
        if (steps <= 0) {
            throw new IllegalArgumentException("Шаги должны быть > 0");
        } else if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Дни должны быть в диапазоне от 1 до 31");
        }
        var calendarOfThisDate = getCalendar(month, day);

        MonthData monthDataKey = new MonthData(
                calendarOfThisDate.get(Calendar.MONTH), calendarOfThisDate.get(Calendar.DAY_OF_MONTH));
        if (!isAddingSteps) {
            stepsByDay.put(monthDataKey, steps);
            return;
        }
        stepsByDay.compute(monthDataKey, (k, v) -> (v == null) ? steps : v + steps);
    }

    private Calendar getCalendar(String month, int day) throws IllegalArgumentException {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.YEAR, Year.now().getValue());
        // создаем формат полного месяца на русском
        DateFormat formatter = new SimpleDateFormat("MMMM", Locale.forLanguageTag("ru"));
        try {
            Date month1 = formatter.parse(month);
            Calendar cal = Calendar.getInstance();
            cal.setTime(month1);
            int calMonth = cal.get(Calendar.MONTH);
            // устанавливаем месяц
            currentCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        } catch (ParseException e) { throw new IllegalArgumentException("Неверно введён месяц"); }
        if (day > currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            throw new IllegalArgumentException("День превышает количество в месяце");
        }
        currentCalendar.set(Calendar.DAY_OF_MONTH, day);

        System.out.println("Месяц и день календаря: "
                + currentCalendar.get(Calendar.MONTH)
                + " "
                + currentCalendar.get(Calendar.DAY_OF_MONTH));

        return currentCalendar;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }
}
