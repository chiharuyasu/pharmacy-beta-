package com.example.pharmacyl3;

public class Notification {
    private int id;
    private String type; // e.g., "LOW_STOCK", "EXPIRING_SOON"
    private String message;
    private String timestamp;
    private boolean isRead;

    public Notification(int id, String type, String message, String timestamp, boolean isRead) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public Notification(String type, String message, String timestamp, boolean isRead) {
        this(-1, type, message, timestamp, isRead);
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
