package ru.kirill.output;

import ru.kirill.bl.StepTracker;
import ru.kirill.bl.TrackerCommand;

import static ru.kirill.bl.TrackerCommand.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static class TrackerCommandExecutor {
        private TrackerCommand command;
        private static final Map<TrackerCommand, Runnable> commandToAction;

        static {
            commandToAction = new HashMap<>();
            commandToAction.put(Help, Main::printStartMessage);
            commandToAction.put(Exit, () -> {
                System.out.println("Надеюсь, наше приложение Вам помогло! Удачи!");
                System.exit(0);
            });
            commandToAction.put(ChangeGoal, () -> {
                System.out.print("Введите желаемую цель шагов в день (Пример: 10000): ");
                var goal = sc.nextLine();
                try {
                    int intGoal = Integer.parseInt(goal);
                    tracker.setGoal(intGoal);
                } catch (NumberFormatException e) {
                    printErrorMessage("Вы ввели не целое число. Операция не завершена.");
                }


            });
            commandToAction.put(PrintStat, () -> {
                System.out.print("Введите месяц на русском языке: ");
                String month = sc.nextLine();
                System.out.println(tracker.getStatistic(month));
            });
            commandToAction.put(AddSteps, () -> {
                addOrSetSteps(true);
            });
            commandToAction.put(EnterSteps, () -> {
                addOrSetSteps(false);
            });
        }

        private static void addOrSetSteps(boolean isAddingSteps) {
            System.out.print("Введите месяц для добавления шагов (Пример: Апрель): ");
            String month = sc.nextLine();
            System.out.print("Введите день для добавления шагов (Пример: 10): ");
            int day;
            String enter = sc.nextLine();
            if (isInteger(enter)) {
                day = Integer.parseInt(enter);
            } else { printErrorMessage("Вы ввели не целое число. Операция отменена."); return; }
            System.out.print("Введите количество шагов для добавления (Пример: 3400): ");
            int steps;
            enter = sc.nextLine();
            if (isInteger(enter)) {
                steps = Integer.parseInt(enter);
            } else { printErrorMessage("Вы ввели не целое число. Операция отменена."); return; }
            try {
                tracker.saveStepsByMonthDay(month, day, steps, isAddingSteps);
            } catch (IllegalArgumentException e) {
                printErrorMessage(String.format("Неверные аргументы. Дни должны быть от 1 до 31," +
                        "а месяц согласно примеру в команде %s", Help.getDescription()));
            }
        }

        private static boolean isInteger(String str) {
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) { return false; }
        }

        public static void execute(TrackerCommand command) {
            commandToAction.get(command).run();
        }
    }
    private static final Scanner sc = new Scanner(System.in);
    private static final StepTracker tracker = new StepTracker();

    public static void main(String[] args) throws ParseException {
        printStartMessage();
        while (true) {
            System.out.print("Введите команду: ");
            TrackerCommand command;
            try {
                command = getCommandFromConsole();
            } catch (NoSuchElementException e) {
                printErrorMessage("Такой команды не существует.\nЧтобы просмотреть список команд, введите Помощь");
                continue;
            }
            TrackerCommandExecutor.execute(command);
        }
    }

    private static void printErrorMessage(String title) {
        System.out.printf("Ошибка! %s%n", title);
    }

    private static TrackerCommand getCommandFromConsole() {
        String enteredString = sc.nextLine();
        for (var val : TrackerCommand.values()) {
            if (enteredString.strip().equals(val.getDescription())) {
                return val;
            }
        }
        throw new NoSuchElementException("Команды не существует");
    }

    private static void printStartMessage() {
        System.out.println(String.format("""
                               🔆Приветствую в приложении🔆
                                    👟StepTracker👟
                Именно здесь ты можешь отслеживать свои шаги за день и собирать статистику! 📈
                🎯 Чтобы задать цель по шагам на день (изначально 10.000), введи %s,
                🚶‍♂️ Чтобы записать количество шагов за сегодня, введи %s,
                ➕ Чтобы добавить шаги к нужной дате, введи %s,
                📜 Чтобы просмотреть статистику за определенный месяц, введи %s
                
                🚪 Для завершения работы приложения, введи %s.
                                
                             🔑Удачного пользования и успехов!🔑
                """, ChangeGoal.getDescription(), EnterSteps.getDescription(),
                AddSteps.getDescription(), PrintStat.getDescription(), Help.getDescription()));
    }
}
