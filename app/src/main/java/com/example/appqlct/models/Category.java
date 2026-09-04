package com.example.appqlct.models;

public class Category {
    private int id;
    private int userId;
    private String name;
    private String icon;
    private String color;
    private String type; // "income" or "expense"
    private boolean isDefault;

    public Category() {}

    public Category(int id, int userId, String name, String icon, String color, String type, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.type = type;
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

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
