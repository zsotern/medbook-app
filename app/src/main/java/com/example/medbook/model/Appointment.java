package com.example.medbook.model;

public class Appointment {
    private String appointmentId;
    private String specialtyId;  // Azonosító a Specialty osztályból
    private String doctorId;     // Azonosító a Doctor osztályból
    private String userId;
    private String year;
    private String month;
    private String day;
    private String time;
    //private String userId;

    // Üres konstruktor Firestore számára
    public Appointment() {}


    // Paraméteres konstruktor
    public Appointment(String appointmentId, String specialtyId, String doctorId, String userId, String year, String month, String day, String time) {
        this.appointmentId = appointmentId;
        this.specialtyId = specialtyId;
        this.doctorId = doctorId;
        this.userId = userId;
        this.year = year;
        this.month = month;
        this.day = day;
        this.time = time;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(String specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
