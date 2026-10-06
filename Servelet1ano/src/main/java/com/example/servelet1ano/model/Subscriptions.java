package com.example.servelet1ano.model;
import java.util.Date;

public class Subscriptions {
    private int id;
    private int companyId;
    private int planId;
    private boolean isActive;
    private boolean installments;
    private Date deactivatedAt;
    private Date createdAt;
    private Date updatedAt;
    private Companies company;

    public Subscriptions() {
    }

    public Subscriptions(int id, int companyId, int planId, boolean isActive, boolean installments, Date createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.planId = planId;
        this.isActive = isActive;
        this.installments = installments;
        this.createdAt = createdAt;
    }

    public Subscriptions(boolean isActive, boolean installments) {
        this.isActive = isActive;
        this.installments = installments;
    }

    public Subscriptions(int id, int companyId, int planId, boolean isActive, boolean installments, Date deactivatedAt, Companies company) {
        this.id = id;
        this.companyId = companyId;
        this.planId = planId;
        this.isActive = isActive;
        this.installments = installments;
        this.deactivatedAt = deactivatedAt;
        this.company = company;
    }

    //    Getters e setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isInstallments() {
        return installments;
    }

    public void setInstallments(boolean installments) {
        this.installments = installments;
    }

    public Date getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(Date deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "Subscriptions{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", planId=" + planId +
                ", isActive=" + isActive +
                ", installments=" + installments +
                ", deactivatedAt=" + deactivatedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", company=" + company +
                '}';
    }
}