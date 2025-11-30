package org.example;

public class ProductStock {

    public int stock;
    public int reserved;
    public int maxCapacity;
    public int reorderThreshold;

    public ProductStock(int initialStock, int maxCapacity, int reorderThreshold) {

        if (initialStock < 0 || maxCapacity <= 0 || reorderThreshold < 0)
            throw new IllegalArgumentException("Invalid input values.");

        if (initialStock > maxCapacity)
            throw new IllegalArgumentException("Initial stock exceeds capacity.");

        this.stock = initialStock;
        this.maxCapacity = maxCapacity;
        this.reorderThreshold = reorderThreshold;
        this.reserved = 0;
    }


    public void addStock(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");

        if (stock + amount > maxCapacity)
            throw new IllegalArgumentException("Cannot exceed max capacity.");

        this.stock += amount;
    }


    public void reserve(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");

        if (amount > stock)
            throw new IllegalStateException("Not enough stock to reserve.");

        stock -= amount;
        reserved += amount;
    }


    public void releaseReservation(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");

        if (amount > reserved)
            throw new IllegalStateException("Cannot release more than reserved.");

        reserved -= amount;
        stock += amount;
    }


    public void shipReserved(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");

        if (amount > reserved)
            throw new IllegalStateException("Not enough reserved stock to ship.");

        reserved -= amount;
    }


    public void removeDamaged(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive.");

        if (amount > stock)
            throw new IllegalStateException("Cannot remove more than available stock.");

        stock -= amount;
    }


    public boolean isReorderNeeded() {
        return stock < reorderThreshold;
    }


    public void updateReorderThreshold(int t) {
        if (t < 0)
            throw new IllegalArgumentException("Threshold must be >= 0.");

        reorderThreshold = t;
    }


    public void updateMaxCapacity(int c) {
        if (c <= 0)
            throw new IllegalArgumentException("Capacity must be > 0.");

        if (c < stock)
            throw new IllegalStateException("New capacity must be >= current stock.");

        maxCapacity = c;
    }
}
