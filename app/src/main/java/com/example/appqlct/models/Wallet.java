package com.example.appqlct.models;

public class Wallet {
    private int id;
    private int userId;
    private String name;
    private String icon;
    private double balance;
    private String color;
    private boolean isDefault;
    private String createdAt;

    public Wallet() {}

    public Wallet(int id, int userId, String name, String icon, double balance, String color, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.balance = balance;
        this.color = color;
        this.isDefault = isDefault;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
