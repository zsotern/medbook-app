package com.example.medbook.model;

public class Doctor {
    private String id;               // Egyedi azonosító
    private String name;             // Doktor neve
    private String specialtyId;      // Specialitás azonosítója (kapcsolat)
    private String bio;              // Rövid bemutatkozás
    private int imageResource;

    public Doctor() {}

    public Doctor(String id, String name, String specialtyId, String bio, int imageResource) {
        this.id = id;
        this.name = name;
        this.specialtyId = specialtyId;
        this.bio = bio;
        this.imageResource = imageResource;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(String specialtyId) { this.specialtyId = specialtyId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public int getImageResource() {return imageResource;}
    public void setImageResource(int imageResource) {this.imageResource = imageResource;}
}
