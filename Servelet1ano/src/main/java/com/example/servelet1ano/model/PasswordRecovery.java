package com.example.servelet1ano.model;

import java.sql.Timestamp;

public class PasswordRecovery {

    private long id;
    private int employeeId;
    private String codeHash;
    private Timestamp expiresAt;
    private int attempts;
    private Timestamp usedAt;
    private Timestamp createdAt;

    public PasswordRecovery() {
    }

    public PasswordRecovery(
            long id,
            int employeeId,
            String codeHash,
            Timestamp expiresAt,
            int attempts,
            Timestamp usedAt,
            Timestamp createdAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Timestamp getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Timestamp usedAt) {
        this.usedAt = usedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}