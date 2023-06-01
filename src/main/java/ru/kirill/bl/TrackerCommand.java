package ru.kirill.bl;

public enum TrackerCommand {
    EnterSteps("Ввести шаги"),
    AddSteps("Добавить шаги"),
    PrintStat("Вывести статистику"),
    ChangeGoal("Изменить цель"),
    Help("Помощь"),
    Exit("Выйти");

    private final String description;
    TrackerCommand(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
