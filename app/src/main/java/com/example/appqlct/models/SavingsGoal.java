package com.example.appqlct.models;

public class SavingsGoal {
    private int id;
    private int userId;
    private String name;
    private double targetAmount;
    private double currentAmount;
    private String deadline;
    private String status; // "in_progress", "completed", "cancelled"
    private String createdAt;

    public SavingsGoal() {}

    public SavingsGoal(int id, int userId, String name, double targetAmount, double currentAmount, String deadline, String status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getPercentage() {
        if (targetAmount <= 0) return 0;
        int p = (int) Math.round((currentAmount / targetAmount) * 100);
        return Math.min(p, 100);
    }
}
